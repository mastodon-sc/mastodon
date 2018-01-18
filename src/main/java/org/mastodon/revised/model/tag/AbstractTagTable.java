package org.mastodon.revised.model.tag;

import static javax.swing.JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.KeyboardFocusManager;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.ToIntFunction;

import javax.swing.DefaultCellEditor;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.ToolTipManager;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import org.scijava.ui.behaviour.io.InputTriggerConfig;
import org.scijava.ui.behaviour.util.Actions;

public abstract class AbstractTagTable< C, T, E extends AbstractTagTable< ?, T, ? >.Element >
{
	private static final ImageIcon ADD_ICON = new ImageIcon( AbstractTagTable.class.getResource( "add.png" ) );

	private static final ImageIcon REMOVE_ICON = new ImageIcon( AbstractTagTable.class.getResource( "delete.png" ) );

	public class Element
	{
		protected final T wrapped;

		public Element( final T wrapped )
		{
			this.wrapped = wrapped;
		}

		public String getName()
		{
			return wrapped == null ? "" : getName.apply( wrapped );
		}

		public void setName( final String name )
		{
			if ( wrapped != null )
				setName.accept( wrapped, name );
		}
	}

	public abstract class Elements
	{
		protected final C wrapped;

		private final E dummyElement;

		public Elements( final C wrapped )
		{
			this.wrapped = wrapped;
			dummyElement = wrap( null );
		}

		protected abstract E wrap( final T wrapped );

		public E getDummyElement()
		{
			return dummyElement;
		}

		public E addElement()
		{
			return wrap( addElement.apply( wrapped ) );
		}

		public int size()
		{
			return size.applyAsInt( wrapped );
		}

		public void remove( final E element )
		{
			remove.accept( wrapped, element.wrapped );
		}

		public E get( final int index )
		{
			return wrap( get.apply( wrapped, index ) );
		}
	}

	public interface UpdateListener
	{
		void modelUpdated();
	}

	public interface SelectionListener< T >
	{
		void selectionChanged( T selected );
	}

	protected final Function< C, T > addElement;

	protected final ToIntFunction< C > size;

	protected final BiConsumer< C, T > remove;

	protected final BiFunction< C, Integer, T > get;

	protected final BiConsumer< T, String > setName;

	protected final Function< T, String > getName;

	protected Elements elements;

	private final int columnCount;

	private final int buttonColumn;

	protected final MyTableModel tableModel;

	protected final JTable table;

	protected final JScrollPane scrollPane;

	private final ArrayList< UpdateListener > updateListeners;

	private final ArrayList< SelectionListener > selectionListeners;

	protected AbstractTagTable(
			final C elements,
			final Function< C, T > addElement,
			final ToIntFunction< C > size,
			final BiConsumer< C, T > remove,
			final BiFunction< C, Integer, T > get,
			final BiConsumer< T, String > setName,
			final Function< T, String > getName,
			final int numAdditionalCols )
	{
		this.addElement = addElement;
		this.size = size;
		this.remove = remove;
		this.get = get;
		this.setName = setName;
		this.getName = getName;

		columnCount = 2 + numAdditionalCols; // name, additional stuff ..., buttons
		buttonColumn = columnCount - 1; // buttons in last column

		tableModel = new MyTableModel();
		table = new JTable( tableModel );
		updateListeners = new ArrayList<>();
		selectionListeners = new ArrayList<>();

		table.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
		table.setTableHeader( null );
		table.setFillsViewportHeight( true );
		table.setAutoResizeMode( JTable.AUTO_RESIZE_ALL_COLUMNS );
		table.setRowHeight( 30 );
		table.getSelectionModel().addListSelectionListener( e -> {
			if ( e.getValueIsAdjusting() )
				return;
			final int row = table.getSelectedRow();
			T selected = ( this.elements != null && row >= 0 && row < this.elements.size() )
					? this.elements.get( row ).wrapped
					: null;
			selectionListeners.forEach( l -> l.selectionChanged( selected ) );
		} );
		table.getColumnModel().getColumn( 0 ).setCellRenderer( new MyTagSetRenderer() );
		table.getColumnModel().getColumn( 0 ).setCellEditor( new MyTagSetNameEditor() );
		table.getColumnModel().getColumn( buttonColumn ).setCellRenderer( new MyButtonRenderer() );
		table.getColumnModel().getColumn( buttonColumn ).setMaxWidth( 32 );
		table.addMouseListener( new MyTableButtonMouseListener() );
		table.setShowGrid( false );
		table.setIntercellSpacing( new Dimension( 0,0 ) );
		table.setSurrendersFocusOnKeystroke( true );
		table.setFocusTraversalKeys( KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, null );
		table.setFocusTraversalKeys( KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, null );

		ToolTipManager.sharedInstance().unregisterComponent( table );

		final Actions actions = new Actions( table.getInputMap( WHEN_ANCESTOR_OF_FOCUSED_COMPONENT ), table.getActionMap(), new InputTriggerConfig() );
		actions.runnableAction( this::editSelectedRow, "edit selected row", "ENTER" );
		actions.runnableAction( this::removeSelectedRow, "remove selected row", "DELETE", "BACK_SPACE" );

		scrollPane = new JScrollPane();
		scrollPane.setVerticalScrollBarPolicy( ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED );

		setElements( elements );
	}

	protected abstract Elements wrap( final C wrapped );

