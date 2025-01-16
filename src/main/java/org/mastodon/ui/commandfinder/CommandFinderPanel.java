/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2025 Tobias Pietzsch, Jean-Yves Tinevez
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package org.mastodon.ui.commandfinder;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableRowSorter;

import org.apache.commons.lang3.StringUtils;
import org.scijava.listeners.Listeners;
import org.scijava.ui.behaviour.InputTrigger;
import org.scijava.ui.behaviour.io.InputTriggerConfig;
import org.scijava.ui.behaviour.io.gui.Command;
import org.scijava.ui.behaviour.io.gui.TagPanelEditor;
import org.scijava.ui.behaviour.io.gui.VisualEditorPanel.ConfigChangeListener;
import org.scijava.ui.behaviour.util.RunnableAction;

public class CommandFinderPanel extends JPanel
{

	private static final long serialVersionUID = 1L;

	private final Consumer< String > runAction;

	private final InputTriggerConfig config;

	private final Map< Command, String > actionDescriptions;

	private final Set< Command > commands;

	private final Listeners.List< ConfigChangeListener > modelChangedListeners;

	final JTextField textFieldFilter;

	private final JPanel panelEditor;

	private final JLabel labelCommandName;

	private final JTextArea textAreaDescription;

	private final JButton btnApply;

	private final JTable tableBindings;

	private TableRowSorter< MyTableModel > tableRowSorter;

	private MyTableModel tableModel;

	private JLabel keybindingEditor;

	public CommandFinderPanel(
			final Consumer< String > runAction,
			final Map< Command, String > commandMap,
			final InputTriggerConfig config
			)
	{
		this.runAction = runAction;
		this.config = config;
		this.actionDescriptions = new HashMap<>( commandMap );
		this.commands = new HashSet<>( actionDescriptions.keySet() );
		this.modelChangedListeners = new Listeners.SynchronizedList<>();

		/*
		 * GUI
		 */

		setLayout( new BorderLayout( 0, 0 ) );

		final JPanel panelFilter = new JPanel();
		add( panelFilter, BorderLayout.NORTH );
		panelFilter.setLayout( new BoxLayout( panelFilter, BoxLayout.X_AXIS ) );

		final Component horizontalStrut = Box.createHorizontalStrut( 5 );
		panelFilter.add( horizontalStrut );

		final JLabel lblFilter = new JLabel( "Filter:" );
		lblFilter.setToolTipText( "Filter on command names. Accept regular expressions." );
		lblFilter.setAlignmentX( Component.CENTER_ALIGNMENT );
		panelFilter.add( lblFilter );

		final Component horizontalStrut_1 = Box.createHorizontalStrut( 5 );
		panelFilter.add( horizontalStrut_1 );

		textFieldFilter = new JTextField();
		panelFilter.add( textFieldFilter );
		textFieldFilter.setColumns( 10 );
		// Run action when the user presses enter.
		textFieldFilter.addActionListener( e -> runFromFilter() );
		// Filter when a key is pressed.
		textFieldFilter.getDocument().addDocumentListener( new DocumentListener()
		{

			@Override
			public void removeUpdate( final DocumentEvent e )
			{
				filterRows();
			}

			@Override
			public void insertUpdate( final DocumentEvent e )
			{
				filterRows();
			}

			@Override
			public void changedUpdate( final DocumentEvent e )
			{
				filterRows();
			}
		} );
		// Move to the list with UP and DOWN array key.
		textFieldFilter.addKeyListener( new KeyAdapter()
		{
			@Override
			public void keyPressed( final KeyEvent e )
			{
				final int keyCode = e.getKeyCode();
				if ( keyCode == KeyEvent.VK_UP )
					goToLastInList();
				else if ( keyCode == KeyEvent.VK_DOWN )
					goToFirstInList();
			}
		} );

		panelEditor = new JPanel();
		add( panelEditor, BorderLayout.SOUTH );
		panelEditor.setLayout( new BorderLayout( 0, 0 ) );

		final JPanel panelCommandEditor = new JPanel();
		panelEditor.add( panelCommandEditor, BorderLayout.CENTER );
		final GridBagLayout gbl_panelCommandEditor = new GridBagLayout();
		gbl_panelCommandEditor.rowHeights = new int[] { 0, 0, 0, 0, 60 };
		gbl_panelCommandEditor.columnWidths = new int[] { 30, 100 };
		gbl_panelCommandEditor.columnWeights = new double[] { 0.0, 1.0 };
		gbl_panelCommandEditor.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0 };
		panelCommandEditor.setLayout( gbl_panelCommandEditor );

