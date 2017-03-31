package org.mastodon.revised.model;

import org.mastodon.graph.ref.AbstractListenableEdge;
import org.mastodon.graph.ref.AbstractListenableVertexPool;
import org.mastodon.pool.MappedElement;
import org.mastodon.pool.PoolObject;

import net.imglib2.EuclideanSpace;

public class AbstractSpotPool<
			V extends AbstractSpot< V, E, ?, T, G >,
			E extends AbstractListenableEdge< E, V, ?, T >,
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

	G modelGraph;

	public void linkModelGraph( final G modelGraph )
	{
		this.modelGraph = modelGraph;
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
