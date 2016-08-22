package net.trackmate.revised.model;

import net.imglib2.EuclideanSpace;
import net.trackmate.graph.ref.AbstractListenableEdge;
import net.trackmate.graph.ref.AbstractListenableVertexPool;
import net.trackmate.pool.MappedElement;
import net.trackmate.pool.PoolObject;

public class AbstractSpotPool<
			V extends AbstractSpot< V, E, T, G >,
			E extends AbstractListenableEdge< E, V, T >,
			T extends MappedElement,
			G extends AbstractModelGraph< ?, ?, ?, V, E, T > >
		extends AbstractListenableVertexPool< V, E, T > implements EuclideanSpace
{
	public AbstractSpotPool(
			final int numDimensions,
			final int initialCapacity,
			final PoolObject.Factory< V, T > vertexFactory )
	{
		super( initialCapacity, vertexFactory );
		this.numDimensions = numDimensions;
	}

	private final int numDimensions;

	private G modelGraph;

	public void linkModelGraph( final G modelGraph )
	{
		this.modelGraph = modelGraph;
	}

	@Override
	public V createRef()
	{
		final V vertex = super.createRef();
		vertex.modelGraph = modelGraph;
		return vertex;
	}

	@Override
	public int numDimensions()
	{
		return numDimensions;
	}

	/*
	 * Debug helper. Uncomment to do additional verifyInitialized() whenever a
	 * Ref is pointed to a vertex.
	 */
//	@Override
//	public V getObject( final int index, final V obj )
//	{
//		final V v = super.getObject( index, obj );
//		v.verifyInitialized();
//		return v;
//	}
}
