package net.trackmate.graph;

import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

import net.trackmate.graph.mempool.ByteMappedElement;
import net.trackmate.graph.mempool.MappedElement;
import net.trackmate.graph.mempool.MemPool;
import net.trackmate.graph.mempool.MemPool.PoolIterator;

/**
 * A pool of {@link PoolObject PoolObjects} all stored in a common
 * {@link MemPool}. Provides methods to {@link #createRef() create} and
 * {@link #releaseRef(PoolObject) release} proxy objects.
 * The pool can be {@link #iterator() iterated}.
 *
 * @param <O>
 *            type of {@link PoolObject} stored in this {@link Pool}.
 * @param <T>
 *            the MappedElement type of the {@link PoolObject}, for example
 *            {@link ByteMappedElement}.
 *
 * @author Tobias Pietzsch <tobias.pietzsch@gmail.com>
 */
public class Pool< O extends PoolObject< O, T >, T extends MappedElement > implements RefPool< O >, Iterable< O >
{
	private final PoolObject.Factory< O, T > objFactory;

	private final MemPool< T > memPool;

	private final ConcurrentLinkedQueue< O > tmpObjRefs = new ConcurrentLinkedQueue< O >();

	public Pool(
			final int initialCapacity,
			final PoolObject.Factory< O, T > objFactory )
	{
		this.objFactory = objFactory;
		this.memPool = objFactory.getMemPoolFactory().createPool( initialCapacity, objFactory.getSizeInBytes() );
	}

	/**
	 * Remove all objects from the pool.
	 *
	 * <p>
	 * Note, that existing proxies refer to invalid data after calling this method!
	 */
	public void clear()
	{
		memPool.clear();
	}

	/**
	 * Returns the pool size, that is, how many objects the pool currently
	 * contains.
	 *
	 * @return the pool size.
	 */
	public int size()
	{
		return memPool.size();
	}

	@Override
	public O createRef()
	{
		return createRef( true );
	}

	public O createRef( final boolean recycle )
	{
		if ( recycle )
		{
			final O obj = tmpObjRefs.poll();
			return obj == null ? objFactory.createEmptyRef() : obj;
		}
		else
			return objFactory.createEmptyRef();
	}

	@Override
	public void releaseRef( final O obj )
	{
		tmpObjRefs.add( obj );
	}

	// TODO: find instances where releaseRefs( PoolObject<?> ... objs ) can be used instead of separately releasing refs (Then probably don't use it because it creates an Object array).
	public static void releaseRefs( final PoolObject< ?, ? >... objs )
	{
		for ( final PoolObject< ?, ? > obj : objs )
			obj.releaseRef();
	}

	@Override
	public void getByInternalPoolIndex( final int index, final O obj )
	{
		obj.updateAccess( memPool, index );
	}

	@Override
	public Iterator< O > iterator()
	{
		return iterator( createRef() );
	}

	// garbage-free version
	public Iterator< O > iterator( final O obj )
	{
		final PoolIterator< T > pi = memPool.iterator();
		return new Iterator< O >()
		{
			@Override
			public boolean hasNext()
			{
				return pi.hasNext();
			}

			@Override
			public O next()
			{
				final int index = pi.next();
				obj.updateAccess( memPool, index );
				return obj;
			}

			@Override
			public void remove()
			{
				throw new UnsupportedOperationException();
			}
		};
	}

	protected MemPool< T > getMemPool()
	{
		return memPool;
	}

	protected O create( final O obj )
	{
		final int index = memPool.create();
		obj.updateAccess( memPool, index );
		obj.setToUninitializedState();
		return obj;
	}

	protected void releaseByInternalPoolIndex( final int index )
	{
		memPool.free( index );
	}
}
