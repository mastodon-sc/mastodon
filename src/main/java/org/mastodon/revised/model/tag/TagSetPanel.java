package org.mastodon.revised.model.tag;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.DefaultCellEditor;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import org.mastodon.revised.model.tag.TagSetStructure.Tag;
import org.mastodon.revised.model.tag.TagSetStructure.TagSet;

public class TagSetPanel extends JPanel
{
	private static final long serialVersionUID = 1L;

	private static final ImageIcon ADD_ICON = new ImageIcon( TagSetPanel.class.getResource( "add.png" ) );

	private static final ImageIcon REMOVE_ICON = new ImageIcon( TagSetPanel.class.getResource( "delete.png" ) );

	private static final ImageIcon SMALL_ADD_ICON = new ImageIcon( TagSetPanel.class.getResource( "bullet_green.png" ) );

	private static final ImageIcon SMALL_REMOVE_ICON = new ImageIcon( TagSetPanel.class.getResource( "bullet_delete.png" ) );

	private final JScrollPane scrollPane;

	private final JColorChooser colorChooser;

	private final ArrayList< UpdateListener > listeners;

	private final JTable tableTagSet;

	private MyTableModel tableModel;

	private final TagSetStructure tss;

	private final Iterator< String > labelGenerator = new Iterator< String >()
	{

		private final AtomicInteger lbl = new AtomicInteger( 0 );

		@Override
		public String next()
		{
			return "label " + lbl.getAndIncrement();
		}

		@Override
		public boolean hasNext()
		{
			return true;
		}
	};

	private final Iterator< Color > colorGenerator = new Iterator< Color >()
	{
		private final Random ran = new Random();

		@Override
		public boolean hasNext()
		{
			return true;
		}

		@Override
		public Color next()
		{
			return new Color( ran.nextInt() );
		}
	};

	public TagSetPanel( final TagSetStructure tss )
	{
		super( new BorderLayout( 0, 0 ) );
		this.tss = tss;
		this.listeners = new ArrayList<>();
		colorChooser = new JColorChooser();

		scrollPane = new JScrollPane();
		scrollPane.setVerticalScrollBarPolicy( ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS );

		final JSplitPane splitPane = new JSplitPane();
		splitPane.setResizeWeight( 0.5 );
		add( splitPane, BorderLayout.CENTER );

		final JPanel panelTagSetEditor = new JPanel();
		splitPane.setLeftComponent( panelTagSetEditor );
		panelTagSetEditor.setLayout( new BorderLayout( 0, 0 ) );

		final JPanel panelTagSetButtons = new JPanel();
		final FlowLayout flowLayout = ( FlowLayout ) panelTagSetButtons.getLayout();
		flowLayout.setAlignment( FlowLayout.TRAILING );
		panelTagSetEditor.add( panelTagSetButtons, BorderLayout.SOUTH );

		final JButton btnNewTagSet = new JButton( ADD_ICON );
		btnNewTagSet.setBorderPainted( false );
		panelTagSetButtons.add( btnNewTagSet );

		final JPanel panelTagSetList = new JPanel();
		panelTagSetEditor.add( panelTagSetList, BorderLayout.CENTER );
		panelTagSetList.setLayout( new BorderLayout( 0, 0 ) );

		final JScrollPane scrollPaneTagSet = new JScrollPane();
		scrollPaneTagSet.setVerticalScrollBarPolicy( ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS );
		panelTagSetList.add( scrollPaneTagSet );

		this.tableModel = new MyTableModel();
		this.tableTagSet = new JTable( tableModel );
		tableTagSet.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
		tableTagSet.setTableHeader( null );
		tableTagSet.setFillsViewportHeight( true );
		tableTagSet.setAutoResizeMode( JTable.AUTO_RESIZE_ALL_COLUMNS );
		tableTagSet.setRowHeight( 30 );
		tableTagSet.getSelectionModel().addListSelectionListener( e -> {
			if ( e.getValueIsAdjusting() )
				return;
			update();
		} );
		tableTagSet.getColumnModel().getColumn( 0 ).setCellRenderer( new MyTagSetRenderer() );
		tableTagSet.getColumnModel().getColumn( 0 ).setCellEditor( new MyTagSetNameEditor() );
		tableTagSet.getColumnModel().getColumn( 1 ).setCellRenderer( new MyButtonRenderer() );
		tableTagSet.getColumnModel().getColumn( 1 ).setMaxWidth( 32 );
		tableTagSet.addMouseListener( new MyTableButtonMouseListener() );
		tableTagSet.setShowGrid( false );
		scrollPaneTagSet.setViewportView( tableTagSet );

		final JPanel panelLabelEditor = new JPanel();
		splitPane.setRightComponent( panelLabelEditor );
		panelLabelEditor.setLayout( new BorderLayout( 0, 0 ) );
		panelLabelEditor.add( scrollPane, BorderLayout.CENTER );

		btnNewTagSet.addActionListener( e -> tableModel.addTagSet() );

		if ( !tss.getTagSets().isEmpty() )
			tableTagSet.setRowSelectionInterval( 0, 0 );
	}

