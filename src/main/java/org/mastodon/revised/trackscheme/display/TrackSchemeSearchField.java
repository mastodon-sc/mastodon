package org.mastodon.revised.trackscheme.display;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.ImageIcon;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.Border;

import org.mastodon.graph.GraphChangeListener;
import org.mastodon.revised.trackscheme.TrackSchemeGraph;
import org.mastodon.revised.trackscheme.TrackSchemeNavigation;
import org.mastodon.revised.trackscheme.TrackSchemeVertex;

public class TrackSchemeSearchField extends JTextField
{
	private static final long serialVersionUID = 1L;

	private static final String UNFOCUSED_TEXT = "Search...";

	private static final ImageIcon FOCUSED_ICON = new ImageIcon( TrackSchemeSearchField.class.getResource( "find-24x24-orange.png" ) );

	private static final ImageIcon UNFOCUSED_ICON = new ImageIcon( TrackSchemeSearchField.class.getResource( "find-24x24.png" ) );

	private static final ImageIcon FOUND_ICON = new ImageIcon( TrackSchemeSearchField.class.getResource( "find-24x24-green.png" ) );

	private static final ImageIcon NOT_FOUND_ICON = new ImageIcon( TrackSchemeSearchField.class.getResource( "find-24x24-red.png" ) );

	private final TrackSchemeGraph< ?, ? > graph;

	private final int leftInset;

	private ImageIcon icon;

	private final TrackSchemeNavigation navigation;

	public TrackSchemeSearchField( final TrackSchemeGraph< ?, ? > graph, final TrackSchemeNavigation navigation )
	{
		this.graph = graph;
		this.navigation = navigation;

		final Border border = UIManager.getBorder( "TextField.border" );
		final JTextField dummy = new JTextField();
		leftInset = border.getBorderInsets( dummy ).left;

		icon = UNFOCUSED_ICON;

		setPreferredSize( new Dimension( 160, 25 ) );
		setMaximumSize( new Dimension( 260, 25 ) );
		setMinimumSize( new Dimension( 26, 25 ) );

		final AtomicBoolean doChangeFocusIcon = new AtomicBoolean( false );
		addFocusListener( new java.awt.event.FocusAdapter()
		{
			@Override
			public void focusLost( final java.awt.event.FocusEvent evt )
			{
				if ( doChangeFocusIcon.get() )
				{
					icon = UNFOCUSED_ICON;
					repaint();
				}
			}
		} );

		final SearchAction sa = new SearchAction();
		graph.addGraphChangeListener( sa );
		addActionListener( new ActionListener()
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
							final boolean found = sa.search( getText() );
							requestFocusInWindow();
							icon = found ? FOUND_ICON : NOT_FOUND_ICON;
							repaint();
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

	@Override
	protected void paintComponent( final Graphics g )
	{
		super.paintComponent( g );

		int textX = 2;
		if ( this.icon != null )
		{
			final int iconWidth = icon.getIconWidth();
			final int iconHeight = icon.getIconHeight();
			final int x = leftInset;
			textX = x + iconWidth + 2;
			final int y = ( this.getHeight() - iconHeight ) / 2;
			icon.paintIcon( this, g, x, y );
		}
		setMargin( new Insets( 2, textX, 2, 2 ) );

		if ( !hasFocus() && getText().equals( "" ) )
		{
			final int height = this.getHeight();
			final Font prev = g.getFont();
			final Font italic = prev.deriveFont( Font.ITALIC );
			final Color prevColor = g.getColor();
			g.setFont( italic );
			g.setColor( UIManager.getColor( "textInactiveText" ) );
			final int h = g.getFontMetrics().getHeight();
			final int textBottom = ( height - h ) / 2 + h - 4;
			final int x = this.getInsets().left;
			final Graphics2D g2d = ( Graphics2D ) g;
			final RenderingHints hints = g2d.getRenderingHints();
			g2d.setRenderingHint( RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON );
			g2d.drawString( UNFOCUSED_TEXT, x, textBottom );
			g2d.setRenderingHints( hints );
			g.setFont( prev );
			g.setColor( prevColor );
		}
	}

	private class SearchAction implements GraphChangeListener
	{
		private Iterator< TrackSchemeVertex > iterator;

		public SearchAction()
		{
			reinit();
		}

		private synchronized boolean search( final String text )
		{
			final TrackSchemeVertex start = graph.vertexRef();
			TrackSchemeVertex v = next();
			start.refTo( v );
			do
			{
				if ( v.getLabel().contains( text ) )
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
			iterator = graph.vertices().iterator();
		}
	}
}
