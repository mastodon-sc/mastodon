package org.mastodon.revised.table;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EventObject;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.MatteBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;
import javax.swing.text.JTextComponent;

import org.mastodon.RefPool;
import org.mastodon.collection.ref.RefArrayList;
import org.mastodon.revised.model.feature.Feature;
import org.mastodon.revised.model.feature.FeatureProjection;
import org.mastodon.revised.model.feature.IntFeatureProjection;
import org.mastodon.revised.model.tag.ObjTags;
import org.mastodon.revised.model.tag.TagSetStructure.Tag;
import org.mastodon.revised.model.tag.TagSetStructure.TagSet;
import org.mastodon.undo.UndoPointMarker;

public class FeatureTagTablePanel< O > extends JPanel
{

	private static final long serialVersionUID = 1L;

	private static final Color HIGHLIGHT_BORDER_COLOR = Color.BLACK;

	private static final Color HEADER_BG_COLOR = new Color( 245, 245, 245 );

	private static final Font FOCUS_FONT = new JLabel().getFont().deriveFont( Font.ITALIC );

	private static final int ROW_HEIGHT = 26;

	private final RefPool< O > idBimap;

	private final ObjTags< O > tags;

	private final JTable table;

	private final MyTableModel tableModel;

	private final MyTableCellRenderer cellRenderer;

	private final List< Class< ? > > columnClasses;

	private final List< FeatureProjection< O > > mapToProjections;

	private final List< int[] > mapToTagIndices;

	private final List< TagSet > tagSets;

	private final List< Feature< ?, ? > > featureList;

	private final RefArrayList< O > objects;

	private final RefArrayList< O > filterBy;

	private final Function< O, String > labelGenerator;

	private final BiConsumer< O, String > labelSetter;

	private final UndoPointMarker undoPointMarker;

	private final Comparator< O > cmp;

	private int focusRow = -1;

	private int highlightRow = -1;

	private boolean doFilter = false;

	public FeatureTagTablePanel(
			final ObjTags< O > tags,
			final RefPool< O > idBimap,
			final Function< O, String > labelGenerator,
			final BiConsumer< O, String > labelSetter,
			final UndoPointMarker undoPointMarker )
	{
		this.tags = tags;
		this.idBimap = idBimap;
		this.labelGenerator = labelGenerator;
		this.labelSetter = labelSetter;
		this.undoPointMarker = undoPointMarker;
		this.columnClasses = new ArrayList<>();
		this.mapToProjections = new ArrayList<>();
		this.mapToTagIndices = new ArrayList<>();
		this.featureList = new ArrayList<>();
		this.tagSets = new ArrayList<>();
		this.objects = new RefArrayList<>( idBimap );
		this.filterBy = new RefArrayList<>( idBimap );
		this.cmp = ( o1, o2 ) -> idBimap.getId( o1 ) - idBimap.getId( o2 );

		this.tableModel = new MyTableModel();
		final DefaultTableColumnModel tableColumnModel = new DefaultTableColumnModel();
		this.table = new JTable( tableModel, tableColumnModel )
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected JTableHeader createDefaultTableHeader()
			{
				return new GroupableTableHeader( columnModel );
			}

			@Override
			public boolean isCellEditable( final int row, final int column )
			{
				// Only label and tags are editable.
				return ( labelSetter != null && column == 0 ) || column >= 2 + mapToProjections.size();
			}

			@Override
			public boolean editCellAt( final int row, final int column, final EventObject e )
			{
				// Intercept to block keys.
				final boolean result = super.editCellAt( row, column, e );
				final Component editor = getEditorComponent();
				if ( editor == null || !( editor instanceof JTextComponent ) ) { return result; }

				// Is about text.
				blockKeys( ( JComponent ) editor, table );
				if ( e instanceof MouseEvent )
					EventQueue.invokeLater( () -> ( ( JTextComponent ) editor ).selectAll() );
				else
					SwingUtilities.invokeLater(new Runnable()
					{
					    @Override
						public void run()
					    {
					    	(( JTextComponent ) editor ).selectAll();
					    	(( JTextComponent ) editor ).requestFocusInWindow();
					    }
					});
				return result;
			}
		};
		table.putClientProperty( "JTable.autoStartsEdit", Boolean.FALSE );

