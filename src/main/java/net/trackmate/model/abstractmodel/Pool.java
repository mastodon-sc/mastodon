package net.trackmate.model.abstractmodel;

import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

import net.trackmate.util.mempool.MappedElement;
import net.trackmate.util.mempool.MemPool;
import net.trackmate.util.mempool.MemPool.PoolIterator;

public class Pool< O extends PoolObject< T >, T extends MappedElement > implements Iterable< O >
{
	private final PoolObject.Factory< O > objFactory;

	final MemPool< T > memPool;

	private final ConcurrentLinkedQueue< O > tmpObjRefs = new ConcurrentLinkedQueue< O >();

	public Pool(
			final int initialCapacity,
			final PoolObject.Factory< O > objFactory,
			final MemPool.Factory< T > poolFactory )
	{
		this.objFactory = objFactory;
		this.memPool = poolFactory.createPool( initialCapacity, objFactory.getSizeInBytes() );
	}

	public void clear()
	{
		memPool.clear();
	}

	public int size()
	{
		return memPool.size();
	}

	public O createEmptyRef()
	{
		return objFactory.createEmptyRef();
	}

	public O getTmpRef()
	{
		final O obj = tmpObjRefs.poll();
		return obj == null ? createEmptyRef() : obj;
	}

	public void releaseTmpRef( final O obj )
	{
		tmpObjRefs.add( obj );
	}

	public O createReferenceTo( final O obj )
	{
		return createReferenceTo( obj, createEmptyRef() );
	}

	// garbage-free version
	public O createReferenceTo( final O obj, final O reference )
	{
		reference.updateAccess( memPool, obj.getInternalPoolIndex() );
		return reference;
	}

	@Override
	public Iterator< O > iterator()
	{
		return iterator( createEmptyRef() );
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

	MemPool< T > getMemPool()
	{
		return memPool;
	}

	O create( final O obj )
	{
		final int index = memPool.create();
		obj.updateAccess( memPool, index );
		obj.setToUninitializedState();
		return obj;
	}

	void getByInternalPoolIndex( final int index, final O obj )
	{
		obj.updateAccess( memPool, index );
	}

	void releaseByInternalPoolIndex( final int index )
	{
		memPool.free( index );
	}
}
