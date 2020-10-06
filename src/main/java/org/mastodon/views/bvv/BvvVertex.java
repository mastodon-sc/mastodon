package org.mastodon.views.bvv;

import org.mastodon.Ref;
import org.mastodon.graph.Vertex;
import org.mastodon.spatial.HasTimepoint;

public interface BvvVertex< O extends BvvVertex< O, E >, E extends BvvEdge< E, ? > >
		extends Vertex< E >, Ref< O >, HasTimepoint
{
	void getCovariance( final double[][] mat );

	float x();

	float y();

	float z();
}
