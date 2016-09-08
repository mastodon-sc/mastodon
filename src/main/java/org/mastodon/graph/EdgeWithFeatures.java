package org.mastodon.graph;

import org.mastodon.features.Feature;
import org.mastodon.features.FeatureValue;

public interface EdgeWithFeatures< E extends EdgeWithFeatures< E, V >, V extends Vertex< ? > > extends Edge< V >
{
	public < F extends FeatureValue< ? >, M > F feature( final Feature< M, E, F > feature );
}
