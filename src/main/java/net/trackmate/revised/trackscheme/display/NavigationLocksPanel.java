package net.trackmate.revised.trackscheme.display;

import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

public class NavigationLocksPanel extends JPanel
{
	private static final ImageIcon LOCK_ICON = new ImageIcon( NavigationLocksPanel.class.getResource( "lock.png" ) );

	private static final ImageIcon UNLOCK_ICON = new ImageIcon( NavigationLocksPanel.class.getResource( "lock_open_grey.png" ) );

	private static final Font FONT = new Font( "Arial", Font.PLAIN, 10 );

	private static final int N_LOCKS = 5;

	public NavigationLocksPanel()
	{
		setLayout( new FlowLayout( FlowLayout.LEADING ) );
		for ( int i = 0; i < N_LOCKS; i++ )
		{
			final int lockId = i;
			final JToggleButton button = new JToggleButton( "" + ( i + 1 ), UNLOCK_ICON, false );
			button.setFont( FONT );
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
					activateListener( lockId, button.isSelected() );
				}
			} );
			add( button );
		}
	}

	protected void activateListener( final int lockId, final boolean activate )
	{
		System.out.println( "Lock Id " + lockId + " is " + ( activate ? "activated." : "not activated." ) ); // TODO
	}
}
