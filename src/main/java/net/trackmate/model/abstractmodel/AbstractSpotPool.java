package net.trackmate.model.abstractmodel;

import gnu.trove.impl.Constants;
import gnu.trove.iterator.TIntIntIterator;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;

import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import net.trackmate.util.mempool.MappedElement;
import net.trackmate.util.mempool.MemPool;

public class AbstractSpotPool< S extends AbstractSpot< T, E >, T extends MappedElement, E extends AbstractEdge< ?, ? > > extends Pool< S, T > implements Iterable< S >
{
	private static AtomicInteger IDcounter = new AtomicInteger( -1 );

	private final AbstractSpot.Factory< S, T > spotFactory;

	private final MemPool< T > memPool;

	private final TIntIntMap spotIdToIndexMap;

	private AbstractEdgePool< E, ?, S > edgePool;

	public AbstractSpotPool(
			final int initialCapacity,
			final AbstractSpot.Factory< S, T > spotFactory,
			final MemPool.Factory< T > poolFactory )
	{
		this.spotFactory = spotFactory;
		this.memPool = poolFactory.createPool( initialCapacity, spotFactory.getSpotSizeInBytes() );
		this.spotIdToIndexMap = new TIntIntHashMap( initialCapacity, Constants.DEFAULT_LOAD_FACTOR, -1, -1 );
	}

	public void linkEdgePool( final AbstractEdgePool< E, ?, S > edgePool )
	{
		this.edgePool = edgePool;
	}

	public void clear()
	{
		memPool.clear();
		spotIdToIndexMap.clear();
	}

	public int size()
	{
		return spotIdToIndexMap.size();
	}

	@Override
	public S createEmptyRef()
	{
		final S spot = spotFactory.createEmptySpotRef();
		if ( edgePool != null )
			spot.linkEdgePool( edgePool );
		return spot;
	}

	public S create()
	{
		return create( createEmptyRef() );
	}

	// garbage-free version
	public S create( final S spot )
	{
		createWithId( IDcounter.incrementAndGet(), spot );
		return spot;
	}

	public S create( final int ID )
	{
		return create( ID, createEmptyRef() );
	}

	// garbage-free version
	public S create( final int ID, final S spot )
	{
		while ( IDcounter.get() < ID )
			IDcounter.compareAndSet( IDcounter.get(), ID );
		createWithId( ID, spot );
		return spot;
	}

	public S createReferenceTo( final S spot )
	{
		return createReferenceTo( spot, createEmptyRef() );
	}

	// garbage-free version
	public S createReferenceTo( final S spot, final S reference )
	{
		reference.updateAccess( memPool, spot.getInternalPoolIndex() );
		return reference;
	}

	public S get( final int ID )
	{
		return get( ID, createEmptyRef() );
	}

	// garbage-free version
	public S get( final int ID, final S spot )
	{
		final int index = spotIdToIndexMap.get( ID );
		if ( index == -1 )
			return null;
		getByInternalPoolIndex( index, spot );
		return spot;
	}

	public void release( final S spot )
	{
		if ( edgePool != null )
			edgePool.releaseAllLinkedEdges( spot );
		release( spot.getId() );
	}

	public void release( final int ID )
	{
		final int index = spotIdToIndexMap.remove( ID );
		releaseByInternalPoolIndex( index );
	}

	@Override
	public Iterator< S > iterator()
	{
		return iterator( createEmptyRef() );
	}

	public Iterator< S > iterator( final S spot )
	{
		final TIntIntIterator iter = spotIdToIndexMap.iterator();
		return new Iterator< S >()
		{
			@Override
			public boolean hasNext()
			{
				return iter.hasNext();
			}

			@Override
			public S next()
			{
				iter.advance();
				getByInternalPoolIndex( iter.value(), spot );
				return spot;
			}

			@Override
			public void remove()
			{
				throw new UnsupportedOperationException();
			}
		};
	}

	/*
	 *
	 * Internal stuff.
	 * If it should be necessary for performance reasons, these can be made protected or public
	 *
	 */

	private void createWithId( final int ID, final S spot )
	{
		final int index = memPool.create();
		spot.updateAccess( memPool, index );
		spot.setToUninitializedState();
		spot.setId( ID );
		spotIdToIndexMap.put( ID, index );
	}

	@Override
	MemPool< T > getMemPool()
	{
		return memPool;
	}

	void getByInternalPoolIndex( final int index, final S spot )
	{
		spot.updateAccess( memPool, index );
	}

	private void releaseByInternalPoolIndex( final int index )
	{
		memPool.free( index );
	}

	private final ConcurrentLinkedQueue< S > tmpSpotRefs = new ConcurrentLinkedQueue< S >();

	public S getTmpSpotRef()
	{
		final S spot = tmpSpotRefs.poll();
		return spot == null ? createEmptyRef() : spot;
	}

	public void releaseTmpSpotRef( final S spot )
	{
		tmpSpotRefs.add( spot );
	}
}
