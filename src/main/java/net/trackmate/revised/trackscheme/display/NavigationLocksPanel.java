package net.trackmate.revised.trackscheme.display;

import gnu.trove.set.hash.TIntHashSet;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;

import net.trackmate.revised.ui.selection.NavigationHandler;
import net.trackmate.revised.ui.selection.NavigationListener;

public class NavigationLocksPanel extends JPanel implements NavigationListener
{

	private static final long serialVersionUID = 1L;

	private static final ImageIcon LOCK_ICON = new ImageIcon( NavigationLocksPanel.class.getResource( "lock.png" ) );

	private static final ImageIcon UNLOCK_ICON = new ImageIcon( NavigationLocksPanel.class.getResource( "lock_open_grey.png" ) );

	private static final Font FONT = new Font( "Arial", Font.PLAIN, 10 );

	private static final int N_LOCKS = 3;

	private final NavigationHandler handler;

	private final TIntHashSet groups;

	private final TrackSchemePanel panel;

	public NavigationLocksPanel( final TrackSchemePanel panel, final NavigationHandler handler )
	{
		this.panel = panel;
		this.handler = handler;
		this.groups = new TIntHashSet();

		setLayout( new FlowLayout( FlowLayout.LEADING ) );
		for ( int i = 0; i < N_LOCKS; i++ )
		{
			final int lockId = i;
			final JToggleButton button = new JToggleButton( "" + ( i + 1 ), UNLOCK_ICON, false );
			button.setFont( FONT );
			button.setPreferredSize( new Dimension( 60, 20 ) );
			button.setHorizontalAlignment( SwingConstants.LEFT );
			button.setOpaque( false );
			button.setContentAreaFilled( false );
			button.setBorderPainted( false );
			button.addActionListener( new ActionListener()
			{
				@Override
				public void actionPerformed( final ActionEvent e )
				{
					if ( button.isSelected() )
						button.setIcon( LOCK_ICON );
					else
						button.setIcon( UNLOCK_ICON );
					activateGroup( lockId, button.isSelected() );
				}
			} );
			add( button );
		}
	}

	protected void activateGroup( final int lockId, final boolean activate )
	{
		if ( activate )
			groups.add( lockId );
		else
			groups.remove( lockId );
	}

	void navigateTo( final int modelVertexId )
	{
		handler.notifyListeners( groups, modelVertexId );
	}

	@Override
	public void navigateToVertex( final int modelVertexId )
	{
		panel.centerOn( modelVertexId );
	}

	@Override
	public boolean isInGroup( final int group )
	{
		return groups.contains( group );
	}

}
