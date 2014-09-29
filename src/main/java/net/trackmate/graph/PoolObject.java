package net.trackmate.graph;

import net.trackmate.graph.mempool.ByteMappedElement;
import net.trackmate.graph.mempool.MappedElement;
import net.trackmate.graph.mempool.MemPool;

/**
 * A proxy object that uses a {@link MappedElement} access to store its data in
 * a {@link MemPool}. The data block that it references to can be set by
 * {@link #updateAccess(MemPool, int)}. Methods to modify the data itself are
 * defined in subclasses.
 *
 * @param <T>
 *            the MappedElement type, for example {@link ByteMappedElement}.
 *
 * @author Tobias Pietzsch <tobias.pietzsch@gmail.com>
 */
public abstract class PoolObject< T extends MappedElement >
{
	/**
	 * Access to the data.
	 */
	protected final T access;

	/**
	 * Current index of (of the access) in the {@link MemPool}.
	 */
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
