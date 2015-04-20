package net.trackmate.graph;

import gnu.trove.TIntCollection;
import gnu.trove.function.TIntFunction;
import gnu.trove.impl.Constants;
import gnu.trove.iterator.TIntIntIterator;
import gnu.trove.iterator.TObjectIntIterator;
import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.procedure.TIntProcedure;
import gnu.trove.procedure.TObjectIntProcedure;
import gnu.trove.procedure.TObjectProcedure;

import java.util.Map;
import java.util.Set;

import net.trackmate.graph.collection.RefIntMap;
import net.trackmate.graph.mempool.MappedElement;
import net.trackmate.graph.mempool.MemPool;

public class PoolObjectIntMap< K extends PoolObject< K, T >, T extends MappedElement > implements RefIntMap< K >
{

	private static final int NO_ENTRY_KEY = -1;

	private static final float DEFAULT_LOAD_FACTOR = Constants.DEFAULT_LOAD_FACTOR;

	private final TIntIntHashMap indexmap;

	private final Pool< K, T > pool;

	/*
	 * CONSTRUCTORS
	 */

	public PoolObjectIntMap( final Pool< K, T > pool, final int noEntryValue, final int initialCapacity )
	{
		this.pool = pool;
		this.indexmap = new TIntIntHashMap( initialCapacity, DEFAULT_LOAD_FACTOR, NO_ENTRY_KEY, noEntryValue );
	}

	public PoolObjectIntMap( final Pool< K, T > pool, final int noEntryValue )
	{
		this( pool, noEntryValue, Constants.DEFAULT_CAPACITY );
	}

	/*
	 * METHODS
	 */

	@Override
	public void clear()
	{
		indexmap.clear();
	}

	@SuppressWarnings( "unchecked" )
	@Override
	public boolean containsKey( final Object key )
	{
		if ( key != null && key instanceof PoolObject )
			return indexmap.containsKey( ( ( K ) key ).getInternalPoolIndex() );
		else
			return false;
	}

	@Override
	public boolean containsValue( final int value )
	{
		return indexmap.containsValue( value );
	}

	@SuppressWarnings( "unchecked" )
	@Override
	public int get( final Object key )
	{
		if ( key != null && key instanceof PoolObject )
			return indexmap.get( ( ( K ) key ).getInternalPoolIndex() );
		else
			return indexmap.getNoEntryValue();
	}

	@Override
	public boolean isEmpty()
	{
		return indexmap.isEmpty();
	}

	@Override
	public Set< K > keySet()
	{
		return new PoolObjectSet< K, T >( pool, indexmap.keySet() );
	}

	@Override
	public int put( final K key, final int value )
	{
		return indexmap.put( key.getInternalPoolIndex(), value );
	}

	@Override
	public int putIfAbsent( final K key, final int value )
	{
		return indexmap.putIfAbsent( key.getInternalPoolIndex(), value );
	}

	@Override
	public void putAll( final Map< ? extends K, ? extends Integer > map )
	{
		for ( final Map.Entry< ? extends K, ? extends Integer > entry : map.entrySet() )
		{
            indexmap.put( entry.getKey().getInternalPoolIndex(), entry.getValue().intValue() );
		}
	}

	@SuppressWarnings( "unchecked" )
	@Override
	public int remove( final Object key )
	{
		if ( key != null && key instanceof PoolObject )
			return indexmap.remove( ( ( K ) key ).getInternalPoolIndex() );
		else
			return indexmap.getNoEntryValue();
	}

	@Override
	public int getNoEntryValue()
	{
		return indexmap.getNoEntryValue();
	}

	@Override
	public int size()
	{
		return indexmap.size();
	}

	@Override
	public int[] values()
	{
		return indexmap.values();
	}

	@Override
	public int[] values( final int[] array )
	{
		return indexmap.values( array );
	}

	@Override
	public TIntCollection valueCollection()
	{
		return indexmap.valueCollection();
	}

	@Override
	public void putAll( final TObjectIntMap< ? extends K > map )
	{
		for ( final K key : map.keySet() )
		{
			put( key, map.get( key ) );
		}
	}

