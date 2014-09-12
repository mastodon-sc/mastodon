package net.trackmate.model.abstractmodel;

import java.util.concurrent.ConcurrentLinkedQueue;

import net.trackmate.util.mempool.MappedElement;
import net.trackmate.util.mempool.MemPool;

public class Pool< O extends PoolObject< T >, T extends MappedElement >
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

	MemPool< T > getMemPool()
	{
		return memPool;
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
