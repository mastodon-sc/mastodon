package org.mastodon.mamut.managers;

import org.mastodon.mamut.ProjectModel;
import org.mastodon.views.grapher.display.style.DataDisplayStyleManager;
import org.mastodon.views.grapher.display.style.DataDisplayStyleSettingsPage;
import org.scijava.Priority;
import org.scijava.plugin.Plugin;

import bdv.ui.settings.SettingsPage;

@Plugin( type = StyleManagerFactory.class, priority = Priority.NORMAL - 2 )
public class DataDisplayStyleManagerFactory implements StyleManagerFactory< DataDisplayStyleManager >
{

	@Override
	public DataDisplayStyleManager create( final ProjectModel projectModel )
	{
		return new DataDisplayStyleManager();
	}

	@Override
	public boolean hasSettingsPage()
	{
		return true;
	}

	@Override
	public SettingsPage createSettingsPage( final DataDisplayStyleManager manager )
	{
		return new DataDisplayStyleSettingsPage( "Grapher Styles", manager );
	}

	@Override
	public Class< DataDisplayStyleManager > getManagerClass()
	{
		return DataDisplayStyleManager.class;
	}
}
