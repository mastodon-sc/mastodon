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

import org.mastodon.revised.ui.keymap.Keymap;
import org.mastodon.revised.util.HasSelectedState;
import org.scijava.ui.behaviour.InputTrigger;
import org.scijava.ui.behaviour.io.InputTriggerConfig;
import org.scijava.ui.behaviour.util.AbstractNamedAction;

public class ViewMenu
{
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
		if ( menu.getItemCount() != 0 ) {
			final JPopupMenu popup = menu.getPopupMenu();
			if ( ! ( popup.getComponent( popup.getComponentCount() - 1 ) instanceof JSeparator ) )
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

		if ( action instanceof AbstractNamedAction )
		{
			final AbstractNamedAction namedAction = ( AbstractNamedAction ) action;
			final Set< InputTrigger > inputs = keyconf.getInputs( namedAction.name(), contexts );
			final Optional< InputTrigger > input = inputs.stream().filter( InputTrigger::isKeyStroke ).findFirst();
			if ( input.isPresent() )
				item.setAccelerator( input.get().getKeyStroke() );
		}

		if ( action instanceof HasSelectedState )
		{
			final HasSelectedState s = ( HasSelectedState ) action;
			item.setSelected( s.isSelected() );
			final HasSelectedState.Listener l = b -> item.setSelected( b );
			s.selectListeners().add( l );
			if ( view != null )
				view.onClose( () -> s.selectListeners().remove( l ) );
		}

		menu.add( item );
		return true;
	}

	public void updateKeymap()
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
					if ( action != null && action instanceof AbstractNamedAction )
					{
						final AbstractNamedAction namedAction = ( AbstractNamedAction ) action;
						final Set< InputTrigger > inputs = keyconf.getInputs( namedAction.name(), contexts );
						final Optional< InputTrigger > input = inputs.stream().filter( InputTrigger::isKeyStroke ).findFirst();
						if ( input.isPresent() )
							mi.setAccelerator( input.get().getKeyStroke() );
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
A:			for ( final MenuElement me : root.getSubElements() )
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
				else if ( root instanceof JMenu )
					( ( JMenu ) root ).add( next );
				else
					throw new IllegalArgumentException( "menu item must be added to a submenu" );
			}

			if ( next instanceof JMenu )
				root = next;
			else
				throw new IllegalArgumentException( "menu item must be added to a submenu" );
		}

		if ( root instanceof JMenu )
			return ( JMenu ) root;
		else
			throw new IllegalArgumentException( "menu item must be added to a submenu" );
	}
}
