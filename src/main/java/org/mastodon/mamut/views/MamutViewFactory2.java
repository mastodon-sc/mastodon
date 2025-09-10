package org.mastodon.mamut.views;

import org.mastodon.app.factory.MastodonViewFactory;
import org.mastodon.app.ui.MastodonFrameView2;
import org.mastodon.mamut.MamutAppModel;
import org.scijava.plugin.SciJavaPlugin;

/**
 * Marker interface for the Mamut app view factories.
 *
 * @author Jean-Yves Tinevez
 */
public interface MamutViewFactory2< T extends MastodonFrameView2 >
		extends MastodonViewFactory< T, MamutAppModel >, SciJavaPlugin
{}