		table.setRowHeight( ROW_HEIGHT );
		this.cellRenderer = new MyTableCellRenderer();

		table.getSelectionModel().setSelectionMode( ListSelectionModel.MULTIPLE_INTERVAL_SELECTION );
		refreshColumns();

		final TableRowSorter< MyTableModel > sorter = new TableRowSorter<>( tableModel );
		final RowFilter< MyTableModel, Integer > rowFilter = new MyRowFilter();
		sorter.setRowFilter( rowFilter );
		table.setRowSorter( sorter );

		final JScrollPane scroll = new JScrollPane( table, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED );
		table.setAutoResizeMode( JTable.AUTO_RESIZE_OFF );

		setLayout( new BorderLayout() );
		add( scroll, BorderLayout.CENTER );
	}

	/**
	 * Exposes the {@link JTable} in which the data is displayed.
	 *
	 * @return the table.
	 */
	public JTable getTable()
	{
		return table;
	}

	/**
	 * Returns the object listed at the specified <b>view</b> row index.
	 *
	 * @param viewRowIndex
	 *            the row to query.
	 * @param ref
	 *            a ref object for allocation free retrieval.
	 * @return the object.
	 */
	public O getObjectForViewRow( final int viewRowIndex, final O ref )
	{
		if ( viewRowIndex < 0 )
			return null;
		final int modelRow = table.convertRowIndexToModel( viewRowIndex );
		return objects.get( modelRow, ref );
	}

	public int getViewRowForObject( final O o )
	{
		final int row = Collections.binarySearch( objects, o, cmp );
		if ( row < 0 ) // Object not in table.
			return -1;
		return table.convertRowIndexToView( row );
	}

	/**
	 * Returns the object listed at the specified <b>view</b> row index.
	 *
	 * @param viewRowIndex
	 *            the row to query.
	 * @return the object.
	 */
	public O getObjectForViewRow( final int viewRowIndex )
	{
		return getObjectForViewRow( viewRowIndex, idBimap.createRef() );
	}

	public void scrollToObject( final O o )
	{
		final Rectangle rect = table.getVisibleRect();
		final int row = getViewRowForObject( o );
		final Rectangle cellRect = table.getCellRect( row, 0, true );
		cellRect.setLocation( rect.x, cellRect.y );
		table.scrollRectToVisible( cellRect );
	}

	/**
	 * Sets the collection of objects to display in this table.
	 *
	 * @param rows
	 *            the collection of objects to display (unchanged).
	 */
	public void setRows( final Collection< O > rows )
	{
		objects.clear();
		objects.addAll( rows );
		objects.sort( cmp );
		tableModel.fireTableDataChanged();
	}

	/**
	 * Sets the collection to filter by.
	 * <p>
	 * The displayed objects in this table will be the intersection of the
	 * collection of objects given by {@link #setRows(Collection)} method, and the
	 * specified collection. If the specified collection is <code>null</code>, then
	 * there is no filtering and all objects are displayed.
	 *
	 * @param filterBy
	 *            the collection of objects to filter by (unchanged).
	 */
	public void setFilterBy( final Collection< O > filterBy )
	{
		if ( null == filterBy )
		{
			doFilter = false;
			tableModel.fireTableDataChanged();
			return;
		}
		this.filterBy.clear();
		this.filterBy.addAll( filterBy );
		this.filterBy.sort( cmp );
		doFilter = true;
		tableModel.fireTableDataChanged();
	}

	private void refreshColumns()
	{
		// Class of columns.
		columnClasses.clear();
		// Last line of header is for units.
		final List< String > lastHeaderLine = new ArrayList<>();
		// Map from column index to projection keys.
		mapToProjections.clear();
		// Map from column index to tag indices.
		mapToTagIndices.clear();
		// Table column model.
		final TableColumnModel tableColumnModel = new DefaultTableColumnModel();
		table.setColumnModel( tableColumnModel );

		final GroupableTableHeader header = ( GroupableTableHeader ) table.getTableHeader();
		final TableCellRenderer defaultRenderer = header.getDefaultRenderer();
		header.clear();

		// Top header, bold and not the last line.
		final MyTopHeaderRenderer headerRenderer = new MyTopHeaderRenderer( defaultRenderer, false, true );
		// Top header, last line, not bold.
		final MyTopHeaderRenderer headerRendererLast = new MyTopHeaderRenderer( defaultRenderer, true, false );
		// Top header, mid line, not bold.
		final MyTopHeaderRenderer headerRendererMid = new MyTopHeaderRenderer( defaultRenderer, false, false );

		int colIndex = 0;
		// First 2 columns.
		lastHeaderLine.add( "Label" );
		columnClasses.add( String.class );
		tableColumnModel.addColumn( new TableColumn( colIndex++ ) );
		lastHeaderLine.add( "ID" );
		columnClasses.add( Integer.class );
		tableColumnModel.addColumn( new TableColumn( colIndex++ ) );
		// Units for feature columns.
		for ( final Feature< ?, ? > feature : featureList )
		{

			final List< String > projectionKeys = new ArrayList<>(
					feature.getProjections().keySet() );
			projectionKeys.sort( null );
			for ( final String projectionKey : projectionKeys )
			{
				@SuppressWarnings( "unchecked" )
				final FeatureProjection< O > fp = ( FeatureProjection< O > ) feature.getProjections().get( projectionKey );
				mapToProjections.add( fp );
				final String units = fp.units();
				lastHeaderLine.add( ( units == null || units.isEmpty() ) ? "" : "(" + units + ")" );
				tableColumnModel.addColumn( new TableColumn( colIndex++ ) );

				final Class< ? > pclass;
				if ( fp instanceof IntFeatureProjection )
					pclass = Integer.class;
				else
					pclass = Double.class;
				columnClasses.add( pclass );
			}
		}

		// Last line for tag columns is empty.
		for ( int tagSetID = 0; tagSetID < tagSets.size(); tagSetID++ )
		{
			final TagSet tagSet = tagSets.get( tagSetID );
			final List< Tag > tags = tagSet.getTags();
			for ( int tagID = 0; tagID < tags.size(); tagID++ )
			{
				columnClasses.add( Boolean.class );
				mapToTagIndices.add( new int[] { tagSetID, tagID } );
				lastHeaderLine.add( "" );
				tableColumnModel.addColumn( new TableColumn( colIndex++ ) );
			}
		}

		// Add feature names and feature projection names.
		int index = 2;
		for ( final Feature< ?, ? > feature : featureList )
		{
			final ColumnGroup featureGroup = new ColumnGroup( feature.getKey() );
			featureGroup.setHeaderRenderer( headerRenderer );
			final List< String > projectionKeys = new ArrayList<>(
					feature.getProjections().keySet() );

			if ( projectionKeys.size() == 1 )
			{
				final ColumnGroup projectionGroup = new ColumnGroup( " " );
				projectionGroup.setHeaderRenderer( headerRendererMid );
				projectionGroup.add( tableColumnModel.getColumn( index++ ) );
				featureGroup.add( projectionGroup );
			}
			else
			{
				projectionKeys.sort( null );

				for ( final String projectionKey : projectionKeys )
				{
					final ColumnGroup projectionGroup = new ColumnGroup( projectionKey );
					projectionGroup.setHeaderRenderer( headerRendererMid );
					projectionGroup.add( tableColumnModel.getColumn( index++ ) );
					featureGroup.add( projectionGroup );
				}
			}
			header.addColumnGroup( featureGroup );
		}

		// Add tag set names and tag names.
		for ( final TagSet tagSet : tagSets )
		{
			final ColumnGroup tagSetGroup = new ColumnGroup( tagSet.getName() );
			tagSetGroup.setHeaderRenderer( headerRenderer );
			final List< Tag > tags = tagSet.getTags();
			for ( final Tag tag : tags )
			{
				final ColumnGroup tagGroup = new ColumnGroup( tag.label() );
				tagGroup.add( tableColumnModel.getColumn( index++ ) );
				tagGroup.setHeaderRenderer( new MyTagHeaderRenderer( tag ) );
				tagSetGroup.add( tagGroup );
			}
			header.addColumnGroup( tagSetGroup );
		}

		// Pass last line to column headers and set cell renderer.
		for ( int c = 0; c < tableColumnModel.getColumnCount(); c++ )
		{
			final TableColumn column = tableColumnModel.getColumn( c );
			column.setHeaderValue( lastHeaderLine.get( c ) );
			column.setCellRenderer( cellRenderer );
		}

		table.getTableHeader().setDefaultRenderer( headerRendererLast );
		tableModel.fireTableStructureChanged();
		revalidate();
	}

	public void setFeatures( final Set< Feature< O, ? > > features )
	{
		this.featureList.clear();
		if ( features != null )
		{
			this.featureList.addAll( features );
			this.featureList.sort( ( o1, o2 ) -> o1.getKey().compareTo( o2.getKey() ) );
		}
		refreshColumns();
	}

	public void setTagSets( final List< TagSet > tagSets )
	{
		this.tagSets.clear();
		if ( tagSets != null )
		{
			this.tagSets.addAll( tagSets );
			this.tagSets.sort( ( o1, o2 ) -> o1.getName().compareTo( o2.getName() ) );
		}
		refreshColumns();
	}

	public void focusObject( final O o )
	{
		this.focusRow = ( null == o ) ? -1 : getViewRowForObject( o );
		table.repaint();
	}

	public void highlightObject( final O o )
	{
		this.highlightRow = ( null == o ) ? -1 : getViewRowForObject( o );
		table.repaint();
	}

	private class MyRowFilter extends RowFilter< MyTableModel, Integer >
	{

		private final O ref = idBimap.createRef();

		@Override
		public boolean include( final Entry< ? extends MyTableModel, ? extends Integer > entry )
		{
			if ( !doFilter )
				return true;

			final int modelRow = entry.getIdentifier().intValue();
			final O o = objects.get( modelRow, ref );
			return Collections.binarySearch( filterBy, o, cmp ) >= 0;
		}
	}

	private class MyTableModel extends AbstractTableModel
	{

		private static final long serialVersionUID = 1L;

		private final O ref = idBimap.createRef();

		@Override
		public Class< ? > getColumnClass( final int columnIndex )
		{
			return columnClasses.get( columnIndex );
		}

		@Override
		public int getRowCount()
		{
			return objects.size();
		}

		@Override
		public int getColumnCount()
		{
			return columnClasses.size();
		}

		@Override
		public Object getValueAt( final int rowIndex, final int columnIndex )
		{
			final O o = objects.get( rowIndex, ref );

			if ( columnIndex == 0 )
				return labelGenerator.apply( o );
			else if ( columnIndex == 1 )
				return Integer.valueOf( idBimap.getId( o ) );
			else if ( columnIndex >= 2 && columnIndex < 2 + mapToProjections.size() )
			{
				final FeatureProjection< O > featureProjection = mapToProjections.get( columnIndex - 2 );
				if ( featureProjection.isSet( o ) )
				{
					if ( columnClasses.get( columnIndex ).equals( Integer.class ) )
						return Integer.valueOf( ( int ) featureProjection.value( o ) );
					else
						return Double.valueOf( featureProjection.value( o ) );
				}
				else
					return null;
			}
			else if ( columnIndex >= 2 + mapToProjections.size() )
			{
				final int[] ids = mapToTagIndices.get( columnIndex - ( 2 + mapToProjections.size() ) );
				final TagSet tagSet = tagSets.get( ids[ 0 ] );
				final Tag columnTag = tagSet.getTags().get( ids[ 1 ] );
				final Tag tag = tags.tags( tagSet ).get( o );
				return Boolean.valueOf( columnTag.equals( tag ) );
			}
			else
				return null;
		}

		@Override
		public void setValueAt( final Object aValue, final int rowIndex, final int columnIndex )
		{
			if ( columnIndex == 0 )
			{
				final O o = objects.get( rowIndex, ref );
				labelSetter.accept( o, ( String ) aValue );
				if ( null != undoPointMarker )
					undoPointMarker.setUndoPoint();
			}
			else if ( columnIndex >= 2 + mapToProjections.size() )
			{
				final boolean isSet = ( boolean ) aValue;
				final O o = objects.get( rowIndex, ref );
				final int[] ids = mapToTagIndices.get( columnIndex - ( 2 + mapToProjections.size() ) );
				final TagSet tagSet = tagSets.get( ids[ 0 ] );
				final Tag columnTag = tagSet.getTags().get( ids[ 1 ] );
				if ( isSet )
					tags.tags( tagSet ).set( o, columnTag );
				else
					tags.tags( tagSet ).remove( o );
				if ( null != undoPointMarker )
					undoPointMarker.setUndoPoint();
				fireTableRowsUpdated( rowIndex, rowIndex );
			}

		}
	}

	private class MyTagHeaderRenderer extends DefaultTableCellRenderer
	{

		private static final long serialVersionUID = 1L;

		public MyTagHeaderRenderer( final Tag tag )
		{
			final Color color = new Color( tag.color(), true );
			final JLabel renderer = ( JLabel ) super.getTableCellRendererComponent( table, "", false, false, 0, 0 );
			renderer.setBackground( color );
			renderer.setHorizontalTextPosition( SwingConstants.CENTER );
			renderer.setHorizontalAlignment( SwingConstants.CENTER );
		}
	}

	private class MyTopHeaderRenderer implements TableCellRenderer
	{

		private final TableCellRenderer defaultRenderer;

		private final MatteBorder border;

		private final Font normalFont = new JLabel().getFont();

		private final Font boldFont = normalFont.deriveFont( Font.BOLD );

		private final boolean bold;

		public MyTopHeaderRenderer( final TableCellRenderer defaultRenderer, final boolean bottomLine, final boolean bold )
		{
			this.defaultRenderer = defaultRenderer;
			this.bold = bold;
			this.border = bottomLine
					? BorderFactory.createMatteBorder( 0, 0, 1, 1, Color.GRAY )
					: BorderFactory.createMatteBorder( 0, 0, 0, 1, Color.GRAY );
		}

		@Override
		public Component getTableCellRendererComponent( final JTable table, final Object value, final boolean isSelected, final boolean hasFocus, final int row, final int column )
		{
			final JLabel renderer = ( JLabel ) defaultRenderer.getTableCellRendererComponent( table, value, isSelected, hasFocus, row, column );
			renderer.setFont( bold ? boldFont : normalFont );
			renderer.setBorder( border );
			renderer.setBackground( HEADER_BG_COLOR );
			return renderer;
		}
	}

	private class MyTableCellRenderer extends DefaultTableCellRenderer
	{

		private final Border normalBorder;

		private final MatteBorder highlightBorderCenter;

		private final MatteBorder highlightBorderRight;

		private final MatteBorder highlightBorderLeft;

		private final Font normalFont;

		private final NumberFormat nf;

		private final JCheckBox checkBox;

		private static final long serialVersionUID = 1L;

		public MyTableCellRenderer()
		{
			this.normalBorder = ( ( JLabel ) super.getTableCellRendererComponent( table, "", false, false, 0, 0 ) ).getBorder();
			this.highlightBorderCenter = BorderFactory.createMatteBorder( 2, 0, 2, 0, HIGHLIGHT_BORDER_COLOR );
			this.highlightBorderLeft = BorderFactory.createMatteBorder( 2, 2, 2, 0, HIGHLIGHT_BORDER_COLOR );
			this.highlightBorderRight = BorderFactory.createMatteBorder( 2, 0, 2, 2, HIGHLIGHT_BORDER_COLOR );
			this.normalFont = table.getFont();
			this.nf = NumberFormat.getInstance();
			this.checkBox = new JCheckBox();
			checkBox.setHorizontalAlignment( SwingConstants.CENTER );
			checkBox.setBorderPainted( true );
		}

		@Override
		public Component getTableCellRendererComponent( final JTable table, final Object value, final boolean isSelected, final boolean hasFocus, final int row, final int column )
		{
			final JComponent c;
			if ( value instanceof Boolean )
			{
				// Special case: boolean. We prepare the checkbox.
				final Boolean boolValue = ( Boolean ) value;
				checkBox.setSelected( boolValue.booleanValue() );
				c = checkBox;
			}
			else
			{
				c = ( JComponent ) super.getTableCellRendererComponent( table, value, isSelected, hasFocus, row, column );
			}

			c.setBorder( normalBorder );

			if ( isSelected )
			{
				c.setBackground( table.getSelectionBackground() );
				c.setForeground( table.getSelectionForeground() );
			}
			else
			{
				c.setBackground( table.getBackground() );
				c.setForeground( table.getForeground() );
			}

			if ( hasFocus )
			{
				c.setBackground( table.getSelectionBackground().darker().darker() );
				c.setForeground( table.getSelectionForeground() );
			}

			if ( row == highlightRow )
			{
				if ( column == 0 )
					c.setBorder( highlightBorderLeft );
				else if ( column == table.getColumnCount() - 1 )
					c.setBorder( highlightBorderRight );
				else
					c.setBorder( highlightBorderCenter );
			}
			c.setFont( focusRow == row ? FOCUS_FONT : normalFont );

			if ( value instanceof Double )
			{
				setHorizontalAlignment( JLabel.RIGHT );
				final Double doubleValue = ( Double ) value;
				setText( nf.format( doubleValue.doubleValue() ) );
			}
			else if ( value instanceof Number )
			{
				setHorizontalAlignment( JLabel.RIGHT );
			}
			else
			{
				setHorizontalAlignment( JLabel.CENTER );
			}

			return c;
		}
	}

	/**
	 * Starts editing the label of the object currently selected in the table.
	 * Has not effect if the table focus is not on the first column (the label
	 * column).
	 */
	public void editCurrentLabel()
	{
		final int col = table.getSelectedColumn();
		final int row = table.getSelectedRow();
		if ( col != 0  || row < 0)
			return;
		table.editCellAt( row, col );
	}

	/**
	 * Toggles the tag that currently has the focus in the table. Does nothing if
	 * the focused cell is not a tag.
	 */
	public void toggleCurrentTag()
	{
		final int col = table.getSelectedColumn();
		final int row = table.getSelectedRow();
		if ( col < ( 2 + mapToProjections.size() ) || row < 0 )
			return;

		final O o = getObjectForViewRow( row );
		final int[] ids = mapToTagIndices.get( col - ( 2 + mapToProjections.size() ) );
		final TagSet tagSet = tagSets.get( ids[ 0 ] );
		final Tag columnTag = tagSet.getTags().get( ids[ 1 ] );
		final boolean isSet = columnTag.equals( tags.tags( tagSet ).get( o ) );
		if ( !isSet )
			tags.tags( tagSet ).set( o, columnTag );
		else
			tags.tags( tagSet ).remove( o );
		if ( null != undoPointMarker )
			undoPointMarker.setUndoPoint();

		final int modelRow = table.convertRowIndexToModel( row );
		tableModel.fireTableRowsUpdated( modelRow, modelRow );
	}

	/**
	 * Adapted from Jan Funke's code in
	 * https://github.com/saalfeldlab/bigcat/blob/janh5/src/main/java/bdv/bigcat/ui/BigCatTable.java#L112-L143
	 *
	 * @param editorComponent
	 */
	private static void blockKeys( final JComponent editorComponent, final JComponent from )
	{
		final ArrayList< KeyStroke > allTableKeys = new ArrayList<>();
		for ( Container c = from; c != null; c = c.getParent() )
		{
			if ( c instanceof JComponent )
			{
				final InputMap inputMap = ( ( JComponent ) c ).getInputMap( JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT );
				final KeyStroke[] tableKeys = inputMap.allKeys();
				if ( tableKeys != null )
					allTableKeys.addAll( Arrays.asList( tableKeys ) );
			}
		}

		final Action nada = new AbstractAction()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed( final ActionEvent e )
			{}
		};
		editorComponent.getActionMap().put( "nothing", nada );

		final InputMap inputMap = editorComponent.getInputMap( JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT );
		for ( final KeyStroke key : allTableKeys )
			inputMap.put( key, "nothing" );
	}
}
