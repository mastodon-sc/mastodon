package org.mastodon.mamut.plugin;

import org.mastodon.app.plugin.MastodonPlugins;
import org.mastodon.ui.keymap.Keymap;

public class MamutPlugins extends MastodonPlugins< MamutPlugin, MamutPluginAppModel >
{

	public MamutPlugins( final Keymap keymap )
	{
		super( keymap );
	}
}
