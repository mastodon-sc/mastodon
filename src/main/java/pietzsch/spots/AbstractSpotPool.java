package pietzsch.spots;

import gnu.trove.impl.Constants;
import gnu.trove.iterator.TIntLongIterator;
import gnu.trove.map.TIntLongMap;
import gnu.trove.map.hash.TIntLongHashMap;

import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import pietzsch.mappedelementpool.MappedElement;
import pietzsch.mappedelementpool.Pool;

public class AbstractSpotPool< S extends AbstractSpot< T, E >, T extends MappedElement, E extends AbstractEdge< ? > > implements Iterable< S >
{
	private static AtomicInteger IDcounter = new AtomicInteger( -1 );

	final Pool< T > memPool;

	private final AbstractSpot.Factory< S, T > spotFactory;

	private final TIntLongMap spotIdToIndexMap;

	private AbstractEdgePool< E, ? > edgePool;

	public AbstractSpotPool(
			final int initialCapacity,
			final AbstractSpot.Factory< S, T > spotFactory,
			final Pool.Factory< T > poolFactory )
	{
		this.spotFactory = spotFactory;
		this.memPool = poolFactory.createPool( initialCapacity, spotFactory.getSpotSizeInBytes() );
		// TODO: synchronize with a ReadWriteLock to allow multiple threads to get()
		this.spotIdToIndexMap = new TIntLongHashMap( initialCapacity, Constants.DEFAULT_LOAD_FACTOR, -1, -1 );
	}

	public void linkEdgePool( final AbstractEdgePool< E, ? > edgePool )
	{
		this.edgePool = edgePool;
	}

	public void clear()
	{
		memPool.clear();
		spotIdToIndexMap.clear();
	}

	public S createEmptySpotRef()
	{
		final S spot = spotFactory.createEmptySpotRef( this );
		if ( edgePool != null )
			spot.linkEdgePool( edgePool );
		return spot;
	}

	public S create()
	{
		return create( createEmptySpotRef() );
	}

	// garbage-free version
	public S create( final S spot )
	{
		createWithId( IDcounter.incrementAndGet(), spot );
		return spot;
	}

	public S create( final int ID )
	{
		return create( ID, createEmptySpotRef() );
	}

	// garbage-free version
	public S create( final int ID, final S spot )
	{
		while ( IDcounter.get() < ID )
			IDcounter.compareAndSet( IDcounter.get(), ID );
		createWithId( ID, spot );
		return spot;
	}

	public S get( final int ID )
	{
		return get( ID, createEmptySpotRef() );
	}

	// garbage-free version
	public S get( final int ID, final S spot )
	{
		final long index = spotIdToIndexMap.get( ID );
		if ( index == -1 )
			return null;
		getByInternalPoolIndex( index, spot );
		return spot;
	}

	public void release( final S spot )
	{
		release( spot.getId() );
	}

	public void release( final int ID )
	{
		final long index = spotIdToIndexMap.remove( ID );
		releaseByInternalPoolIndex( index );
	}

	@Override
	public Iterator< S > iterator()
	{
		return iterator( createEmptySpotRef() );
	}

	public Iterator< S > iterator( final S spot )
	{
		final TIntLongIterator iter = spotIdToIndexMap.iterator();
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
		final long index = memPool.create();
		spot.updateAccess( memPool, index );
		spot.init();
		spot.setId( ID );
		spotIdToIndexMap.put( ID, index );
	}

	// TODO: make package private. this is just for debugging
	public void getByInternalPoolIndex( final long index, final S spot )
	{
		spot.updateAccess( memPool, index );
	}

	private void releaseByInternalPoolIndex( final long index )
	{
		memPool.free( index );
	}

	private final ConcurrentLinkedQueue< S > tmpSpotRefs = new ConcurrentLinkedQueue< S >();

	public S getTmpSpotRef()
	{
		final S spot = tmpSpotRefs.poll();
		return spot == null ? createEmptySpotRef() : spot;
	}

	public void releaseTmpSpotRef( final S spot )
	{
		tmpSpotRefs.add( spot );
	}
}
