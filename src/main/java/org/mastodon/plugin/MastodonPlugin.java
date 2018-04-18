package org.mastodon.plugin;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.mastodon.app.ui.ViewMenuBuilder.MenuItem;
import org.scijava.plugin.SciJavaPlugin;
import org.scijava.ui.behaviour.util.Actions;

public interface MastodonPlugin extends SciJavaPlugin
{
	void setAppModel( final MastodonPluginAppModel appModel );

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
