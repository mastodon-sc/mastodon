/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2022 Tobias Pietzsch, Jean-Yves Tinevez
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
package org.mastodon.app.ui;

import static org.mastodon.grouping.GroupManager.NO_GROUP;

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
import org.mastodon.grouping.GroupChangeListener;
import org.mastodon.grouping.GroupHandle;

/**
 * TODO: javadoc
 *
 * @author Tobias Pietzsch
 * @author Jean-Ives Tinevez
 */
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
