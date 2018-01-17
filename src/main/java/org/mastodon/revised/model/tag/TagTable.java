package org.mastodon.revised.model.tag;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.KeyboardFocusManager;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.DefaultCellEditor;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.ToolTipManager;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import org.scijava.ui.behaviour.io.InputTriggerConfig;
import org.scijava.ui.behaviour.util.Actions;

import static javax.swing.JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT;

public class TagTable< T extends TagTable.Element >
{
	private static final long serialVersionUID = 1L;

	private static final ImageIcon ADD_ICON = new ImageIcon( TagTable.class.getResource( "add.png" ) );

	private static final ImageIcon REMOVE_ICON = new ImageIcon( TagTable.class.getResource( "delete.png" ) );

	public interface Element
	{
		String getName();

		void setName( String name );
	}

	public interface Elements< T extends Element > extends List< T >
	{
		Element getDummyElement();

		Element addElement();
	}

	public interface UpdateListener
	{
		void modelUpdated();
	}

	private Elements< T > elements;

	private final MyTableModel tableModel;

	private final JTable table;

	private final ArrayList< UpdateListener > listeners;

	private final int buttonColumn = 1;

	public TagTable( final Elements< T > elements )
	{
		this.elements = elements;

		tableModel = new MyTableModel();
		table = new JTable( tableModel );
		listeners = new ArrayList<>();

		table.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
		table.setTableHeader( null );
		table.setFillsViewportHeight( true );
		table.setAutoResizeMode( JTable.AUTO_RESIZE_ALL_COLUMNS );
		table.setRowHeight( 30 );
		table.getSelectionModel().addListSelectionListener( e -> {
			if ( e.getValueIsAdjusting() )
				return;
			update();
		} );
		table.getColumnModel().getColumn( 0 ).setCellRenderer( new MyTagSetRenderer() );
		table.getColumnModel().getColumn( 0 ).setCellEditor( new MyTagSetNameEditor() );
		table.getColumnModel().getColumn( 1 ).setCellRenderer( new MyButtonRenderer() );
		table.getColumnModel().getColumn( 1 ).setMaxWidth( 32 );
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
	}

	public void setElements( final Elements<T> elements )
	{
		this.elements = elements;
		tableModel.fireTableDataChanged();
	}

	public JTable getTable()
	{
		return table;
	}

	private void update()
	{
	}

	private void notifyListeners()
	{
		listeners.forEach( UpdateListener::modelUpdated );
	}

	public synchronized boolean addUpdateListener( final UpdateListener l )
	{
		if ( !listeners.contains( l ) )
		{
			listeners.add( l );
			return true;
		}
		return false;
	}

	public synchronized boolean removeUpdateListener( final UpdateListener l )
	{
		return listeners.remove( l );
	}

	private void addAndEditRow()
	{
		int row = elements.size();
		elements.addElement();
		tableModel.fireTableRowsInserted( row, row );
		table.setRowSelectionInterval( row, row );
		table.editCellAt( row, 0 );
	}

	private void removeSelectedRow()
	{
		int row = table.getSelectedRow();
		if ( row >= 0 && row < elements.size() )
		{
			elements.remove( elements.get( row ) );
			notifyListeners();
			tableModel.fireTableRowsDeleted( row, row );
			int s = Math.max( row - 1, 0 );
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

	private class MyTableModel extends AbstractTableModel
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
			return 2;
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
			boolean paintSelected = isSelected && !table.isEditing();
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
