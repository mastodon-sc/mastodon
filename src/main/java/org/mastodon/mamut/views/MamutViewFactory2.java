package org.mastodon.mamut.views;

import org.mastodon.mamut.MamutAppModel;
import org.mastodon.views.MastodonFrameView2;
import org.mastodon.views.MastodonViewFactory;
import org.scijava.plugin.SciJavaPlugin;

/**
 * Marker interface for the Mamut app view factories.
 *
 * @author Jean-Yves Tinevez
 */
public interface MamutViewFactory2< T extends MastodonFrameView2 >
		extends MastodonViewFactory< T, MamutAppModel >, SciJavaPlugin
{}
