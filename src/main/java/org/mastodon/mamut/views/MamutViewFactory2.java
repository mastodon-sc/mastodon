package org.mastodon.mamut.views;

import org.mastodon.app.views.MastodonFrameView2;
import org.mastodon.app.views.MastodonViewFactory;
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
