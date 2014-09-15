package net.trackmate.graph;

import net.trackmate.graph.mempool.MappedElement;
import net.trackmate.graph.mempool.MemPool;


public abstract class PoolObject< T extends MappedElement >
{
	protected final T access;

	private int index;

	public PoolObject( final MemPool< T > pool )
	{
		this.access = pool.createAccess();
	}

	public int getInternalPoolIndex()
	{
		return index;
	}

	protected abstract void setToUninitializedState();

	void updateAccess( final MemPool< T > pool, final int index )
	{
		this.index = index;
		pool.updateAccess( access, index );
	}

	public static interface Factory< O >
	{
		public int getSizeInBytes();

		public O createEmptyRef();
	}
}
