package org.mastodon.mamut.views;

import org.mastodon.app.MastodonViewFactory;
import org.mastodon.app.ui.MastodonFrameView2;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.scijava.plugin.SciJavaPlugin;

/**
 * Marker interface for the Mamut app view factories.
 *
 * @author Jean-Yves Tinevez
 */
public interface MamutViewFactory2< T extends MastodonFrameView2< Model, ?, Spot, Link, ?, ? > > extends SciJavaPlugin, MastodonViewFactory< T, Model, ModelGraph, Spot, Link >
{}
