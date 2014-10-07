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
 * <p>
 * In principle, this could extend {@link MappedElement}, but we rather use
 * composition to hide {@link MappedElement} methods from users.
 *
 * @param <O>
 *            recursive type of this {@link PoolObject}.
 * @param <T>
 *            the MappedElement type, for example {@link ByteMappedElement}.
 *
 * @author Tobias Pietzsch <tobias.pietzsch@gmail.com>
 */
public abstract class PoolObject< O extends PoolObject< O, T >, T extends MappedElement >
{
	/**
	 * Access to the data.
	 */
	protected final T access;

	/**
	 * Current index (of the access) in the {@link MemPool}.
	 */
	private int index;

	/**
	 * The {@link MemPool} into which this proxy currently refers.
	 */
	private MemPool< T > pool;

	/**
	 * Create a {@link PoolObject} referring data in the given {@link MemPool}.
	 * The element that it references to can be set by
	 * {@link #updateAccess(MemPool, int)}.
	 *
	 * @param pool
	 *            the {@link MemPool} where derived classes store their data.
	 */
	public PoolObject( final MemPool< T > pool )
	{
		this.pool = pool;
		this.access = pool.createAccess();
	}

	/**
	 * Get the element index that this {@link PoolObject} currently refers to.
	 *
	 * @return the element index that this {@link PoolObject} currently refers
	 *         to.
	 */
	public int getInternalPoolIndex()
	{
		return index;
	}

	/**
	 * When creating new elements (see {@link Pool#create(PoolObject)}, they
	 * might reuse storage that was occupied by previously freed elements. This
	 * is overridden by subclasses to set (some, important) fields to their
	 * initial state. An example are indices used to start linked lists in
	 * {@link AbstractEdge}.
	 */
	protected abstract void setToUninitializedState();

	/**
	 * Make this proxy refer the element at the specified {@code index} in the
	 * specified {@code pool}.
	 *
	 * @param pool
	 * @param index
	 */
	void updateAccess( final MemPool< T > pool, final int index )
	{
		this.pool = pool;
		this.index = index;
		pool.updateAccess( access, index );
	}

	/**
	 * Make this proxy refer the element at the specified {@code index} in the
	 * current {@link #pool}.
	 *
	 * @param index
	 */
	// TODO: go through updateAccess( pool, index ) uses an find out, when this one can be used instead.
	void updateAccess( final int index )
	{
		this.index = index;
		pool.updateAccess( access, index );
	}

	/**
	 * Make this {@link PoolObject} refer to the same data as {@code obj}
	 *
	 * @param obj
	 *            A {@link PoolObject}, usually of the same type as this one.
	 */
	@SuppressWarnings( "unchecked" )
	public O refTo( final O obj )
	{
		updateAccess( obj.pool, obj.index );
		return ( O ) this;
	}

	/**
	 * A factory for {@link PoolObject}s of type {@code O}.
	 *
	 * @param <O>
	 *            a {@link PoolObject} type.
	 */
	public static interface Factory< O >
	{
		public int getSizeInBytes();

		// TODO: rename to createRef()?
		public O createEmptyRef();
	}
}
