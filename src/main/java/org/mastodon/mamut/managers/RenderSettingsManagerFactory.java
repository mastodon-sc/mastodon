package org.mastodon.mamut.managers;

import org.mastodon.mamut.ProjectModel;
import org.mastodon.views.bdv.overlay.ui.RenderSettingsConfigPage;
import org.mastodon.views.bdv.overlay.ui.RenderSettingsManager;
import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import bdv.ui.settings.SettingsPage;

@Plugin( type = StyleManagerFactory.class, priority = Priority.NORMAL )
public class RenderSettingsManagerFactory implements StyleManagerFactory< RenderSettingsManager >
{

	@Override
	public RenderSettingsManager create( final ProjectModel projectModel )
	{
		return new RenderSettingsManager();
	}

	@Override
	public boolean hasSettingsPage()
	{
		return true;
	}

	@Override
	public SettingsPage createSettingsPage( final RenderSettingsManager manager )
	{
		return new RenderSettingsConfigPage( "Settings > BDV Render Settings", manager );
	}

	@Override
	public Class< RenderSettingsManager > getManagerClass()
	{
		return RenderSettingsManager.class;
	}
}
