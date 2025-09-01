package org.mastodon.app.ui;

import org.mastodon.app.ViewGraph;
import org.mastodon.graph.Edge;
import org.mastodon.graph.Vertex;
import org.mastodon.model.MastodonModel;

public interface MastodonFrameView2<
	M extends MastodonModel< ?, MV, ME >,
	VG extends ViewGraph< MV, ME, V, E >,
	MV extends Vertex< ME >,
	ME extends Edge< MV >,
	V extends Vertex< E >,
	E extends Edge< V > > extends MastodonView2< M, VG, MV, ME, V, E >, HasFrame
{}
