package org.mastodon.app.ui;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.MenuElement;

import org.scijava.ui.behaviour.InputTrigger;
import org.scijava.ui.behaviour.io.InputTriggerConfig;
import org.scijava.ui.behaviour.util.AbstractNamedAction;

public class ViewMenu
{
	private final JMenuBar menubar;

	private final InputTriggerConfig keyconf;

	private final Set< String > contexts;

	public ViewMenu( final MastodonFrameView< ?, ?, ?, ?, ?, ? > view )
	{
		this( view.getFrame().menubar, view.getAppModel().getKeyConfig(), view.getAppModel().getKeyConfigContexts() );
	}

	public ViewMenu( final JMenuBar menubar, final InputTriggerConfig keyconf, final String... contexts )
	{
		this( menubar, keyconf, new HashSet<>( Arrays.asList( contexts ) ) );
	}

	public ViewMenu( final JMenuBar menubar, final InputTriggerConfig keyconf, final Set< String > contexts )
	{
		this.menubar = menubar;
		this.keyconf = keyconf;
		this.contexts = contexts;
	}

	public void addSeparator( final String path )
	{
		final JMenu menu = menu( path );
		menu.addSeparator();
	}

	public boolean addItem( final String path, final String name, final AbstractNamedAction action )
	{
		final JMenu menu = menu( path );

		final int n = menu.getItemCount();
		for ( int i = 0; i < n; ++i )
			if ( menu.getItem( i ) != null && menu.getItem( i ).getText().equals( name ) )
				return false; // item with that name already exists
		final JMenuItem item = new JMenuItem( action );
		item.setText( name );

		final Set< InputTrigger > inputs = keyconf.getInputs( action.name(), contexts );
		final Optional< InputTrigger > input = inputs.stream().filter( InputTrigger::isKeyStroke ).findFirst();
		if ( input.isPresent() )
			item.setAccelerator( input.get().getKeyStroke() );

		menu.add( item );
		return true;
	}

	public boolean addItem( final String path, final String name, final String action )
	{
		final JMenu menu = menu( path );

		final int n = menu.getItemCount();
		for ( int i = 0; i < n; ++i )
			if ( menu.getItem( i ) != null && menu.getItem( i ).getText().equals( name ) )
				return false; // item with that name already exists
		final JMenuItem item = new JMenuItem( name );
		menu.add( item );
		return true;
	}

	JMenu menu( final String path )
	{
		final String[] parts = path.split( ">" );
		MenuElement root = menubar;
		for ( final String part : parts )
		{
			final String text = part.trim();

			JMenuItem next = null;
			for ( final MenuElement me : root.getSubElements() )
			{
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
