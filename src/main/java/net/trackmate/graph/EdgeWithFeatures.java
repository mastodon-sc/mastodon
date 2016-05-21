package net.trackmate.graph;

public interface EdgeWithFeatures< E extends EdgeWithFeatures< E, V >, V extends Vertex< ? > > extends Edge< V >
{
	public < F extends FeatureValue< ? >, M > F feature( final EdgeFeature< M, E, F > feature );
}
