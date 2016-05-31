package net.trackmate.graph;

import net.trackmate.graph.features.unify.Feature;

public interface VertexWithFeatures< V extends VertexWithFeatures< V, E >, E extends Edge< ? > > extends Vertex< E >
{
	public < F extends FeatureValue< ? >, M > F feature( final Feature< M, V, F > feature );
}
