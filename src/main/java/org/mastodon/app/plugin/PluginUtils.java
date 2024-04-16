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
