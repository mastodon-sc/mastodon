package org.mastodon.app.plugin;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.scijava.Context;
import org.scijava.InstantiableException;
import org.scijava.plugin.PluginInfo;
import org.scijava.plugin.PluginService;
import org.scijava.plugin.SciJavaPlugin;

/**
 * Static utilities to manage and discover classes that typically implement
 * generic plugin functionalities.
 */
public class PluginUtils
{

	/**
	 * Discovers plugins of the specified class, and executes the specified
	 * action on all of them that are marked as <code>enabled</code>.
	 * 
	 * @param <T>
	 *            the type of plugin to discover.
	 * @param pluginClass
	 *            the class of plugin.
	 * @param action
	 *            the action to execute.
	 * @param context
	 *            the current context.
	 */
	public static < T extends SciJavaPlugin > void forEachDiscoveredPlugin( final Class< T > pluginClass, final Consumer< ? super T > action, final Context context )
	{
		final BiConsumer< ? super T, PluginInfo< T > > bc = ( plugin, info ) -> action.accept( plugin );
		forEachDiscoveredPlugin( pluginClass, bc, context );
	}

	/**
	 * Discovers plugins of the specified class, and executes the specified
	 * action on all of them that are marked as <code>enabled</code>.
	 * <p>
	 * This version of the method accepts a {@link BiConsumer}, that will also
	 * receive the plugin info.
	 * 
	 * @param <T>
	 *            the type of plugin to discover.
	 * @param pluginClass
	 *            the class of plugin.
	 * @param action
	 *            the action to execute.
	 * @param context
	 *            the current context.
	 */
	public static < T extends SciJavaPlugin > void forEachDiscoveredPlugin( final Class< T > pluginClass, final BiConsumer< ? super T, PluginInfo< T > > action, final Context context )
	{
		final PluginService pluginService = context.getService( PluginService.class );
		final List< PluginInfo< T > > infos = pluginService.getPluginsOfType( pluginClass );
		for ( final PluginInfo< T > info : infos )
		{
			if ( !info.isEnabled() )
				continue;

			try
			{
				final T plugin = info.createInstance();
				context.inject( plugin );
				action.accept( plugin, info );
			}
			catch ( final InstantiableException e )
			{
				e.printStackTrace();
			}
		}
	}

}
