/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2023 Tobias Pietzsch, Jean-Yves Tinevez
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

	public static void build( final ViewMenu viewMenu, final ActionMap actionMap, final Map< String, String > menuTexts,
			final MenuItem... items )
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

		public MenuItemVisitor( final ViewMenu viewMenu, final ActionMap actionMap,
				final Map< String, String > menuTexts, final String path )
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
