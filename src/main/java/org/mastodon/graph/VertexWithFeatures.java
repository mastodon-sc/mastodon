package org.mastodon.graph;

import org.mastodon.features.Feature;
import org.mastodon.features.FeatureValue;

public interface VertexWithFeatures< V extends VertexWithFeatures< V, E >, E extends Edge< ? > > extends Vertex< E >
{
	public < F extends FeatureValue< ? >, M > F feature( final Feature< M, V, F > feature );
}
