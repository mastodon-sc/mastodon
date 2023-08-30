package org.mastodon.mamut.managers;

import org.mastodon.mamut.ProjectModel;
import org.mastodon.views.trackscheme.display.style.TrackSchemeStyleManager;
import org.mastodon.views.trackscheme.display.style.TrackSchemeStyleSettingsPage;
import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import bdv.ui.settings.SettingsPage;

@Plugin( type = StyleManagerFactory.class, priority = Priority.NORMAL - 1 )
public class TrackSchemeStyleManagerFactory implements StyleManagerFactory< TrackSchemeStyleManager >
{

	@Override
	public TrackSchemeStyleManager create( final ProjectModel projectModel )
	{
		return new TrackSchemeStyleManager();
	}

	@Override
	public boolean hasSettingsPage()
	{
		return true;
	}

	@Override
	public SettingsPage createSettingsPage( final TrackSchemeStyleManager manager )
	{
		return new TrackSchemeStyleSettingsPage( "Settings > TrackScheme Styles", manager );
	}

	@Override
	public Class< TrackSchemeStyleManager > getManagerClass()
	{
		return TrackSchemeStyleManager.class;
	}
}
