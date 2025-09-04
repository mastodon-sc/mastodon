package org.mastodon.mamut.views;

import org.mastodon.app.MastodonViewFactory;
import org.scijava.plugin.SciJavaPlugin;

/**
 * Marker interface for the Mamut app view factories.
 *
 * @author Jean-Yves Tinevez
 */
public interface MamutViewFactory2< T extends MamutView2 > extends MastodonViewFactory< T >, SciJavaPlugin
{}
