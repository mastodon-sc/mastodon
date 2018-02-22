package org.mastodon.app.ui;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Collections;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

import org.mastodon.collection.RefCollections;
import org.mastodon.collection.RefList;
import org.mastodon.collection.RefSet;
import org.mastodon.graph.Edge;
import org.mastodon.graph.ReadOnlyGraph;
import org.mastodon.graph.Vertex;
import org.mastodon.graph.algorithm.AbstractGraphAlgorithm;
import org.mastodon.graph.algorithm.RootFinder;
import org.mastodon.graph.algorithm.traversal.BreadthFirstCrossComponentIterator;
import org.mastodon.graph.algorithm.traversal.InverseDepthFirstIterator;
import org.mastodon.model.FocusModel;
import org.mastodon.model.HasLabel;
import org.mastodon.model.NavigationHandler;
import org.mastodon.util.KeyConfigUtils;
import org.mastodon.views.trackscheme.util.AlphanumCompare;
import org.scijava.ui.behaviour.util.Actions;

public class SearchVertexLabel< V extends Vertex< E > & HasLabel, E extends Edge< V > >
{

	private static final String UNFOCUSED_TEXT = "Search...";

	private static final ImageIcon FOCUSED_ICON = new ImageIcon( SearchVertexLabel.class.getResource( "find-24x24-orange.png" ) );

	private static final ImageIcon UNFOCUSED_ICON = new ImageIcon( SearchVertexLabel.class.getResource( "find-24x24.png" ) );

	private static final ImageIcon FOUND_ICON = new ImageIcon( SearchVertexLabel.class.getResource( "find-24x24-green.png" ) );

	private static final ImageIcon NOT_FOUND_ICON = new ImageIcon( SearchVertexLabel.class.getResource( "find-24x24-red.png" ) );

	public static final String SEARCH = "search label";

	public static final String[] SEARCH_KEYS = new String[] { "ctrl F", "meta F", "SLASH" };

	private final JTextField searchField;

	private final JButton labelIcon;

	private final AtomicBoolean doChangeFocusIcon;

	private final JPanel searchPanel;

	public static < V extends Vertex< E > & HasLabel, E extends Edge< V > > JPanel install(
			final Actions actions,
			final ReadOnlyGraph< V, E > graph,
			final NavigationHandler< V, E > navigation,
			final FocusModel< V, E > focus )
	{
		final SearchVertexLabel< V, E > search = new SearchVertexLabel<>( graph, navigation, focus );
		actions.runnableAction( () -> search.searchField.requestFocusInWindow(), SEARCH, SEARCH_KEYS );
		return search.searchPanel;
	}

	private SearchVertexLabel(
			final ReadOnlyGraph< V, E > graph,
			final NavigationHandler< V, E > navigation,
			final FocusModel< V, E > focus )
	{
		searchPanel = new JPanel();
		searchPanel.setMinimumSize( new Dimension( 26, 25 ) );
		searchPanel.setMaximumSize( new Dimension( 310, 25 ) );
		searchPanel.setPreferredSize( new Dimension( 220, 25 ) );
		final GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 25, 122, 50 };
		gridBagLayout.rowHeights = new int[] { 25 };
		gridBagLayout.columnWeights = new double[] { 0.0, 1.0, 0.0 };
		gridBagLayout.rowWeights = new double[] { 0.0 };
		searchPanel.setLayout( gridBagLayout );

		labelIcon = new JButton( UNFOCUSED_ICON );
		labelIcon.setBorder( new EmptyBorder( 0, 0, 0, 0 ) );
		final GridBagConstraints gbc_labelIcon = new GridBagConstraints();
		gbc_labelIcon.anchor = GridBagConstraints.WEST;
		gbc_labelIcon.gridx = 0;
		gbc_labelIcon.gridy = 0;
		searchPanel.add( labelIcon, gbc_labelIcon );

		searchField = new JTextField();
		searchField.setBorder( null );
		final GridBagConstraints gbc_searchField = new GridBagConstraints();
		gbc_searchField.fill = GridBagConstraints.HORIZONTAL;
		gbc_searchField.anchor = GridBagConstraints.EAST;
		gbc_searchField.gridx = 1;
		gbc_searchField.gridy = 0;
		searchPanel.add( searchField, gbc_searchField );
		searchField.setColumns( 10 );

		doChangeFocusIcon = new AtomicBoolean( true );
		final MyFocusListener mfl = new MyFocusListener();
		mfl.focusLost( null );
		searchField.addFocusListener( mfl );

		final JCheckBox chckbxstartswith = new JCheckBox( "<html>starts<br>with</html>" );
		chckbxstartswith.setFont( chckbxstartswith.getFont().deriveFont( 8f ) );
		final GridBagConstraints gbc_chckbxstartswith = new GridBagConstraints();
		gbc_chckbxstartswith.gridx = 2;
		gbc_chckbxstartswith.gridy = 0;
		searchPanel.add( chckbxstartswith, gbc_chckbxstartswith );

		final SearchAction< V, E > sa = new SearchAction<>( graph, navigation, focus );
		searchField.addFocusListener( sa );
		labelIcon.addActionListener( ( event ) -> searchField.requestFocusInWindow() );