	private void update()
	{
		final JPanel panelTags = new JPanel();
		scrollPane.setViewportView( panelTags );
		final GridBagLayout layout = new GridBagLayout();
		panelTags.setLayout( layout );

		final GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets( 0, 5, 0, 5 );
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.PAGE_START;
		c.gridy = 0;

		final int selectedRow = tableTagSet.getSelectedRow();
		if ( selectedRow < 0 )
			return;
		final TagSet tagset = tss.getTagSets().get( selectedRow );

		final Collection< Tag > tags = tagset.getTags();
		int index = 1;
		if ( !tags.isEmpty() )
			for ( final Tag tag : tags )
			{
				c.anchor = GridBagConstraints.CENTER;
				c.gridx = 0;
				c.weightx = 0.;
				final JLabel lblIndex = new JLabel( "" + index++ + "." );
				panelTags.add( lblIndex, c );

				c.anchor = GridBagConstraints.PAGE_START;
				c.gridx++;
				c.weightx = 1.;
				final JTextField txtLabel = new JTextField( tag.label() );
				txtLabel.addActionListener( new ActionListener()
				{
					@Override
					public void actionPerformed( final ActionEvent e )
					{
						final String nlbl = txtLabel.getText();
						if ( !tag.label().equals( nlbl ) )
						{
							tag.setLabel( nlbl );
							notifyListeners();
							update();
						}
					}
				} );
				txtLabel.addFocusListener( new FocusListener()
				{

					@Override
					public void focusLost( final FocusEvent e )
					{
						final String nlbl = txtLabel.getText();
						if ( !tag.label().equals( nlbl ) )
						{
							tag.setLabel( nlbl );
							notifyListeners();
							update();
						}
					}

					@Override
					public void focusGained( final FocusEvent e )
					{
						txtLabel.selectAll();
					}
				} );
				panelTags.add( txtLabel, c );
				txtLabel.requestFocusInWindow();

				c.gridx++;
				c.weightx = 0.;
				final ColorSetter colorSetter = new ColorSetter()
				{
					@Override
					public Color getColor()
					{
						return tag.color();
					}

					@Override
					public void setColor( final Color color )
					{
						tag.setColor( color );
						notifyListeners();
					}
				};
				final JButton btnColor = new JButton( new ColorIcon( colorSetter.getColor() ) );
				btnColor.setContentAreaFilled( false );
				btnColor.setBorderPainted( false );
				btnColor.addActionListener( new ActionListener()
				{
					@Override
					public void actionPerformed( final ActionEvent e )
					{
						colorChooser.setColor( colorSetter.getColor() );
						final JDialog d = JColorChooser.createDialog( btnColor, "Choose a color", true, colorChooser, new ActionListener()
						{
							@Override
							public void actionPerformed( final ActionEvent arg0 )
							{
								final Color c = colorChooser.getColor();
								if ( c != null )
								{
									btnColor.setIcon( new ColorIcon( c ) );
									colorSetter.setColor( c );
								}
							}
						}, null );
						d.setVisible( true );
					}
				} );
				panelTags.add( btnColor, c );

				final JButton btnRemove = new JButton( SMALL_REMOVE_ICON );
				btnRemove.setSize( 25, 25 );
				btnRemove.setContentAreaFilled( false );
				btnRemove.setBorderPainted( false );
				btnRemove.addActionListener( new ActionListener()
				{
					@Override
					public void actionPerformed( final ActionEvent e )
					{
						tagset.removeTag( tag );
						notifyListeners();
						update();
					}
				} );
				c.gridx++;
				panelTags.add( btnRemove, c );

				c.gridy++;
			}

		c.gridx = 1;
		c.weightx = 1.;
		panelTags.add( new JLabel(), c );

		final JButton btnAdd = new JButton( SMALL_ADD_ICON );
		btnAdd.setSize( 25, 25 );
		btnAdd.setContentAreaFilled( false );
		btnAdd.setBorderPainted( false );
		btnAdd.addActionListener( new ActionListener()
		{

			@Override
			public void actionPerformed( final ActionEvent e )
			{
				final String name = labelGenerator.next();
				final Color color = colorGenerator.next();
				tagset.createTag( name, color );
				notifyListeners();
				update();
			}

		} );
		c.gridx = 3;
		c.weighty = 1;
		c.weightx = 0.;
		panelTags.add( btnAdd, c );
	}

