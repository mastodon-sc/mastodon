package org.mastodon.graph;

import org.mastodon.graph.features.Feature;

public interface EdgeWithFeatures< E extends EdgeWithFeatures< E, V >, V extends Vertex< ? > > extends Edge< V >
{
	public < F extends FeatureValue< ? >, M > F feature( final Feature< M, E, F > feature );
}
