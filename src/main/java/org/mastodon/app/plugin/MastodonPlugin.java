package org.mastodon.app.plugin;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.mastodon.app.ui.ViewMenuBuilder.MenuItem;
import org.scijava.plugin.SciJavaPlugin;
import org.scijava.ui.behaviour.util.Actions;

/**
 * Mother interface for Mastodon plugins.
 * <p>
 * Each concrete app should have a more specialized interface deriving from this
 * one, that specifies against what concrete {@link MastodonAppPluginModel} it
 * is built.
 *
 * @param <M>
 *            the type of {@link MastodonAppPluginModel} this plugin will use.
 */
public interface MastodonPlugin< M extends MastodonAppPluginModel > extends SciJavaPlugin
{
	void setAppPluginModel( final M appPluginModel );

	default List< MenuItem > getMenuItems()
	{
		return Collections.emptyList();
	}

	default Map< String, String > getMenuTexts()
	{
		return Collections.emptyMap();
	}

	default void installGlobalActions( final Actions pluginActions )
	{};
}
