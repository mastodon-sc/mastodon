package net.trackmate.graph.ref;

import net.trackmate.graph.features.Features;
import net.trackmate.pool.MappedElement;
import net.trackmate.pool.PoolObject;

public class AbstractVertexWithFeaturesPool<
			V extends AbstractVertexWithFeatures< V, E, T >,
			E extends AbstractEdge< E, ?, ? >,
			T extends MappedElement >
		extends AbstractVertexPool< V, E, T >
{
	private Features< V > features;

	public AbstractVertexWithFeaturesPool(
			final int initialCapacity,
			final PoolObject.Factory< V, T > vertexFactory )
	{
		super( initialCapacity, vertexFactory );
	}

	public void linkFeatures( final Features< V > features )
	{
		this.features = features;
	}

	@Override
	public V createRef()
	{
		final V vertex = super.createRef();
		vertex.features = features;
		return vertex;
	}

	@Override
	public void delete( final V vertex )
	{
		vertex.features.delete( vertex );
		super.delete( vertex );
	}
}
