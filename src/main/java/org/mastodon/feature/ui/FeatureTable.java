package org.mastodon.feature.ui;

import java.awt.Component;
import java.awt.Dimension;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import org.mastodon.revised.model.tag.ui.AbstractTagTable;
import org.mastodon.util.Listeners;
import org.scijava.ui.behaviour.io.InputTriggerConfig;
import org.scijava.ui.behaviour.util.Actions;

import static javax.swing.JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT;

/**
 *
 * @param <C> collection-of-elements type
 * @param <T> element type
 */
public class FeatureTable< C, T >
{
	private static final ImageIcon UP_TO_DATE_ICON = new ImageIcon( FeatureTable.class.getResource( "bullet_green.png" ) );
	private static final ImageIcon NOT_UP_TO_DATE_ICON = new ImageIcon( FeatureTable.class.getResource( "time.png" ) );

	private C elements;
	private final ToIntFunction< C > size;
	private final BiFunction< C, Integer, T > get;
	private final Function< T, String > getName;
	private final Predicate< T > isSelected;
	private final BiConsumer< T, Boolean > setSelected;
	private final Predicate< T > isUptodate;

	private final Listeners.List< SelectionListener< T > > selectionListeners;

	private final MyTableModel tableModel;

	private final JTable table;

	public FeatureTable(
			final C elements,                           // collection of elements
			final ToIntFunction< C > size,              // given collection returns number of elements
			final BiFunction< C, Integer, T > get,      // given collection and index returns element at index
			final Function< T, String > getName,        // given element returns name
			final Predicate< T > isSelected,            // given element returns whether it is selected
			final BiConsumer< T, Boolean > setSelected, // given element and boolean sets selection of element
			final Predicate< T > isUptodate )           // given element returns whether it is up-to-date
	{
		this.elements = elements;
		this.size = size;
		this.get = get;
		this.getName = getName;
		this.isSelected = isSelected;
		this.setSelected = setSelected;
		this.isUptodate = isUptodate;

		selectionListeners = new Listeners.SynchronizedList<>();

		tableModel = new MyTableModel();
		table = new JTable( tableModel );
		table.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
		table.setTableHeader( null );
		table.setFillsViewportHeight( true );
		table.setAutoResizeMode( JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS );
		table.setRowHeight( 30 );
		table.setIntercellSpacing( new Dimension( 0, 0 ) );
		table.getColumnModel().getColumn( 0 ).setMaxWidth( 30 );
		table.getColumnModel().getColumn( 2 ).setMaxWidth( 64 );
		table.getColumnModel().getColumn( 2 ).setCellRenderer( new UpdatedCellRenderer() );
		table.setShowGrid( false );

		final Actions actions = new Actions( table.getInputMap( WHEN_ANCESTOR_OF_FOCUSED_COMPONENT ), table.getActionMap(), new InputTriggerConfig() );
		actions.runnableAction( this::toggleSelectedRow, "toggle selected row", "SPACE", "ENTER" );

		setElements( elements );
	}

	private void toggleSelectedRow()
	{
		final int row = table.getSelectedRow();
		if ( row >= 0 )
		{
			final T feature = get.apply( elements, row );
			setSelected.accept( feature, !isSelected.test( feature ) );
			tableModel.fireTableCellUpdated( row, 0 );
		}
	}

	/**
	 * Exposes the component in which the elements are displayed.
	 *
	 * @return the component.
	 */
	public JComponent getComponent()
	{
		return table;
	}

	public ListSelectionModel getListSelectionModel()
	{
		return table.getSelectionModel();
	}

	/**
	 * Sets the collection of elements to show.
	 *
	 * @param elements
	 *            the collection of elements to show.
	 */
	public void setElements( final C elements )
	{
		this.elements = elements;
		if ( elements == null )
		{
			selectionListeners.list.forEach( l -> l.selectionChanged( null ) );
		}
		else
		{
			tableModel.fireTableDataChanged();
			if ( table.getRowCount() > 0 )
				table.setRowSelectionInterval( 0, 0 );
		}
	}


	public interface SelectionListener< T >
	{
		void selectionChanged( T selected );
	}

	public Listeners< SelectionListener< T > > selectionListeners()
	{
		return selectionListeners;
	}

	private class MyTableModel extends DefaultTableModel
	{

		private static final long serialVersionUID = 1L;

		@Override
		public int getColumnCount()
		{
			return 3;
		}

		@Override
		public int getRowCount()
		{
			return ( null == elements ) ? 0 : size.applyAsInt( elements );
		}

		public T get( final int index )
		{
			return get.apply( elements, Integer.valueOf( index ) );
		}

		@Override
		public Object getValueAt( final int rowIndex, final int columnIndex )
		{
			switch ( columnIndex )
			{
			case 0:
				return isSelected.test( get( rowIndex ) );
			case 1:
				return getName.apply( get( rowIndex ) );
			case 2:
				return isUptodate.test( get( rowIndex ) );
			}
			throw new IllegalArgumentException( "Cannot return value for colum index larger than " + getColumnCount() );
		}

		@Override
		public Class< ? > getColumnClass( final int columnIndex )
		{
			switch ( columnIndex )
			{
			case 0:
				return Boolean.class;
			case 1:
				return String.class;
			case 2:
				return Boolean.class;
			}
			throw new IllegalArgumentException( "Cannot return value for colum index larger than " + getColumnCount() );
		}

		@Override
		public boolean isCellEditable( final int rowIndex, final int columnIndex )
		{
			return columnIndex == 0;
		}

		@Override
		public void setValueAt( final Object aValue, final int rowIndex, final int columnIndex )
		{
			final boolean selected =  (columnIndex == 0)
					? ( boolean ) aValue
					: !isSelected.test( get( rowIndex ) );
			if ( selected != isSelected.test( get( rowIndex ) ) )
			{
				setSelected.accept( get( rowIndex ), ( Boolean ) aValue );
				fireTableRowsUpdated( rowIndex, rowIndex );
				for ( final SelectionListener< T > listener : selectionListeners.list )
					listener.selectionChanged( get( rowIndex ) );
			}
		}
	}

	private class UpdatedCellRenderer implements TableCellRenderer
	{

		private final DefaultTableCellRenderer renderer;

		public UpdatedCellRenderer()
		{
			this.renderer = new DefaultTableCellRenderer();
			final JLabel label = ( JLabel ) renderer.getTableCellRendererComponent( null, null, false, false, 0, 0 );
			label.setHorizontalAlignment( SwingConstants.CENTER );
		}

		@Override
		public Component getTableCellRendererComponent(
				final JTable table,
				final Object value,
				final boolean isSelected,
				final boolean hasFocus,
				final int row,
				final int column )
		{
			final JLabel label = ( JLabel ) renderer.getTableCellRendererComponent( table, value, isSelected, hasFocus, row, column );
			label.setIcon( isUptodate.test( get.apply( elements, Integer.valueOf( row ) ) )
					? UP_TO_DATE_ICON
					: NOT_UP_TO_DATE_ICON );
			label.setText( "" );
			return label;
		}
	}
}