	private void notifyListeners()
	{
		for ( final UpdateListener l : listeners )
			l.tagSetCollectionUpdated();
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

	private final String makeNewName( final String name )
	{
		final Pattern pattern = Pattern.compile( "(.+) \\((\\d+)\\)$" );
		final Matcher matcher = pattern.matcher( name );
		int n;
		String prefix;
		if ( matcher.matches() )
		{
			final String nstr = matcher.group( 2 );
			n = Integer.parseInt( nstr );
			prefix = matcher.group( 1 );
		}
		else
		{
			n = 1;
			prefix = name;
		}
		String newName;
		INCREMENT: while ( true )
		{
			newName = prefix + " (" + ( ++n ) + ")";
			for ( int j = 0; j < tss.getTagSets().size(); j++ )
			{
				if ( tss.getTagSets().get( j ).getName().equals( newName ) )
					continue INCREMENT;
			}
			break;
		}
		return newName;
	}

	/**
	 * Adapted from http://stackoverflow.com/a/3072979/230513
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

	private static abstract class ColorSetter
	{
		public abstract Color getColor();

		public abstract void setColor( Color c );
	}

	public static interface UpdateListener
	{
		public void tagSetCollectionUpdated();
	}

	private class MyTableModel extends AbstractTableModel
	{

		private static final long serialVersionUID = 1L;

		private final Map< TagSet, JButton > removeTagSetButtons;

		public MyTableModel()
		{
			this.removeTagSetButtons = new HashMap<>();
			tss.getTagSets().forEach( key -> removeTagSetButtons.put( key, makeRemoveButton( key ) ) );
		}

		private JButton makeRemoveButton( final TagSet tagSet )
		{
			final JButton removeButton = new JButton( REMOVE_ICON );
			removeButton.addActionListener( e -> {
				final int row = tss.getTagSets().indexOf( tagSet );
				tss.remove( tagSet );
				notifyListeners();
				fireTableRowsDeleted( row, row );
				update();
			} );
			removeButton.setBorderPainted( false );
			return removeButton;
		}

		@Override
		public boolean isCellEditable( final int rowIndex, final int columnIndex )
		{
			return columnIndex != 1;
		}

		@Override
		public int getColumnCount()
		{
			return 2;
		}

		@Override
		public int getRowCount()
		{
			return tss.getTagSets().size();
		}

		@Override
		public Object getValueAt( final int rowIndex, final int columnIndex )
		{
			if ( rowIndex >= getRowCount() )
				return null;

			switch ( columnIndex )
			{
			case 0:
				return tss.getTagSets().get( rowIndex );
			case 1:
				return removeTagSetButtons.get( tss.getTagSets().get( rowIndex ) );

			default:
				throw new IllegalArgumentException();
			}
		}

		private void addTagSet()
		{
			final int selectedRow = tableTagSet.getSelectedRow();
			final String name;
			if ( selectedRow < 0 )
			{
				name = makeNewName( "Tag set" );
			}
			else
			{
				final TagSet current = tss.getTagSets().get( selectedRow );
				name = makeNewName( current.getName() );
			}

			final TagSet tagSet = tss.createTagSet( name );
			removeTagSetButtons.put( tagSet, makeRemoveButton( tagSet ) );

			final int newIndex = tss.getTagSets().indexOf( tagSet );
			fireTableRowsInserted( newIndex, newIndex );
			tableTagSet.setRowSelectionInterval( newIndex, newIndex );
			tableTagSet.scrollRectToVisible( new Rectangle( tableTagSet.getCellRect( newIndex, 0, true ) ) );
			update();
			notifyListeners();
		}
	}

	private class MyTagSetRenderer extends DefaultTableCellRenderer
	{

		private static final long serialVersionUID = 1L;

		@Override
		public Component getTableCellRendererComponent( final JTable table, final Object value, final boolean isSelected, final boolean hasFocus, final int row, final int column )
		{
			super.getTableCellRendererComponent( table, value, isSelected, hasFocus, row, column );
			setText( ( ( TagSet ) value ).getName() );
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
			setIcon( REMOVE_ICON );
		}

		@Override
		public Component getTableCellRendererComponent( final JTable table, final Object value,
				final boolean isSelected, final boolean hasFocus, final int row, final int column )
		{
			setForeground( isSelected ? tableTagSet.getSelectionForeground() : tableTagSet.getForeground() );
			setBackground( isSelected ? tableTagSet.getSelectionBackground() : tableTagSet.getBackground() );
			return this;
		}
	}

	private class MyTableButtonMouseListener extends MouseAdapter
	{

		@Override
		public void mouseClicked( final MouseEvent e )
		{
			final int column = tableTagSet.getColumnModel().getColumnIndexAtX( e.getX() );
			final int row = e.getY() / tableTagSet.getRowHeight();

			if ( row < tableTagSet.getRowCount() && row >= 0 && column < tableTagSet.getColumnCount() && column >= 0 )
			{
				final Object value = tableTagSet.getValueAt( row, column );
				if ( value instanceof JButton )
					( ( JButton ) value ).doClick();
			}
		}
	}

	private class MyTagSetNameEditor extends DefaultCellEditor implements TableCellEditor
	{

		private static final long serialVersionUID = 1L;

		private TagSet edited;

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
			edited = ( TagSet ) value;
			editor.setText( ( ( TagSet ) value ).getName() );
			return editor;
		}
	}
}