		searchField.addActionListener( new ActionListener()
		{
			@Override
			public void actionPerformed( final ActionEvent e )
			{
				doChangeFocusIcon.set( false );
				new Thread()
				{
					@Override
					public void run()
					{
						try
						{
							final boolean found = sa.search( searchField.getText(), chckbxstartswith.isSelected() );
							searchField.requestFocusInWindow();
							setIcon( found ? FOUND_ICON : NOT_FOUND_ICON );
						}
						finally
						{
							doChangeFocusIcon.set( true );
						}
					};
				}.start();
			}
		} );
	}

	private void setIcon( final Icon icon )
	{
		labelIcon.setIcon( icon );
		labelIcon.setPressedIcon( icon == UNFOCUSED_ICON ? FOCUSED_ICON : icon );
	}

	private static class SearchAction< V extends Vertex< E > & HasLabel, E extends Edge< V > >
			extends AbstractGraphAlgorithm< V, E >
			implements FocusListener
	{
		private Iterator< V > iterator;

		private final NavigationHandler< V, E > navigation;

		private final FocusModel< V, E > focus;

		private V start;

		private String previousSearchString = "";

		public SearchAction(
				final ReadOnlyGraph< V, E > graph,
				final NavigationHandler< V, E > navigation,
				final FocusModel< V, E > focus )
		{
			super( graph );
			this.navigation = navigation;
			this.focus = focus;
			this.start = graph.vertexRef();
			getStartFromFocus();
			reinit();
		}

		private synchronized boolean search( final String text, final boolean startsWith )
		{
			if ( !previousSearchString.equals( text ) )
			{
				reinit();
				previousSearchString = text;
			}

			while ( iterator.hasNext() )
			{
				final V v = iterator.next();
				final String label = v.getLabel();
				if ( startsWith ? label.startsWith( text ) : label.contains( text ) )
				{
					navigation.notifyNavigateToVertex( v );
					if ( iterator.hasNext() )
						start = assign( v, start );
					return true;
				}
			}
			return false;
		}

		private void getStartFromFocus()
		{
			// Start the search from the vertex with the focus.
			final V ref = graph.vertexRef();
			final V focusedVertex = focus.getFocusedVertex( ref );
			if ( null != focusedVertex )
				start = assign( focusedVertex, start );
			else if ( !graph.vertices().isEmpty() )
				start = assign( graph.vertices().iterator().next(), start );
		}

		private synchronized void reinit()
		{
			if ( graph.vertices().isEmpty() )
			{
				iterator = Collections.emptyIterator();
				return;
			}

			// First, look for the root of the start vertex.
			final InverseDepthFirstIterator< V, E > rootFinder =
					new InverseDepthFirstIterator<>( start, graph );

			V root = graph.vertexRef();
			boolean rootFound = false;
			while ( rootFinder.hasNext() )
			{
				final V next = rootFinder.next();
				if ( next.incomingEdges().isEmpty() )
				{
					root = assign( next, root );
					rootFound = true;
					break;
				}
			}
			final RefSet< V > roots = RootFinder.getRoots( graph );
			if ( !rootFound )
			{
				/*
				 * Hum we have a problem. Each vertex SHOULD have a root. If you
				 * reach this point in code, then there is an unknown problem
				 * probably related to changing roots while looking for the
				 * right root.
				 */
				System.err.println( "[SearchVertex] Could not find the root for vertex " + start );
				iterator = new BreadthFirstCrossComponentIterator<>( start, graph, roots );
				graph.releaseRef( root );
				return;
			}

			/*
			 * Now build an ordered root list that starts with the root just
			 * next to the one we have found.
			 */
			final RefList< V > sortedRoots = RefCollections.createRefList( graph.vertices(), roots.size() );
			sortedRoots.addAll( roots );
			sortedRoots.sort( ( v1, v2 ) -> AlphanumCompare.compare( v1.getLabel(), v2.getLabel() ) );

			final int iroot = sortedRoots.indexOf( root );
			final RefList< V > iteratedRoots = RefCollections.createRefList( graph.vertices(), roots.size() );
			for ( int i = iroot + 1; i < sortedRoots.size(); i++ )
				iteratedRoots.add( sortedRoots.get( i ) );
			for ( int i = 0; i <= iroot; i++ )
				iteratedRoots.add( sortedRoots.get( i ) );

			iterator = new BreadthFirstCrossComponentIterator<>( root, graph, iteratedRoots );
			// Rewind to the starting point.
			while ( !iterator.next().equals( start ) );

			graph.releaseRef( root );
		}

		@Override
		public void focusGained( final FocusEvent e )
		{
			getStartFromFocus();
		}

		@Override
		public void focusLost( final FocusEvent e )
		{}
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
					KeyConfigUtils.blockKeys( searchField );
					keysBlocked = true;
				}

				setIcon( FOCUSED_ICON );
				searchField.setText( oldText );
				searchField.setFont( searchField.getFont().deriveFont( Font.PLAIN ) );
				searchField.setForeground( UIManager.getColor( "Label.foreground" ) );
			}
		}
	}
}