	public void setElements( final C elements )
	{
		if ( elements == null )
		{
			scrollPane.setViewportView( new JPanel() );
			this.elements = null;
			selectionListeners.forEach( l -> l.selectionChanged( null ) );
		}
		else
		{
			final boolean install = this.elements == null;
			this.elements = wrap( elements );
			if ( install )
				scrollPane.setViewportView( table );
			tableModel.fireTableDataChanged();
		}
	}

	public JComponent getTable()
	{
		return scrollPane;
	}

	protected void update()
	{
	}

	protected void notifyListeners()
	{
		updateListeners.forEach( UpdateListener::modelUpdated );
	}

	public synchronized boolean addUpdateListener( final UpdateListener l )
	{
		if ( !updateListeners.contains( l ) )
		{
			updateListeners.add( l );
			return true;
		}
		return false;
	}

	public synchronized boolean removeUpdateListener( final UpdateListener l )
	{
		return updateListeners.remove( l );
	}

	public synchronized boolean addSelectionListener( final SelectionListener< T > l )
	{
		if ( !selectionListeners.contains( l ) )
		{
			selectionListeners.add( l );
			return true;
		}
		return false;
	}

	public synchronized boolean removeSelectionListener( final SelectionListener l )
	{
		return selectionListeners.remove( l );
	}

	private void addAndEditRow()
	{
		final int row = elements.size();
		elements.addElement();
		tableModel.fireTableRowsInserted( row, row );
		table.setRowSelectionInterval( row, row );
		table.editCellAt( row, 0 );
	}

	private void removeSelectedRow()
	{
		final int row = table.getSelectedRow();
		if ( row >= 0 && row < elements.size() )
		{
			elements.remove( elements.get( row ) );
			notifyListeners();
			tableModel.fireTableRowsDeleted( row, row );
			final int s = Math.max( row - 1, 0 );
			table.setRowSelectionInterval( s, s );
			update();
		}
	}

	private void editSelectedRow()
	{
		final int row = table.getSelectedRow();
		if ( row == elements.size() )
			addAndEditRow();
		else if ( row >= 0 )
			table.editCellAt( row, 0 );
	}

	protected class MyTableModel extends AbstractTableModel
	{
		private static final long serialVersionUID = 1L;

		@Override
		public boolean isCellEditable( final int rowIndex, final int columnIndex )
		{
			// last row (with the Add button) is not editable
			if ( rowIndex == getRowCount() - 1 )
				return false;

			// only the column with the element name is editable
			return columnIndex == 0;
		}

		@Override
		public int getColumnCount()
		{
			return columnCount;
		}

		@Override
		public int getRowCount()
		{
			return elements.size() + 1;
		}

		@Override
		public Object getValueAt( final int rowIndex, final int columnIndex )
		{
			if ( rowIndex >= getRowCount() )
				return null;
			else if ( rowIndex == elements.size() )
				return elements.getDummyElement();
			return elements.get( rowIndex );
		}
	}

	private class MyTagSetRenderer extends DefaultTableCellRenderer
	{
		private static final long serialVersionUID = 1L;

		@Override
		public Component getTableCellRendererComponent( final JTable table, final Object value, final boolean isSelected, final boolean hasFocus, final int row, final int column )
		{
			super.getTableCellRendererComponent( table, value, isSelected, hasFocus, row, column );
			setText( ( ( Element ) value ).getName() );
			return this;
		}
	}

	private class MyButtonRenderer extends JButton implements TableCellRenderer
	{
		private static final long serialVersionUID = 1L;

		public MyButtonRenderer()
		{
			setOpaque( true );
			setBorderPainted( false );
		}

		@Override
		public Component getTableCellRendererComponent( final JTable table, final Object value,
				final boolean isSelected, final boolean hasFocus, final int row, final int column )
		{
			if ( row == elements.size() )
				setIcon( ADD_ICON );
			else
				setIcon( REMOVE_ICON );
			final boolean paintSelected = isSelected && !table.isEditing();
			setForeground( paintSelected ? table.getSelectionForeground() : table.getForeground() );
			setBackground( paintSelected ? table.getSelectionBackground() : table.getBackground() );
			return this;
		}
	}

	private class MyTableButtonMouseListener extends MouseAdapter
	{
		@Override
		public void mouseClicked( final MouseEvent e )
		{
			final int column = table.getColumnModel().getColumnIndexAtX( e.getX() );
			if ( column == buttonColumn )
			{
				if ( table.getSelectedRow() == elements.size() )
					addAndEditRow();
				else
					removeSelectedRow();
			}
		}
	}

	private class MyTagSetNameEditor extends DefaultCellEditor
	{
		private static final long serialVersionUID = 1L;

		private Element edited;

		public MyTagSetNameEditor()
		{
			super( new JTextField() );
			setClickCountToStart( 2 );
			addCellEditorListener( new CellEditorListener()
			{

				@Override
				public void editingStopped( final ChangeEvent e )
				{
					edited.setName( getCellEditorValue().toString() );
					notifyListeners();
				}

				@Override
				public void editingCanceled( final ChangeEvent e )
				{}
			} );
		}

		@Override
		public Component getTableCellEditorComponent( final JTable table, final Object value, final boolean isSelected, final int row, final int column )
		{
			final JTextField editor = ( JTextField ) super.getTableCellEditorComponent( table, value, isSelected, row, column );
			edited = ( Element ) value;
			editor.setText( edited.getName() );
			return editor;
		}
	}
}
