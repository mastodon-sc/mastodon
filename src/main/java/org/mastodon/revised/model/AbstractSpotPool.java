package org.mastodon.revised.model;

import org.mastodon.graph.ref.AbstractListenableEdge;
import org.mastodon.graph.ref.AbstractListenableVertexPool;
import org.mastodon.pool.MappedElement;
import org.mastodon.pool.MemPool;
import org.mastodon.pool.attributes.IntAttribute;
import org.mastodon.pool.attributes.RealPointAttribute;

import net.imglib2.EuclideanSpace;

public abstract class AbstractSpotPool<
			V extends AbstractSpot< V, E, ?, T, G >,
			E extends AbstractListenableEdge< E, V, ?, T >,
			T extends MappedElement,
			G extends AbstractModelGraph< ?, ?, ?, V, E, T > >
		extends AbstractListenableVertexPool< V, E, T > implements EuclideanSpace
{
	public static class AbstractSpotLayout extends AbstractVertexLayout
	{
		final DoubleArrayField position;
		final IntField timepoint;

		public AbstractSpotLayout( final int numDimensions )
		{
			position = doubleArrayField( numDimensions );
			timepoint = intField();
		}
	}

	final AbstractSpotLayout layout;

	G modelGraph;

	protected final RealPointAttribute< V > position;

	protected final IntAttribute< V > timepoint;

	public AbstractSpotPool(
			final int initialCapacity,
			final AbstractSpotLayout layout,
			final Class< V > vertexClass,
			final MemPool.Factory< T > memPoolFactory )
	{
		super( initialCapacity, layout, vertexClass, memPoolFactory );
		this.layout = layout;
		position = new RealPointAttribute<>( layout.position );
		timepoint = new IntAttribute<>( layout.timepoint );
	}

	public void linkModelGraph( final G modelGraph )
	{
		this.modelGraph = modelGraph;
	}

	@Override
	public int numDimensions()
	{
		return layout.position.numElements();
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
