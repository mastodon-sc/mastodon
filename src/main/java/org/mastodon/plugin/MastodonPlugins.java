package org.mastodon.plugin;

import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.ActionMap;

import org.mastodon.app.ui.ViewMenu;
import org.mastodon.app.ui.ViewMenuBuilder;
import org.mastodon.app.ui.ViewMenuBuilder.MenuItem;
import org.mastodon.revised.ui.keymap.Keymap;
import org.scijava.ui.behaviour.util.Actions;

public class MastodonPlugins
{
	private final ArrayList< MastodonPlugin > plugins = new ArrayList<>();

	private final Actions pluginActions;

	private final ArrayList< MenuItem > menuItems;

	private final HashMap< String, String > menuTexts;

	public MastodonPlugins( final Keymap keymap )
	{
		pluginActions = new Actions( keymap.getConfig(), "mastodon" );
		keymap.updateListeners().add( () -> pluginActions.updateKeyConfig( keymap.getConfig() ) );
		menuItems = new ArrayList<>();
		menuTexts = new HashMap<>();
	}

	public synchronized void register( final MastodonPlugin plugin )
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

	public void setAppModel( final MastodonPluginAppModel model )
	{
		for ( final MastodonPlugin plugin : plugins )
			plugin.setAppModel( model );
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
