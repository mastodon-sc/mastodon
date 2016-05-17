package net.trackmate.graph.zzgraphinterfaces;

public interface VertexWithFeatures< V extends VertexWithFeatures< V, E >, E extends Edge< ? > > extends Vertex< E >
{
	public < F extends FeatureValue< ? >, M > F feature( final VertexFeature< M, V, F > feature );
}
