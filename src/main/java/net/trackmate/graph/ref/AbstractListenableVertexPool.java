package net.trackmate.graph.ref;

import net.trackmate.pool.MappedElement;
import net.trackmate.pool.PoolObject;

public class AbstractListenableVertexPool<
			V extends AbstractListenableVertex< V, E, T >,
			E extends AbstractEdge< E, ?, ? >,
			T extends MappedElement >
		extends AbstractVertexWithFeaturesPool< V, E, T >
{
	public AbstractListenableVertexPool(
			final int initialCapacity,
			final PoolObject.Factory< V, T > vertexFactory )
	{
		super( initialCapacity, vertexFactory );
	}

	private NotifyPostInit< V, ? > notifyPostInit;

	public void linkNotify( final NotifyPostInit< V, ? > notifyPostInit )
	{
		this.notifyPostInit = notifyPostInit;
	}

	@Override
	public V createRef()
	{
		final V vertex = super.createRef();
		vertex.notifyPostInit = notifyPostInit;
		return vertex;
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
