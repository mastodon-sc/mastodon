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
package org.mastodon.app.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import javax.swing.Action;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.MenuElement;

import org.mastodon.util.HasSelectedState;
import org.mastodon.util.MastodonDebugSettings;
import org.scijava.ui.behaviour.InputTrigger;
import org.scijava.ui.behaviour.io.InputTriggerConfig;
import org.scijava.ui.behaviour.util.AbstractNamedAction;

import bdv.ui.keymap.Keymap;

public class ViewMenu
{
	private static final boolean USE_ACCELERATORS = MastodonDebugSettings.getInstance().isUseMenuAccelerators(); // TODO: remove, once Fiji ships at least jdk1.8.0_162

	private MastodonFrameView< ?, ?, ?, ?, ?, ? > view;

	private final JMenuBar menubar;

	private final Keymap keymap;

	private final Set< String > contexts;

	public ViewMenu( final MastodonFrameView< ?, ?, ?, ?, ?, ? > view )
	{
		this( view.getFrame().menubar, view.getKeymap(), view.getKeyConfigContexts() );
		this.view = view;

		final Keymap.UpdateListener updateListener = this::updateKeymap;
		keymap.updateListeners().add( updateListener );
		view.onClose( () -> keymap.updateListeners().remove( updateListener ) );
	}

	public ViewMenu( final JMenuBar menubar, final Keymap keymap, final String... contexts )
	{
		this( menubar, keymap, new HashSet<>( Arrays.asList( contexts ) ) );
	}

	public ViewMenu( final JMenuBar menubar, final Keymap keymap, final Set< String > contexts )
	{
		this.menubar = menubar;
		this.keymap = keymap;
		this.contexts = contexts;
	}

	public void addSeparator( final String path )
	{
		final JMenu menu = menu( path );
		if ( menu.getItemCount() != 0 )
		{
			final JPopupMenu popup = menu.getPopupMenu();
			if ( !( popup.getComponent( popup.getComponentCount() - 1 ) instanceof JSeparator ) )
				menu.addSeparator();
		}
	}

	public boolean addItem( final String path, final String name, final Action action )
	{
		final InputTriggerConfig keyconf = keymap.getConfig();

		final JMenu menu = menu( path );

		final int n = menu.getItemCount();
		for ( int i = 0; i < n; ++i )
			if ( menu.getItem( i ) != null && menu.getItem( i ).getText().equals( name ) )
				return false; // item with that name already exists
		final JMenuItem item = ( action instanceof HasSelectedState )
				? new JCheckBoxMenuItem( action )
				: new JMenuItem( action );
		item.setText( name );

		if ( USE_ACCELERATORS )
		{
			if ( action instanceof AbstractNamedAction )
			{
				final AbstractNamedAction namedAction = ( AbstractNamedAction ) action;
				final Set< InputTrigger > inputs = keyconf.getInputs( namedAction.name(), contexts );
				final Optional< InputTrigger > input = inputs.stream().filter( InputTrigger::isKeyStroke ).findFirst();
				if ( input.isPresent() )
					item.setAccelerator( input.get().getKeyStroke() );
			}
		}

		if ( action instanceof HasSelectedState )
		{
			final HasSelectedState s = ( HasSelectedState ) action;
			item.setSelected( s.isSelected() );
			final HasSelectedState.Listener l = item::setSelected;
			s.selectListeners().add( l );
			if ( view != null )
				view.onClose( () -> s.selectListeners().remove( l ) );
		}

		menu.add( item );
		return true;
	}

	public void updateKeymap()
	{
		if ( USE_ACCELERATORS )
		{
			final InputTriggerConfig keyconf = keymap.getConfig();

			final ArrayList< MenuElement > elements = new ArrayList<>();
			elements.add( menubar );

			while ( !elements.isEmpty() )
			{
				final MenuElement element = elements.remove( elements.size() - 1 );
				for ( final MenuElement me : element.getSubElements() )
				{
					if ( me instanceof JMenu || me instanceof JPopupMenu )
					{
						elements.add( me );
					}
					else if ( me instanceof JMenuItem )
					{
						final JMenuItem mi = ( JMenuItem ) me;
						final Action action = mi.getAction();
						if ( action instanceof AbstractNamedAction )
						{
							final AbstractNamedAction namedAction = ( AbstractNamedAction ) action;
							final Set< InputTrigger > inputs = keyconf.getInputs( namedAction.name(), contexts );
							final Optional< InputTrigger > input =
									inputs.stream().filter( InputTrigger::isKeyStroke ).findFirst();
							if ( input.isPresent() )
								mi.setAccelerator( input.get().getKeyStroke() );
							else
								mi.setAccelerator( null );
						}
					}
				}
			}
		}
	}

	JMenu menu( final String path )
	{
		final String[] parts = path.split( ">" );
		MenuElement root = menubar;
		for ( final String part : parts )
		{
			final String text = part.trim();

			JMenuItem next = null;
			A: for ( final MenuElement me : root.getSubElements() )
			{
				if ( me instanceof JPopupMenu )
				{
					final JPopupMenu pm = ( JPopupMenu ) me;
					for ( final MenuElement pme : pm.getSubElements() )
					{
						if ( pme instanceof JMenu )
						{
							final JMenuItem mi = ( JMenuItem ) pme;
							if ( mi.getText().equals( text ) )
							{
								next = mi;
								break A;
							}
						}
					}
				}
				if ( me instanceof JMenu )
				{
					final JMenuItem mi = ( JMenuItem ) me;
					if ( mi.getText().equals( text ) )
					{
						next = mi;
						break;
					}
				}
			}

			if ( next == null )
			{
				next = new JMenu( text );
				if ( root == menubar )
					menubar.add( next );
				else
					( ( JMenu ) root ).add( next );
			}

			root = next;
		}

		if ( root instanceof JMenu )
			return ( JMenu ) root;
		else
			throw new IllegalArgumentException( "menu item must be added to a submenu" );
	}
}
