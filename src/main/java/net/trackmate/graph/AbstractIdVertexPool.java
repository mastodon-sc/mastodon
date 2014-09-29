package net.trackmate.graph;

import gnu.trove.impl.Constants;
import gnu.trove.iterator.TIntIntIterator;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

import net.trackmate.graph.mempool.MappedElement;
import net.trackmate.graph.mempool.MemPool;

public class AbstractIdVertexPool< V extends AbstractIdVertex< T, E >, T extends MappedElement, E extends AbstractEdge< ?, ? > > extends AbstractVertexPool< V, T, E >
{
	private static AtomicInteger IDcounter = new AtomicInteger( -1 );

	private final TIntIntMap vertexIdToIndexMap;

	public AbstractIdVertexPool(
			final int initialCapacity,
			final PoolObject.Factory< V > vertexFactory,
			final MemPool.Factory< T > poolFactory )
	{
		super( initialCapacity, vertexFactory, poolFactory );
		this.vertexIdToIndexMap = new TIntIntHashMap( initialCapacity, Constants.DEFAULT_LOAD_FACTOR, -1, -1 );
	}

	@Override
	public void clear()
	{
		super.clear();
		vertexIdToIndexMap.clear();
	}

	// garbage-free version
	@Override
	public V create( final V vertex )
	{
		createWithId( IDcounter.incrementAndGet(), vertex );
		return vertex;
	}

	public V create( final int ID )
	{
		return create( ID, createRef() );
	}

	// garbage-free version
	public V create( final int ID, final V vertex )
	{
		while ( IDcounter.get() < ID )
			IDcounter.compareAndSet( IDcounter.get(), ID );
		createWithId( ID, vertex );
		return vertex;
	}

	public V get( final int ID )
	{
		return get( ID, createRef() );
	}

	// garbage-free version
	public V get( final int ID, final V vertex )
	{
		final int index = vertexIdToIndexMap.get( ID );
		if ( index == -1 )
			return null;
		getByInternalPoolIndex( index, vertex );
		return vertex;
	}

	@Override
	public void release( final V vertex )
	{
		vertexIdToIndexMap.remove( vertex.getId() );
		super.release( vertex );
	}

	public void release( final int ID )
	{
		final int index = vertexIdToIndexMap.remove( ID );
		final V vertex = createRef();
		getByInternalPoolIndex( index, vertex );
		super.release( vertex );
		releaseRef( vertex );
	}

	@Override
	public Iterator< V > iterator()
	{
		return iterator( createRef() );
	}

	@Override
	public Iterator< V > iterator( final V vertex )
	{
		final TIntIntIterator iter = vertexIdToIndexMap.iterator();
		return new Iterator< V >()
		{
			@Override
			public boolean hasNext()
			{
				return iter.hasNext();
			}

			@Override
			public V next()
			{
				iter.advance();
				getByInternalPoolIndex( iter.value(), vertex );
				return vertex;
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

	private void createWithId( final int ID, final V vertex )
	{
		super.create( vertex );
		vertex.setId( ID );
		vertexIdToIndexMap.put( ID, vertex.getInternalPoolIndex() );
	}
}
