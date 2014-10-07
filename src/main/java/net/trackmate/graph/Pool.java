package net.trackmate.graph;

import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

import net.trackmate.graph.mempool.MappedElement;
import net.trackmate.graph.mempool.MemPool;
import net.trackmate.graph.mempool.MemPool.PoolIterator;

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

	public void releaseRef( final O obj )
	{
		tmpObjRefs.add( obj );
	}

	// TODO: add releaseRefs( PoolObject<?> ... objs )
	// This requires that each PoolObject knows its pool so that it can release itself.
	// This in turn requires adding generic parameter PoolObject< T, O extends PoolObject< T, O > >

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

	protected void getByInternalPoolIndex( final int index, final O obj )
	{
		obj.updateAccess( memPool, index );
	}

	protected void releaseByInternalPoolIndex( final int index )
	{
		memPool.free( index );
	}
}
