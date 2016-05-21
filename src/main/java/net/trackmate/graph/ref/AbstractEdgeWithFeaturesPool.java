package net.trackmate.graph.ref;

import net.trackmate.graph.GraphFeatures;
import net.trackmate.pool.MappedElement;
import net.trackmate.pool.PoolObject;

public class AbstractEdgeWithFeaturesPool<
 			E extends AbstractEdgeWithFeatures< E, V, T >,
 			V extends AbstractVertex< V, ?, ? >,
			T extends MappedElement >
		extends AbstractEdgePool< E, V, T >
{
	private GraphFeatures< ?, E > features;

	public AbstractEdgeWithFeaturesPool(
			final int initialCapacity,
			final PoolObject.Factory< E, T > edgeFactory,
			final AbstractVertexPool< V, ?, ? > vertexPool )
	{
		super( initialCapacity, edgeFactory, vertexPool );
	}

	public void linkFeatures( final GraphFeatures< ?, E > features )
	{
		this.features = features;
	}

	@Override
	public E createRef()
	{
		final E edge = super.createRef();
		edge.features = features;
		return edge;
	}

	@Override
	public void delete( final E edge )
	{
		edge.features.delete( edge );
		super.delete( edge );
	}
}
