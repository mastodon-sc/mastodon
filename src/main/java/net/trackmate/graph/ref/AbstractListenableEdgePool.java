package net.trackmate.graph.ref;

import net.trackmate.pool.MappedElement;
import net.trackmate.pool.PoolObject;

public class AbstractListenableEdgePool<
			E extends AbstractListenableEdge< E, V, T >,
			V extends AbstractVertex< V, ?, ? >,
			T extends MappedElement >
		extends AbstractEdgeWithFeaturesPool< E, V, T >
{
	public AbstractListenableEdgePool(
			final int initialCapacity,
			final PoolObject.Factory< E, T > edgeFactory,
			final AbstractVertexPool< V, ?, ? > vertexPool )
	{
		super( initialCapacity, edgeFactory, vertexPool );
	}

	private NotifyPostInit< ?, E > notifyPostInit;

	public void linkNotify( final NotifyPostInit< ?, E > notifyPostInit )
	{
		this.notifyPostInit = notifyPostInit;
	}

	@Override
	public E createRef()
	{
		final E edge = super.createRef();
		edge.notifyPostInit = notifyPostInit;
		return edge;
	}

	/*
	 * Debug helper. Uncomment to do additional verifyInitialized() whenever a
	 * Ref is pointed to an edge.
	 */
//	@Override
//	public E getObject( final int index, final E obj )
//	{
//		final E e = super.getObject( index, obj );
//		e.verifyInitialized();
//		return e;
//	}
}
