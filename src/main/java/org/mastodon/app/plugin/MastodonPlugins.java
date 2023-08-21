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
package org.mastodon.app.plugin;

import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.ActionMap;

import org.mastodon.app.ui.ViewMenu;
import org.mastodon.app.ui.ViewMenuBuilder;
import org.mastodon.app.ui.ViewMenuBuilder.MenuItem;
import org.mastodon.ui.keymap.KeyConfigContexts;
import org.scijava.ui.behaviour.util.Actions;

import bdv.ui.keymap.Keymap;

public class MastodonPlugins< PL extends MastodonPlugin< M >, M extends MastodonAppPluginModel >
{
	private final ArrayList< PL > plugins = new ArrayList<>();

	private final Actions pluginActions;

	private final ArrayList< MenuItem > menuItems;

	private final HashMap< String, String > menuTexts;

	public MastodonPlugins( final Keymap keymap )
	{
		pluginActions = new Actions( keymap.getConfig(), KeyConfigContexts.MASTODON, KeyConfigContexts.BIGDATAVIEWER,
				KeyConfigContexts.TRACKSCHEME, KeyConfigContexts.TABLE );
		keymap.updateListeners().add( () -> pluginActions.updateKeyConfig( keymap.getConfig() ) );
		menuItems = new ArrayList<>();
		menuTexts = new HashMap<>();
	}

	public synchronized void register( final PL plugin )
	{
		if ( !plugins.contains( plugin ) )
		{
			plugins.add( plugin );

			/*
			 * TODO: prefix action names with unique string? Maybe better leave
			 * that to plugin implementors to make it easy to call actions
			 * across plugins?
			 */
			menuItems.addAll( plugin.getMenuItems() );

			// collect menuTexts from plugin
			plugin.getMenuTexts().entrySet().forEach( entry -> menuTexts.put( entry.getKey(), entry.getValue() ) );

			plugin.installGlobalActions( pluginActions );
		}
	}

	public void addMenus( final ViewMenu menu )
	{
		addMenus( menu, pluginActions.getActionMap() );
	}

	public void addMenus( final ViewMenu menu, final ActionMap actionMap )
	{
		ViewMenuBuilder.build( menu, actionMap, menuTexts, menuItems.toArray( new MenuItem[ 0 ] ) );
	}

	public Actions getPluginActions()
	{
		return pluginActions;
	}
}
