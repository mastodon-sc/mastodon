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
}
