package org.mastodon.graph.ref;

import org.mastodon.features.Features;
import org.mastodon.pool.MappedElement;

public class GraphWithFeaturesImp<
		VP extends AbstractVertexWithFeaturesPool< V, E, T >,
		EP extends AbstractEdgeWithFeaturesPool< E, V, T >,
		V extends AbstractVertexWithFeatures< V, E, T >,
		E extends AbstractEdgeWithFeatures< E, V, T >,
		T extends MappedElement >
	extends GraphImp< VP, EP, V, E, T >
{
	protected final Features< V > vertexFeatures;

	protected final Features< E > edgeFeatures;

	public GraphWithFeaturesImp( final VP vertexPool, final EP edgePool )
	{
		super( vertexPool, edgePool );
		vertexFeatures = new Features<>( vertices() );
		edgeFeatures = new Features<>( edges() );
		vertexPool.linkFeatures( vertexFeatures );
		edgePool.linkFeatures( edgeFeatures );
	}

	public GraphWithFeaturesImp( final EP edgePool )
	{
		super( edgePool );
		vertexFeatures = new Features<>( vertices() );
		edgeFeatures = new Features<>( edges() );
		vertexPool.linkFeatures( vertexFeatures );
		edgePool.linkFeatures( edgeFeatures );
	}

	@Override
	protected void clear()
	{
		super.clear();
		vertexFeatures.clear();
		edgeFeatures.clear();
	}
}
