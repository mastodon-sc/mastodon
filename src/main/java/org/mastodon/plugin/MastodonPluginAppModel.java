package org.mastodon.plugin;

import org.mastodon.revised.mamut.MamutAppModel;
import org.mastodon.revised.mamut.WindowManager;

public class MastodonPluginAppModel
{
	private final WindowManager windowManager;

	private final MamutAppModel appModel;

	public MastodonPluginAppModel( final MamutAppModel appModel, final WindowManager windowManager )
	{
		this.appModel = appModel;
		this.windowManager = windowManager;
	}

	public MamutAppModel getAppModel()
	{
		return appModel;
	}

	public WindowManager getWindowManager()
	{
		return windowManager;
	}
}
