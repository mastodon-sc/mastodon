package org.mastodon.revised.model.tag;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.KeyboardFocusManager;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;
import javax.swing.DefaultCellEditor;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JColorChooser;
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

public class ColorTagTable< T extends ColorTagTable.Element > extends TagTable< T >
{
	public interface Element extends TagTable.Element
	{
		Color getColor();

		void setColor( Color color );
	}

	private final JColorChooser colorChooser;

	private final int colorColumn = 1;

	public ColorTagTable( final Elements< T > elements )
	{
		super( elements, 1 );
		colorChooser = new JColorChooser();

		table.getColumnModel().getColumn( colorColumn ).setCellRenderer( new MyColorButtonRenderer() );
		table.getColumnModel().getColumn( colorColumn ).setMaxWidth( 32 );
		table.addMouseListener( new MyTableColorButtonMouseListener() );
	}

	private void editSelectedRowColor()
	{
		final int row = table.getSelectedRow();
		if ( row >= 0 )
		{
			final T element = elements.get( row );
			colorChooser.setColor( element.getColor() );
			JColorChooser.createDialog( table, "Choose a color", true, colorChooser, ok -> {
				final Color c = colorChooser.getColor();
				if ( c != null )
				{
					element.setColor( c );
					notifyListeners();
					tableModel.fireTableRowsUpdated( row, row );
					update();
				}
			}, null ).setVisible( true );
		}
	}

	private class MyColorButtonRenderer extends JButton implements TableCellRenderer
	{
		private static final long serialVersionUID = 1L;

		public MyColorButtonRenderer()
		{
			setOpaque( true );
			setBorderPainted( false );
		}

		@Override
		public Component getTableCellRendererComponent( final JTable table, final Object value,
				final boolean isSelected, final boolean hasFocus, final int row, final int column )
		{
			if ( row == elements.size() )
				setIcon( null );
			else
				setIcon( new ColorIcon( elements.get( row ).getColor() ) );
			boolean paintSelected = isSelected && !table.isEditing();
			setForeground( paintSelected ? table.getSelectionForeground() : table.getForeground() );
			setBackground( paintSelected ? table.getSelectionBackground() : table.getBackground() );
			return this;
		}
	}

	private class MyTableColorButtonMouseListener extends MouseAdapter
	{
		@Override
		public void mouseClicked( final MouseEvent e )
		{
			final int column = table.getColumnModel().getColumnIndexAtX( e.getX() );
			if ( column == colorColumn )
			{
				if ( table.getSelectedRow() <= elements.size() )
					editSelectedRowColor();
			}
		}
	}


	/**
	 * Adapted from http://stackoverflow.com/a/3072979/230513
	 *
	 * TODO: unify and move the various ColorIcon inner classes to a common utility class
	 */
	private static class ColorIcon implements Icon
	{
		private final int size = 16;

		private final Color color;

		public ColorIcon( final Color color )
		{
			this.color = color;
		}

		@Override
		public void paintIcon( final Component c, final Graphics g, final int x, final int y )
		{
			final Graphics2D g2d = ( Graphics2D ) g;
			g2d.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
			g2d.setColor( color );
			g2d.fill( new RoundRectangle2D.Float( x, y, size, size, 5, 5 ) );
		}

		@Override
		public int getIconWidth()
		{
			return size;
		}

		@Override
		public int getIconHeight()
		{
			return size;
		}
	}
}
