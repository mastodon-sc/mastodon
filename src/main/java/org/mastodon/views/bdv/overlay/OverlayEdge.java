package org.mastodon.views.bdv.overlay;

import org.mastodon.Ref;
import org.mastodon.graph.Edge;

public interface OverlayEdge< O extends OverlayEdge< O, V >, V extends OverlayVertex< V, ? > >
		extends Edge< V >, Ref< O >
{
	public O init();
}
