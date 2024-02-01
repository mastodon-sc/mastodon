/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2024 Tobias Pietzsch, Jean-Yves Tinevez
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
package org.mastodon.mamut;

import static org.mastodon.mamut.MamutMenuBuilder.windowMenu;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ActionMap;

import org.mastodon.app.ui.ViewMenu;
import org.mastodon.app.ui.ViewMenuBuilder;
import org.mastodon.app.ui.ViewMenuBuilder.MenuItem;
import org.mastodon.mamut.views.MamutViewFactory;
import org.mastodon.mamut.views.MamutViewI;
import org.mastodon.ui.keymap.KeyConfigContexts;
import org.scijava.ui.behaviour.io.gui.CommandDescriptionProvider;
import org.scijava.ui.behaviour.io.gui.CommandDescriptions;

/**
 * Manages a collection of view factories.
 * <p>
 * Collect and install actions, menu items, menu texts.
 */
public class MamutViews
{
	private final Map< Class< ? extends MamutViewI >, MamutViewFactory< ? extends MamutViewI > > factories = new HashMap<>();

	private final ArrayList< MenuItem > menuItems;

	private final HashMap< String, String > menuTexts;

	MamutViews()
	{
		menuItems = new ArrayList<>();
		menuTexts = new HashMap<>();
	}

	synchronized void register( final MamutViewFactory< ? extends MamutViewI > factory, final ProjectModel projectModel )
	{
		if ( !factories.containsValue( factory ) )
		{
			factories.put( factory.getViewClass(), factory );
			menuItems.add( ViewMenuBuilder.item( factory.getCommandName() ) );
			menuTexts.put( factory.getCommandName(), factory.getCommandMenuText() );
		}
	}

	/**
	 * Returns the collection of view classes for which we have a factory.
	 * 
	 * @return the collection of view classes.
	 */
	public Collection< Class< ? extends MamutViewI > > getKeys()
	{
		return Collections.unmodifiableCollection( factories.keySet() );
	}

	/**
	 * Returns a factory for the specified view class.
	 * 
	 * @param <T>
	 *            the type of view.
	 * @param klass
	 *            the class of the view.
	 * @return a view factory, or <code>null</code> if the specified class is
	 *         unknown.
	 */
	@SuppressWarnings( "unchecked" )
	public < T extends MamutViewI > MamutViewFactory< T > getFactory( final Class< T > klass )
	{
		return ( MamutViewFactory< T > ) factories.get( klass );
	}

	CommandDescriptionProvider getCommandDescriptions()
	{
		return new CommandDescriptionProvider( KeyConfigScopes.MAMUT, KeyConfigContexts.MASTODON )
		{

			@Override
			public void getCommandDescriptions( final CommandDescriptions descriptions )
			{
				for ( final MamutViewFactory< ? extends MamutViewI > factory : factories.values() )
					descriptions.add( factory.getCommandName(), factory.getCommandKeys(), factory.getCommandDescription() );
			}
		};
	}

	void addWindowMenuTo( final ViewMenu menu, final ActionMap actionMap )
	{
		MamutMenuBuilder.build( menu, actionMap, menuTexts, windowMenu( menuItems.toArray( new MenuItem[ 0 ] ) ) );
	}
}