		final JLabel lblName = new JLabel( "Name:" );
		final GridBagConstraints gbc_lblName = new GridBagConstraints();
		gbc_lblName.insets = new Insets( 5, 5, 5, 5 );
		gbc_lblName.anchor = GridBagConstraints.WEST;
		gbc_lblName.gridx = 0;
		gbc_lblName.gridy = 0;
		panelCommandEditor.add( lblName, gbc_lblName );

		this.labelCommandName = new JLabel();
		final GridBagConstraints gbc_labelActionName = new GridBagConstraints();
		gbc_labelActionName.anchor = GridBagConstraints.WEST;
		gbc_labelActionName.insets = new Insets( 5, 5, 5, 0 );
		gbc_labelActionName.gridx = 1;
		gbc_labelActionName.gridy = 0;
		panelCommandEditor.add( labelCommandName, gbc_labelActionName );

		final JLabel lblBinding = new JLabel( "Binding:" );
		final GridBagConstraints gbc_lblBinding = new GridBagConstraints();
		gbc_lblBinding.anchor = GridBagConstraints.WEST;
		gbc_lblBinding.insets = new Insets( 5, 5, 5, 5 );
		gbc_lblBinding.gridx = 0;
		gbc_lblBinding.gridy = 1;
		panelCommandEditor.add( lblBinding, gbc_lblBinding );

		this.keybindingEditor = new JLabel();
		final GridBagConstraints gbc_textFieldBinding = new GridBagConstraints();
		gbc_textFieldBinding.insets = new Insets( 5, 5, 5, 5 );
		gbc_textFieldBinding.fill = GridBagConstraints.HORIZONTAL;
		gbc_textFieldBinding.gridx = 1;
		gbc_textFieldBinding.gridy = 1;
		panelCommandEditor.add( keybindingEditor, gbc_textFieldBinding );

		final JLabel lblDescription = new JLabel( "Description:" );
		final GridBagConstraints gbc_lblDescription = new GridBagConstraints();
		gbc_lblDescription.insets = new Insets( 5, 5, 5, 5 );
		gbc_lblDescription.anchor = GridBagConstraints.NORTHWEST;
		gbc_lblDescription.gridx = 0;
		gbc_lblDescription.gridy = 4;
		panelCommandEditor.add( lblDescription, gbc_lblDescription );

		final JScrollPane scrollPaneDescription = new JScrollPane();
		scrollPaneDescription.setOpaque( false );
		scrollPaneDescription.setHorizontalScrollBarPolicy( ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER );
		final GridBagConstraints gbc_scrollPaneDescription = new GridBagConstraints();
		gbc_scrollPaneDescription.insets = new Insets( 5, 5, 5, 5 );
		gbc_scrollPaneDescription.fill = GridBagConstraints.BOTH;
		gbc_scrollPaneDescription.gridx = 1;
		gbc_scrollPaneDescription.gridy = 4;
		panelCommandEditor.add( scrollPaneDescription, gbc_scrollPaneDescription );

		textAreaDescription = new JTextArea();
		textAreaDescription.setRows( 3 );
		textAreaDescription.setFont( getFont().deriveFont( getFont().getSize2D() - 1f ) );
		textAreaDescription.setOpaque( false );
		textAreaDescription.setWrapStyleWord( true );
		textAreaDescription.setEditable( false );
		textAreaDescription.setLineWrap( true );
		textAreaDescription.setFocusable( false );
		scrollPaneDescription.setViewportView( textAreaDescription );

