package org.mastodon.revised.trackscheme.display;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

import org.mastodon.graph.GraphChangeListener;
import org.mastodon.graph.algorithm.traversal.BreadthFirstIterator;
import org.mastodon.revised.trackscheme.LexicographicalVertexOrder;
import org.mastodon.revised.trackscheme.TrackSchemeGraph;
import org.mastodon.revised.trackscheme.TrackSchemeNavigation;
import org.mastodon.revised.trackscheme.TrackSchemeVertex;
import org.scijava.ui.behaviour.util.RunnableAction;

import com.itextpdf.text.Font;

public class TrackSchemeSearchPanel extends JPanel
{
	private static final long serialVersionUID = 1L;

	private static final String UNFOCUSED_TEXT = "Search...";

	private static final ImageIcon FOCUSED_ICON = new ImageIcon( TrackSchemeSearchPanel.class.getResource( "find-24x24-orange.png" ) );

	private static final ImageIcon UNFOCUSED_ICON = new ImageIcon( TrackSchemeSearchPanel.class.getResource( "find-24x24.png" ) );

	private static final ImageIcon FOUND_ICON = new ImageIcon( TrackSchemeSearchPanel.class.getResource( "find-24x24-green.png" ) );

	private static final ImageIcon NOT_FOUND_ICON = new ImageIcon( TrackSchemeSearchPanel.class.getResource( "find-24x24-red.png" ) );

	private final AtomicBoolean doChangeFocusIcon;

	private final JTextField searchField;

	private final JButton labelIcon;

	public TrackSchemeSearchPanel( final TrackSchemeGraph< ?, ? > graph, final TrackSchemeNavigation navigation, final JComponent display )
	{
		/*
		 * GUI
		 */

		setMinimumSize( new Dimension( 26, 25 ) );
		setMaximumSize( new Dimension( 310, 25 ) );
		setPreferredSize( new Dimension( 220, 25 ) );
		final GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 25, 122, 50 };
		gridBagLayout.rowHeights = new int[] { 25 };
		gridBagLayout.columnWeights = new double[] { 0.0, 1.0, 0.0 };
		gridBagLayout.rowWeights = new double[] { 0.0 };
		setLayout( gridBagLayout );

		labelIcon = new JButton( UNFOCUSED_ICON );
		labelIcon.setBorder( new EmptyBorder( 0, 0, 0, 0 ) );
		final GridBagConstraints gbc_labelIcon = new GridBagConstraints();
		gbc_labelIcon.anchor = GridBagConstraints.WEST;
		gbc_labelIcon.gridx = 0;
		gbc_labelIcon.gridy = 0;
		add( labelIcon, gbc_labelIcon );
		labelIcon.addActionListener( ( event ) -> focus() );

		searchField = new JTextField();
		final GridBagConstraints gbc_searchField = new GridBagConstraints();
		gbc_searchField.fill = GridBagConstraints.HORIZONTAL;
		gbc_searchField.anchor = GridBagConstraints.EAST;
		gbc_searchField.gridx = 1;
		gbc_searchField.gridy = 0;
		add( searchField, gbc_searchField );
		searchField.setColumns( 10 );

		doChangeFocusIcon = new AtomicBoolean( true );
		final MyFocusListener mfl = new MyFocusListener();
		mfl.focusLost( null );
		searchField.addFocusListener( mfl );

		searchField.getInputMap( JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT ).put( KeyStroke.getKeyStroke( "ESCAPE" ), "abort search" );
		new RunnableAction( "abort search", display::requestFocusInWindow ).put( searchField.getActionMap() );

		final JCheckBox chckbxstartswith = new JCheckBox( "<html>starts<br>with</html>" );
		chckbxstartswith.setFont( chckbxstartswith.getFont().deriveFont( 8f ) );
		final GridBagConstraints gbc_chckbxstartswith = new GridBagConstraints();
		gbc_chckbxstartswith.gridx = 2;
		gbc_chckbxstartswith.gridy = 0;
		add( chckbxstartswith, gbc_chckbxstartswith );

