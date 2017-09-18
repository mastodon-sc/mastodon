package org.mastodon.revised.ui.grouping;

import static org.mastodon.revised.ui.grouping.GroupManager.NO_GROUP;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;

public class GroupLocksPanel extends JPanel implements GroupChangeListener
{
	private static final long serialVersionUID = 1L;

	private static final ImageIcon LOCK_ICON = new ImageIcon( GroupLocksPanel.class.getResource( "lock.png" ) );

	private static final ImageIcon UNLOCK_ICON = new ImageIcon( GroupLocksPanel.class.getResource( "lock_open_grey.png" ) );

	private static final Font FONT = new Font( "Arial", Font.PLAIN, 10 );

	private final ArrayList< JToggleButton > buttons;

	private final GroupHandle groupHandle;

	public GroupLocksPanel( final GroupHandle groupHandle )
	{
		super( new FlowLayout( FlowLayout.LEADING ) );
		this.groupHandle = groupHandle;
		this.buttons = new ArrayList<>();
		final int numGroups = groupHandle.getNumGroups();
		for ( int i = 0; i < numGroups; i++ )
		{
			final int lockId = i;
			final boolean isActive = groupHandle.getGroupId() == lockId;
			final JToggleButton button = new JToggleButton( "" + ( i + 1 ), isActive ? LOCK_ICON : UNLOCK_ICON, isActive );
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
						groupHandle.setGroupId( lockId );
					else
						groupHandle.setGroupId( NO_GROUP );
				}
			} );
			add( button );
			buttons.add( button );
		}
		groupHandle.groupChangeListeners().add( this );
	}

	@Override
	public void groupChanged()
	{
		final int numGroups = groupHandle.getNumGroups();
		for ( int i = 0; i < numGroups; i++ )
		{
			final boolean activated = groupHandle.getGroupId() == i;
			final JToggleButton button = buttons.get( i );
			button.setSelected( activated );
			button.setIcon( activated ? LOCK_ICON : UNLOCK_ICON );
		}
	}
}
