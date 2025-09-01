package org.mastodon.mamut.views;

import org.mastodon.app.ViewGraph;
import org.mastodon.app.ui.MastodonFrameView2;
import org.mastodon.graph.Edge;
import org.mastodon.graph.Vertex;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.Spot;

public interface MamutView2< 
		VG extends ViewGraph< Spot, Link, V, E >, 
		V extends Vertex< E >,
		E extends Edge< V > >
		extends MastodonFrameView2< Model, VG, Spot, Link, V, E >
{}
