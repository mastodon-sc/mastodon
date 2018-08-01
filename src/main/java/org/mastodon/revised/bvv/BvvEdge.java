package org.mastodon.revised.bvv;

import org.mastodon.Ref;
import org.mastodon.graph.Edge;

public interface BvvEdge< O extends BvvEdge< O, V >, V extends BvvVertex< V, ? > >
		extends Edge< V >, Ref< O >
{
	O init();
}
