package org.mastodon.revised.bvv;

import org.mastodon.Ref;
import org.mastodon.graph.Vertex;
import org.mastodon.spatial.HasTimepoint;

public interface BvvVertex< O extends BvvVertex< O, E >, E extends BvvEdge< E, ? > >
		extends Vertex< E >, Ref< O >, HasTimepoint
{
}
