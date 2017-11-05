package org.mastodon.app.ui;

import java.util.Map;

import javax.swing.ActionMap;

public class ViewMenuBuilder
{
	public static Menu menu( final String name, final MenuItem... items )
	{
		return new Menu( name, items );
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
			viewMenu.menu( nestedPath );
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

	static class Menu implements MenuItem
	{
		private final String text;

		private final MenuItem[] content;

		Menu( final String text, final MenuItem... content )
		{
			this.text = text;
			this.content = content;
		}

		@Override
		public void accept( final MenuItemVisitor visitor )
		{
			visitor.visit( this );
		}
	}
}