		final SearchAction sa = new SearchAction( graph, navigation );
		graph.addGraphChangeListener( sa );
		searchField.addActionListener( new ActionListener()
		{
			@Override
			public void actionPerformed( final ActionEvent e )
			{
				doChangeFocusIcon.set( false );
				setEnabled( false );
				new Thread()
				{
					@Override
					public void run()
					{
						try
						{
							final boolean found = sa.search( searchField.getText(), chckbxstartswith.isSelected() );
							focus();
							setIcon( found ? FOUND_ICON : NOT_FOUND_ICON );
						}
						finally
						{
							setEnabled( true );
							doChangeFocusIcon.set( true );
						}
					};
				}.start();
			}
		} );
	}

	public void focus()
	{
		searchField.requestFocusInWindow();
	}

	private void setIcon( final Icon icon )
	{
		labelIcon.setIcon( icon );
		labelIcon.setPressedIcon( icon == UNFOCUSED_ICON ? FOCUSED_ICON : icon );
	}

	private static class SearchAction implements GraphChangeListener
	{
		private Iterator< TrackSchemeVertex > iterator;

		private Iterator< TrackSchemeVertex > rootIterator;

		private final TrackSchemeGraph< ?, ? > graph;

		private final TrackSchemeNavigation navigation;

		public SearchAction( final TrackSchemeGraph< ?, ? > graph, final TrackSchemeNavigation navigation )
		{
			this.graph = graph;
			this.navigation = navigation;
			reinit();
		}

		private synchronized boolean search( final String text, final boolean startsWith )
		{
			final TrackSchemeVertex start = graph.vertexRef();
			TrackSchemeVertex v = next();
			start.refTo( v );
			do
			{
				if ( startsWith ? v.getLabel().startsWith( text ) : v.getLabel().contains( text ) )
				{
					graph.releaseRef( start );
					navigation.notifyNavigateToVertex( v );
					return true;
				}

			}
			while ( !( v = next() ).equals( start ) );

			graph.releaseRef( start );
			return false;
		}

		private TrackSchemeVertex next()
		{
			if ( !iterator.hasNext() )
				reinit();

			return iterator.next();
		}

		@Override
		public void graphChanged()
		{
			reinit();
		}

		private synchronized void reinit()
		{
			if ( null == rootIterator || !rootIterator.hasNext() )
				rootIterator = LexicographicalVertexOrder.sort( graph, graph.getRoots() ).iterator();

			final TrackSchemeVertex root = rootIterator.next();
			iterator = new BreadthFirstIterator<>( root, graph );
		}
	}

	private class MyFocusListener implements FocusListener
	{
		private String oldText;

		private boolean keysBlocked = false;

		@Override
		public void focusLost( final FocusEvent evt )
		{
			if ( doChangeFocusIcon.get() )
			{
				setIcon( UNFOCUSED_ICON );
				oldText = searchField.getText();
				searchField.setText( UNFOCUSED_TEXT );
				searchField.setFont( searchField.getFont().deriveFont( Font.ITALIC ) );
				searchField.setForeground( UIManager.getColor( "textInactiveText" ) );
			}
		}

		@Override
		public void focusGained( final FocusEvent evt )
		{
			if ( doChangeFocusIcon.get() )
			{
				if ( !keysBlocked )
				{
					blockKeys( searchField );
					keysBlocked = true;
				}

				setIcon( FOCUSED_ICON );
				searchField.setText( oldText );
				searchField.setFont( searchField.getFont().deriveFont( Font.NORMAL ) );
				searchField.setForeground( UIManager.getColor( "Label.foreground" ) );
			}
		}
	}

	/**
	 * TODO move to utility classes.
	 * <p>
	 * Preserves the specified component from behavior and action related key
	 * presses. This useful <i>e.g.</i> for text fields; user input can be
	 * confused with behaviors key shortcuts.
	 * <p>
	 * Adapted from Jan Funke's code in <a href=
	 * "https://github.com/saalfeldlab/bigcat/blob/janh5/src/main/java/bdv/bigcat/ui/BigCatTable.java#L112-L143">
	 * BigCat repo</a>
	 *
	 * @param ctn
	 *            the JComponent to preserve from key presses.
	 */
	private static void blockKeys( final JComponent ctn )
	{
		// Get all keystrokes that are mapped to actions in higher components
		final ArrayList< KeyStroke > allTableKeys = new ArrayList<>();
		for ( Container c = ctn.getParent(); c != null; c = c.getParent() )
		{
			if ( c instanceof JComponent )
			{
				final InputMap inputMap = ( ( JComponent ) c ).getInputMap( JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT );
				final KeyStroke[] tableKeys = inputMap.allKeys();
				if ( tableKeys != null )
					allTableKeys.addAll( Arrays.asList( tableKeys ) );
			}
		}

		// An action that does nothing. We can not just map to "none",
		// as this is not interrupting the action-name -> action search.
		// We have to map to a proper action, "nothing" in this case.
		final Action nada = new AbstractAction()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed( final ActionEvent e )
			{}
		};
		ctn.getActionMap().put( "nothing", nada );

		// Replace every table key binding with nothing, thus creating an
		// event-barrier.
		final InputMap inputMap = ctn.getInputMap( JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT );
		for ( final KeyStroke key : allTableKeys )
			inputMap.put( key, "nothing" );

		ctn.getActionMap().put( "nothing", nada );
	}
}
