package net.trackmate.revised.trackscheme.display;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;

import net.trackmate.revised.ui.selection.NavigationGroupHandler;

public class NavigationLocksPanel extends JPanel
{
	private static final long serialVersionUID = 1L;

	private static final ImageIcon LOCK_ICON = new ImageIcon( NavigationLocksPanel.class.getResource( "lock.png" ) );

	private static final ImageIcon UNLOCK_ICON = new ImageIcon( NavigationLocksPanel.class.getResource( "lock_open_grey.png" ) );

	private static final Font FONT = new Font( "Arial", Font.PLAIN, 10 );

	private static final int N_LOCKS = 3;

	private final NavigationGroupHandler groupHandler;

	/*
	 * TODO: NavigationGroupHandler should also have NavigationGroupEmmitter&Reveiver methods, so that button state can be set from the actual group membership state.
	 * TODO: This is then general enough to be reused in BDV window as well.
	 */
	public NavigationLocksPanel( final NavigationGroupHandler groupHandler )
	{
		this.groupHandler = groupHandler;

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
					final boolean active = button.isSelected();
					button.setIcon( active ? LOCK_ICON : UNLOCK_ICON );
					groupHandler.setGroupActive( lockId, active );
				}
			} );
			add( button );
		}
	}
}
