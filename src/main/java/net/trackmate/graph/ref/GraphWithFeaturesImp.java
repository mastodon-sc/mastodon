package net.trackmate.graph.ref;

import net.trackmate.graph.GraphFeatures;
import net.trackmate.pool.MappedElement;

public class GraphWithFeaturesImp<
		VP extends AbstractVertexWithFeaturesPool< V, E, T >,
		EP extends AbstractEdgePool< E, V, T >,
		V extends AbstractVertexWithFeatures< V, E, T >,
		E extends AbstractEdge< E, V, T >,
		T extends MappedElement >
	extends GraphImp< VP, EP, V, E, T >
{
	protected final GraphFeatures< V, E > features;

	public GraphWithFeaturesImp( final VP vertexPool, final EP edgePool )
	{
		super( vertexPool, edgePool );
		features = new GraphFeatures<>( this );
		vertexPool.linkFeatures( features );
	}

	public GraphWithFeaturesImp( final EP edgePool )
	{
		super( edgePool );
		features = new GraphFeatures<>( this );
		vertexPool.linkFeatures( features );
	}

	@Override
	protected void clear()
	{
		super.clear();
		features.clear();
	}
}
