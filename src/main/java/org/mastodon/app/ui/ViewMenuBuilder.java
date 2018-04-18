package org.mastodon.app.ui;

import java.util.Arrays;
import java.util.Map;

import javax.swing.ActionMap;
import javax.swing.JMenu;

public class ViewMenuBuilder
{
	public static Menu menu( final String name, final MenuItem... items )
	{
		return new Menu( name, items );
	}

	public static Menu menu( final String name, final JMenuHandle handle, final MenuItem... items )
	{
		return new Menu( name, handle, items );
	}

	public static Item item( final String action )
	{
		return new Item( action );
	}

	public static Separator separator()
	{
		return new Separator();
	}

	public interface MenuItem
	{
		void accept( MenuItemVisitor visitor );
	}

	public static void build( final ViewMenu viewMenu, final ActionMap actionMap, final Map< String, String > menuTexts, final MenuItem... items )
	{
		final MenuItemVisitor visitor = new MenuItemVisitor( viewMenu, actionMap, menuTexts, null );
		Arrays.asList( items ).forEach( item -> item.accept( visitor ) );
	}

	protected static class MenuItemVisitor
	{
		private final ViewMenu viewMenu;

		private final ActionMap actionMap;

		private final Map< String, String > menuTexts;

		private final String path;

		public MenuItemVisitor( final ViewMenu viewMenu, final ActionMap actionMap, final Map< String, String > menuTexts, final String path )
		{
			this.viewMenu = viewMenu;
			this.menuTexts = menuTexts;
			this.actionMap = actionMap;
			this.path = path;
		}

		void visit( final Item item )
		{
			final String text = menuTexts.getOrDefault( item.action, item.action );
			viewMenu.addItem( path, text, actionMap.get( item.action ) );
		}

		void visit( final Separator separator )
		{
			viewMenu.addSeparator( path );
		}

		void visit( final Menu menu )
		{
			final String nestedPath;
			if ( path != null && !path.isEmpty() )
				nestedPath = path + ">" + menu.text;
			else
				nestedPath = menu.text;
			final JMenu m = viewMenu.menu( nestedPath );
			if ( menu.handle != null )
				menu.handle.menu = m;
			final MenuItemVisitor visitor = new MenuItemVisitor( viewMenu, actionMap, menuTexts, nestedPath );
			for ( final MenuItem menuItem : menu.content )
			{
				menuItem.accept( visitor );
			}
		}
	}

	static class Item implements MenuItem
	{
		private final String action;

		Item( final String action )
		{
			this.action = action;
		}

		@Override
		public void accept( final MenuItemVisitor visitor )
		{
			visitor.visit( this );
		}
	}

	static class Separator implements MenuItem
	{
		@Override
		public void accept( final MenuItemVisitor visitor )
		{
			visitor.visit( this );
		}
	}

	public static class JMenuHandle
	{
		JMenu menu;

		public JMenu getMenu()
		{
			return menu;
		}
	}

	static class Menu implements MenuItem
	{
		private final String text;

		private final JMenuHandle handle;

		private final MenuItem[] content;

		Menu( final String text, final MenuItem... content )
		{
			this( text, null, content );
		}

		Menu( final String text, final JMenuHandle handle, final MenuItem... content )
		{
			this.text = text;
			this.handle = handle;
			this.content = content;
		}

		@Override
		public void accept( final MenuItemVisitor visitor )
		{
			visitor.visit( this );
		}
	}
}