	@Override
	public Object[] keys()
	{
		/*
		 * This is just the method we would like people not to call. But hey,
		 * it's there.
		 */
		return keySet().toArray();
	}

	@Override
	public K[] keys( final K[] array )
	{
		return keySet().toArray( array );
	}

	@Override
	public TObjectIntIterator< K > iterator()
	{
		return new PoolObjectIntIterator();
	}

	@Override
	public boolean increment( final K key )
	{
		return indexmap.increment( key.getInternalPoolIndex() );
	}

	@Override
	public boolean adjustValue( final K key, final int amount )
	{
		return indexmap.adjustValue( key.getInternalPoolIndex(), amount );
	}

	@Override
	public int adjustOrPutValue( final K key, final int adjust_amount, final int put_amount )
	{
		return indexmap.adjustOrPutValue( key.getInternalPoolIndex(), adjust_amount, put_amount );
	}

	@Override
	public boolean forEachKey( final TObjectProcedure< ? super K > procedure, final K ref )
	{
		final MemPool< T > memPool = pool.getMemPool();
		for ( final int id : indexmap.keys() )
		{
			ref.updateAccess( memPool, id );
			if ( !procedure.execute( ref ) ) { return false; }
		}
		return true;
	}

	@Override
	public boolean forEachKey( final TObjectProcedure< ? super K > procedure )
	{
		final K ref = createRef();
		return forEachKey( procedure, ref );
	}

	@Override
	public boolean forEachValue( final TIntProcedure procedure )
	{
		return indexmap.forEachValue( procedure );
	}

	@Override
	public boolean forEachEntry( final TObjectIntProcedure< ? super K > procedure, final K ref )
	{
		final MemPool< T > memPool = pool.getMemPool();
		for ( final int id : indexmap.keys() )
		{
			ref.updateAccess( memPool, id );
			if ( !procedure.execute( ref, indexmap.get( ref.getInternalPoolIndex() ) ) ) { return false; }
		}
		return true;
	}

	@Override
	public boolean forEachEntry( final TObjectIntProcedure< ? super K > procedure )
	{
		final K ref = createRef();
		return forEachEntry( procedure, ref );
	}

	@Override
	public void transformValues( final TIntFunction function )
	{
		indexmap.transformValues( function );
	}

	@Override
	public boolean retainEntries( final TObjectIntProcedure< ? super K > procedure, final K ref )
	{
		boolean modified = false;
		final MemPool< T > memPool = pool.getMemPool();
		for ( final int id : indexmap.keys() )
		{
			ref.updateAccess( memPool, id );
			if ( !procedure.execute( ref, indexmap.get( ref.getInternalPoolIndex() ) ) )
			{
				remove( ref );
				modified = true;
			}
		}
		return modified;
	}

	@Override
	public boolean retainEntries( final TObjectIntProcedure< ? super K > procedure )
	{
		final K ref = createRef();
		return retainEntries( procedure, ref );
	}

	@Override
	public K createRef()
	{
		return pool.createRef();
	}

	@Override
	public void releaseRef( final K obj )
	{
		pool.releaseRef( obj );
	}

	/*
	 * INNER CLASSES
	 */

	private class PoolObjectIntIterator implements TObjectIntIterator< K >
	{

		private final TIntIntIterator it;

		private final K obj;

		private final MemPool< T > memPool;

		public PoolObjectIntIterator()
		{
			this.it = indexmap.iterator();
			this.obj = createRef();
			this.memPool = pool.getMemPool();
		}

		@Override
		public void advance()
		{
			it.advance();
		}

		@Override
		public boolean hasNext()
		{
			return it.hasNext();
		}

		@Override
		public void remove()
		{
			it.remove();
		}

		@Override
		public K key()
		{
			final int id = it.key();
			obj.updateAccess( memPool, id );
			return obj;
		}

		@Override
		public int value()
		{
			return it.value();
		}

		@Override
		public int setValue( final int val )
		{
			return it.setValue( val );
		}
	}
}
