package net.trackmate.graph;

import net.trackmate.graph.features.Feature;

public interface EdgeWithFeatures< E extends EdgeWithFeatures< E, V >, V extends Vertex< ? > > extends Edge< V >
{
	public < F extends FeatureValue< ? >, M > F feature( final Feature< M, E, F > feature );
}
