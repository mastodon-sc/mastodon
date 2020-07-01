package org.mastodon.mamut.plugin;

import org.mastodon.app.plugin.MastodonAppPluginModel;
import org.mastodon.mamut.MamutAppModel;
import org.mastodon.mamut.WindowManager;

public class MamutPluginAppModel  implements MastodonAppPluginModel
{
	private final WindowManager windowManager;

	private final MamutAppModel appModel;

	public MamutPluginAppModel( final MamutAppModel appModel, final WindowManager windowManager )
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
