package org.mastodon.revised.trackscheme.display;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;

import org.mastodon.graph.GraphChangeListener;
import org.mastodon.graph.algorithm.traversal.BreadthFirstIterator;
import org.mastodon.revised.trackscheme.LexicographicalVertexOrder;
import org.mastodon.revised.trackscheme.TrackSchemeEdge;
import org.mastodon.revised.trackscheme.TrackSchemeGraph;
import org.mastodon.revised.trackscheme.TrackSchemeNavigation;
import org.mastodon.revised.trackscheme.TrackSchemeVertex;

import com.itextpdf.text.Font;

public class TrackSchemeSearchPanel extends JPanel
{
	private static final long serialVersionUID = 1L;

	private static final String UNFOCUSED_TEXT = "Search...";

	private static final ImageIcon FOCUSED_ICON = new ImageIcon( TrackSchemeSearchPanel.class.getResource( "find-24x24-orange.png" ) );

	private static final ImageIcon UNFOCUSED_ICON = new ImageIcon( TrackSchemeSearchPanel.class.getResource( "find-24x24.png" ) );

	private static final ImageIcon FOUND_ICON = new ImageIcon( TrackSchemeSearchPanel.class.getResource( "find-24x24-green.png" ) );

	private static final ImageIcon NOT_FOUND_ICON = new ImageIcon( TrackSchemeSearchPanel.class.getResource( "find-24x24-red.png" ) );

	public TrackSchemeSearchPanel()
	{
		this( null, null );
	}

	public TrackSchemeSearchPanel( final TrackSchemeGraph< ?, ? > graph, final TrackSchemeNavigation navigation )
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

		final JLabel labelIcon = new JLabel( UNFOCUSED_ICON );
		final GridBagConstraints gbc_labelIcon = new GridBagConstraints();
		gbc_labelIcon.anchor = GridBagConstraints.WEST;
		gbc_labelIcon.gridx = 0;
		gbc_labelIcon.gridy = 0;
		add( labelIcon, gbc_labelIcon );

		final JTextField searchField = new JTextField();
		searchField.setBorder( null );
		final GridBagConstraints gbc_searchField = new GridBagConstraints();
		gbc_searchField.fill = GridBagConstraints.HORIZONTAL;
		gbc_searchField.anchor = GridBagConstraints.EAST;
		gbc_searchField.gridx = 1;
		gbc_searchField.gridy = 0;
		add( searchField, gbc_searchField );
		searchField.setColumns( 10 );

		final AtomicBoolean doChangeFocusIcon = new AtomicBoolean( true );
		final MyFocusListener mfl = new MyFocusListener( searchField, labelIcon, doChangeFocusIcon );
		mfl.focusLost( null );
		searchField.addFocusListener( mfl );

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
							searchField.requestFocusInWindow();
							labelIcon.setIcon( found ? FOUND_ICON : NOT_FOUND_ICON );
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
			iterator = new BreadthFirstIterator< TrackSchemeVertex, TrackSchemeEdge >( root, graph );
		}
	}

	private static class MyFocusListener implements FocusListener
	{

		private String oldText;

		private final JTextField searchField;

		private final JLabel labelIcon;

		private final AtomicBoolean doChangeFocusIcon;

		public MyFocusListener( final JTextField searchField, final JLabel labelIcon, final AtomicBoolean doChangeFocusIcon )
		{
			this.labelIcon = labelIcon;
			this.doChangeFocusIcon = doChangeFocusIcon;
			this.searchField = searchField;
		}

		@Override
		public void focusLost( final FocusEvent evt )
		{
			if ( doChangeFocusIcon.get() )
			{
				labelIcon.setIcon( UNFOCUSED_ICON );
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
				labelIcon.setIcon( FOCUSED_ICON );
				searchField.setText( oldText );
				searchField.setFont( searchField.getFont().deriveFont( Font.NORMAL ) );
				searchField.setForeground( UIManager.getColor( "Label.foreground" ) );
			}
		}
	}

}