		final JPanel panelButtons = new JPanel();
		panelEditor.add( panelButtons, BorderLayout.SOUTH );
		final FlowLayout flowLayout = ( FlowLayout ) panelButtons.getLayout();
		flowLayout.setAlignment( FlowLayout.TRAILING );

		this.btnApply = new JButton( "Run" );
		btnApply.setToolTipText( "Run the selected command." );
		panelButtons.add( btnApply );

		final JScrollPane scrollPane = new JScrollPane();
		scrollPane.setVerticalScrollBarPolicy( ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS );
		scrollPane.setHorizontalScrollBarPolicy( ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER );
		add( scrollPane, BorderLayout.CENTER );

		tableBindings = new JTable()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void updateUI()
			{
				super.updateUI();
				setRowHeight( ( int ) ( getFontMetrics( getFont() ).getHeight() * 1.5 ) );
			}
		};
		tableBindings.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
		tableBindings.setFillsViewportHeight( true );
		tableBindings.setAutoResizeMode( JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS );
		tableBindings.getSelectionModel().addListSelectionListener( new ListSelectionListener()
		{
			@Override
			public void valueChanged( final ListSelectionEvent e )
			{
				if ( e.getValueIsAdjusting() )
					return;
				updateEditors();
			}
		} );
		tableBindings.setFocusTraversalKeys( KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, null );
		tableBindings.setFocusTraversalKeys( KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, null );

		// Run when the user press enter and a row is selected
		final KeyStroke enter = KeyStroke.getKeyStroke( KeyEvent.VK_ENTER, 0 );
		final String solve = "Run command";
		tableBindings.getInputMap( JTable.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT ).put( enter, solve );
		tableBindings.getActionMap().put( solve, new RunnableAction( solve, this::runSelection ) );

		// Run when the user double-clicks on a row
		tableBindings.addMouseListener( new MouseAdapter()
		{
			@Override
			public void mouseClicked( final MouseEvent e )
			{
				if ( e.getClickCount() == 2 )
					runSelection();
			}
		} );

		// Run when the user presses the button
		btnApply.addActionListener( ( e ) -> runSelection() );

		configToModel();
		tableBindings.getRowSorter().toggleSortOrder( 0 );
		if ( tableBindings.getRowCount() > 0 )
			tableBindings.getSelectionModel().setSelectionInterval( 0, 0 );

		scrollPane.setViewportView( tableBindings );
	}

	private void goToFirstInList()
	{
		selectAndShowViewRow( 0 );
	}

	private void goToLastInList()
	{
		final int nFiltered = tableBindings.getRowSorter().getViewRowCount();
		selectAndShowViewRow( nFiltered - 1 );
	}

	private void selectAndShowViewRow( final int row )
	{
		tableBindings.getSelectionModel().setSelectionInterval( row, row );
		tableBindings.requestFocusInWindow();
		tableBindings.scrollRectToVisible( tableBindings.getCellRect( row, row, true ) );
	}

	private void runFromFilter()
	{
		final int nFiltered = tableBindings.getRowSorter().getViewRowCount();
		if ( nFiltered != 1 )
			return;

		runRow( 0 );
	}

	private void runSelection()
	{
		final int viewRow = tableBindings.getSelectedRow();
		if ( viewRow < 0 )
		{
			SwingUtilities.getWindowAncestor( this ).setVisible( false );
			return;
		}
		runRow( viewRow );
	}

	private void runRow( final int viewRow )
	{
		final int modelRow = tableBindings.convertRowIndexToModel( viewRow );
		final MyTableRow row = tableModel.rows.get( modelRow );
		final String actionName = row.getName();

		runAction.accept( actionName );
		SwingUtilities.getWindowAncestor( this ).setVisible( false );
	}

	private void updateEditors()
	{
		final int viewRow = tableBindings.getSelectedRow();
		if ( viewRow < 0 )
		{
			labelCommandName.setText( "" );
			textAreaDescription.setText( "" );
			keybindingEditor.setText( InputTrigger.NOT_MAPPED.toString() );
			return;
		}

		final int modelRow = tableBindings.convertRowIndexToModel( viewRow );
		final MyTableRow row = tableModel.rows.get( modelRow );
		final String action = row.getName();
		final List< InputTrigger > triggers = row.getTrigger();
		final List< String > contexts = row.getContexts();
		final String description = actionDescriptions.get( new Command( action, contexts.get( 0 ) ) );
		labelCommandName.setText( action );
		keybindingEditor.setText( StringUtils.join( triggers, ", " ) );
		textAreaDescription.setText( description );
		textAreaDescription.setCaretPosition( 0 );
	}

	public void setCommandDescriptions( final Map< Command, String > cds )
	{
		actionDescriptions.clear();
		commands.clear();
		actionDescriptions.putAll( cds );
		commands.addAll( actionDescriptions.keySet() );
		configToModel();
	}

	private void configToModel()
	{
		tableModel = new MyTableModel( commands, config );
		tableBindings.setModel( tableModel );

		tableRowSorter = new TableRowSorter<>( tableModel );
		tableRowSorter.setComparator( 1, InputTriggersComparator );
		tableBindings.setRowSorter( tableRowSorter );
		filterRows();

		// Renderers.
		tableBindings.getColumnModel().getColumn( 1 ).setCellRenderer( new MyBindingsRenderer() );
		tableBindings.getColumnModel().getColumn( 2 ).setCellRenderer( new MyContextsRenderer( Collections.emptyList() ) );

		// Notify listeners.
		notifyListeners();
	}

	private void notifyListeners()
	{
		modelChangedListeners.list.forEach( ConfigChangeListener::configChanged );
	}

	private void filterRows()
	{
		final String regex = textFieldFilter.getText();
		final Pattern pattern = Pattern.compile( regex, Pattern.CASE_INSENSITIVE );
		final Matcher matcher = pattern.matcher( "" );
		final RowFilter< MyTableModel, Integer > rf = new RowFilter< MyTableModel, Integer >()
		{

			@Override
			public boolean include( final Entry< ? extends MyTableModel, ? extends Integer > entry )
			{
				int count = entry.getValueCount();
				while ( --count >= 0 )
				{
					matcher.reset( entry.getStringValue( count ) );
					if ( matcher.find() )
					{ return true; }
				}
				return false;
			}
		};
		tableRowSorter.setRowFilter( rf );
	}

	private static class MyTableModel extends AbstractTableModel
	{

		private static final long serialVersionUID = 1L;

		private static final String[] TABLE_HEADERS = new String[] { "Command", "Binding", "Contexts" };

		private final List< MyTableRow > rows;

		private final Set< Command > allCommands;

		public MyTableModel( final Set< Command > commands, final InputTriggerConfig config )
		{
			rows = new ArrayList<>();
			allCommands = commands;
			for ( final Command command : commands )
			{
				final Set< InputTrigger > inputs = config.getInputs( command.getName(), command.getContext() );
				for ( final InputTrigger input : inputs )
					rows.add( new MyTableRow( command.getName(), input, command.getContext() ) );
			}
			addMissingRows();
		}

		/**
		 * In the given list of {@code rows}, find and merge rows with the same
		 * action name and trigger, but different contexts.
		 *
		 * @param rows
		 *            list of rows to modify.
		 */
		private void mergeRows( final List< MyTableRow > rows )
		{
			final List< MyTableRow > rowsUnmerged = new ArrayList<>( rows );
			rows.clear();

			rowsUnmerged.sort( MyTableRowComparator );

			for ( int i = 0; i < rowsUnmerged.size(); )
			{
				final MyTableRow rowA = rowsUnmerged.get( i );
				int j = i + 1;
				while ( j < rowsUnmerged.size() && MyTableRowComparator.compare( rowsUnmerged.get( j ), rowA ) == 0 )
					++j;

				// Merge contexts.
				final Set< String > contexts = new HashSet<>();
				for ( int k = i; k < j; ++k )
					contexts.addAll( rowsUnmerged.get( k ).getContexts() );
				final List< String > contextsList = new ArrayList<>( contexts );
				contextsList.sort( null );

				// Merge triggers.
				final Set< InputTrigger > triggers = new HashSet<>();
				for ( int k = i; k < j; ++k )
					triggers.addAll( rowsUnmerged.get( k ).getTrigger() );
				final List< InputTrigger > triggersList = new ArrayList<>( triggers );
				triggersList.sort( InputTriggerComparator );

				rows.add( new MyTableRow( rowA.getName(), triggersList, contextsList ) );

				i = j;
			}
		}

		/**
		 * Add {@code NOT_MAPPED} rows for (name, context) pairs in
		 * {@link #allCommands} that are not otherwise covered. Then
		 * {@link #mergeRows()}.
		 *
		 * If any changes are made, {@code fireTableDataChanged} is fired.
		 *
		 * @return true, if changes were made.
		 */
		private boolean addMissingRows()
		{
			final ArrayList< MyTableRow > copy = new ArrayList<>( rows );
			addMissingRows( rows );
			if ( !copy.equals( rows ) )
			{
				this.fireTableDataChanged();
				return true;
			}
			return false;
		}

		/**
		 * In the given list of {@code rows}, add {@code NOT_MAPPED} rows for
		 * (name, context) pairs in {@link #allCommands} that are not otherwise
		 * covered. Then {@link #mergeRows(List)}.
		 *
		 * @param rows
		 *            list of rows to modify.
		 */
		private void addMissingRows( final List< MyTableRow > rows )
		{
			final ArrayList< Command > missingCommands = new ArrayList<>();
			for ( final Command command : allCommands )
			{
				boolean found = false;
				for ( final MyTableRow row : rows )
				{
					if ( row.getName().equals( command.getName() ) && row.getContexts().contains( command.getContext() ) )
					{
						found = true;
						break;
					}
				}
				if ( !found )
					missingCommands.add( command );
			}

			for ( final Command command : missingCommands )
				rows.add( new MyTableRow( command.getName(), InputTrigger.NOT_MAPPED, command.getContext() ) );

			mergeRows( rows );
		}

		@Override
		public int getRowCount()
		{
			return rows.size();
		}

		@Override
		public int getColumnCount()
		{
			return 3;
		}

		@Override
		public Object getValueAt( final int rowIndex, final int columnIndex )
		{
			switch ( columnIndex )
			{
			case 0:
				return rows.get( rowIndex ).getName();
			case 1:
				return rows.get( rowIndex ).getTrigger();
			case 2:
				return rows.get( rowIndex ).getContexts();
			default:
				throw new NoSuchElementException( "Cannot access column " + columnIndex + " in this model." );
			}
		}

		@Override
		public String getColumnName( final int column )
		{
			return TABLE_HEADERS[ column ];
		}
	}

	private static class MyTableRow
	{
		private final String name;

		private final List< InputTrigger > triggers;

		private final List< String > contexts;

		public MyTableRow( final String name, final InputTrigger trigger, final String context )
		{
			this( name, Collections.singletonList( trigger ), Collections.singletonList( context ) );
		}

		public MyTableRow( final String name, final List< InputTrigger > triggers, final Collection< String > contexts )
		{
			this.name = name;
			this.triggers = triggers;
			this.contexts = new ArrayList<>( contexts );
		}

		public String getName()
		{
			return name;
		}

		public List< InputTrigger > getTrigger()
		{
			return triggers;
		}

		public List< String > getContexts()
		{
			return contexts;
		}

		@Override
		public boolean equals( final Object o )
		{
			if ( this == o )
				return true;
			if ( o == null || getClass() != o.getClass() )
				return false;

			final MyTableRow that = ( MyTableRow ) o;

			if ( !name.equals( that.name ) )
				return false;
			if ( !triggers.equals( that.triggers ) )
				return false;
			return contexts.equals( that.contexts );
		}

		@Override
		public int hashCode()
		{
			int result = name.hashCode();
			result = 31 * result + triggers.hashCode();
			result = 31 * result + contexts.hashCode();
			return result;
		}

		@Override
		public String toString()
		{
			return "MyTableRow{" +
					"name='" + name + '\'' +
					", trigger=" + triggers +
					", contexts=" + contexts +
					'}';
		}
	}

	private static final Comparator< MyTableRow > MyTableRowComparator = new Comparator< MyTableRow >()
	{
		@Override
		public int compare( final MyTableRow o1, final MyTableRow o2 )
		{
			final int cn = o1.name.compareTo( o2.name );
			if ( cn != 0 )
				return cn;

			return 0;
		}
	};

	private static final Comparator< InputTrigger > InputTriggerComparator = new Comparator< InputTrigger >()
	{
		@Override
		public int compare( final InputTrigger o1, final InputTrigger o2 )
		{
			if ( o1 == InputTrigger.NOT_MAPPED )
				return 1;
			if ( o2 == InputTrigger.NOT_MAPPED )
				return -1;
			return o1.toString().compareTo( o2.toString() );
		}
	};

	private static final Comparator< List< InputTrigger > > InputTriggersComparator = new Comparator< List< InputTrigger > >()
	{
		@Override
		public int compare( final List< InputTrigger > o1, final List< InputTrigger > o2 )
		{
			if ( o1.equals( Collections.singletonList( InputTrigger.NOT_MAPPED ) ) )
				return 1;
			if ( o2.equals( Collections.singletonList( InputTrigger.NOT_MAPPED ) ) )
				return -1;

			return StringUtils.join( o1, ", " ).compareTo( StringUtils.join( o2, ", " ) );
		}
	};

	private static final class MyBindingsRenderer extends MultiInputTriggerPanelEditor implements TableCellRenderer
	{

		private static final long serialVersionUID = 1L;

		public MyBindingsRenderer()
		{
			super();
		}

		@Override
		public void updateUI()
		{
			super.updateUI();
			setBorder( new EmptyBorder( 0, 0, 0, 0 ) );
		}

		@Override
		public Component getTableCellRendererComponent( final JTable table, final Object value, final boolean isSelected, final boolean hasFocus, final int row, final int column )
		{
			setForeground( isSelected ? table.getSelectionForeground() : table.getForeground() );
			setBackground( isSelected ? table.getSelectionBackground() : table.getBackground() );

			@SuppressWarnings( "unchecked" )
			final List< InputTrigger > inputs = ( List< InputTrigger > ) value;
			if ( null != inputs )
			{
				setInputTriggers( inputs );
				final String val = StringUtils.join( inputs, ", " );
				setToolTipText( val );
			}
			else
			{
				setInputTriggers( Collections.singletonList( InputTrigger.NOT_MAPPED ) );
				setToolTipText( "No binding" );
			}
			return this;
		}
	}

	private final class MyContextsRenderer extends TagPanelEditor implements TableCellRenderer
	{

		private static final long serialVersionUID = 1L;

		public MyContextsRenderer( final Collection< String > tags )
		{
			super( tags, false );
		}

		@Override
		public void updateUI()
		{
			super.updateUI();
			setBorder( new EmptyBorder( 0, 0, 0, 0 ) );
		}

		@Override
		public Component getTableCellRendererComponent( final JTable table, final Object value, final boolean isSelected, final boolean hasFocus, final int row, final int column )
		{
			setForeground( isSelected ? table.getSelectionForeground() : table.getForeground() );
			setBackground( isSelected ? table.getSelectionBackground() : table.getBackground() );

			@SuppressWarnings( "unchecked" )
			final List< String > contexts = value != null
					? ( List< String > ) value
					: Collections.emptyList();
			if ( contexts.isEmpty() )
				setBackground( Color.PINK );
			setAcceptableTags( contexts );
			setTags( contexts );
			setToolTipText( contexts.toString() );
			return this;
		}
	}
}
