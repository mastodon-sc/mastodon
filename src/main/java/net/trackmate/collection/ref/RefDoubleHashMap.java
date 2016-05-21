package net.trackmate.collection.ref;

import java.util.Map;
import java.util.Set;

import gnu.trove.TDoubleCollection;
import gnu.trove.function.TDoubleFunction;
import gnu.trove.impl.Constants;
import gnu.trove.iterator.TIntDoubleIterator;
import gnu.trove.iterator.TObjectDoubleIterator;
import gnu.trove.map.TObjectDoubleMap;
import gnu.trove.map.hash.TIntDoubleHashMap;
import gnu.trove.procedure.TDoubleProcedure;
import gnu.trove.procedure.TObjectDoubleProcedure;
import gnu.trove.procedure.TObjectProcedure;
import net.trackmate.RefPool;
import net.trackmate.collection.RefDoubleMap;

public class RefDoubleHashMap< K > implements RefDoubleMap< K >
{
	private static final int NO_ENTRY_KEY = -1;

	private static final float DEFAULT_LOAD_FACTOR = Constants.DEFAULT_LOAD_FACTOR;

	private final TIntDoubleHashMap indexmap;

	private final RefPool< K > pool;

	private final Class< K > keyType;

	/*
	 * CONSTRUCTORS
	 */

	public RefDoubleHashMap( final RefPool< K > pool, final double noEntryValue, final int initialCapacity )
	{
		this.pool = pool;
		this.keyType = pool.getRefClass();
		this.indexmap = new TIntDoubleHashMap( initialCapacity, DEFAULT_LOAD_FACTOR, NO_ENTRY_KEY, noEntryValue );
	}

	public RefDoubleHashMap( final RefPool< K > pool, final double noEntryValue )
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
		if ( keyType.isInstance( key ) )
			return indexmap.containsKey( pool.getId( ( K ) key ) );
		else
			return false;
	}

	@Override
	public boolean containsValue( final double value )
	{
		return indexmap.containsValue( value );
	}

	@SuppressWarnings( "unchecked" )
	@Override
	public double get( final Object key )
	{
		if ( keyType.isInstance( key ) )
			return indexmap.get( pool.getId( ( K ) key ) );
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
		return new RefSetImp< K >( pool, indexmap.keySet() );
	}

	@Override
	public double put( final K key, final double value )
	{
		return indexmap.put( pool.getId( key ), value );
	}

	@Override
	public double putIfAbsent( final K key, final double value )
	{
		return indexmap.putIfAbsent( pool.getId( key ), value );
	}

	@Override
	public void putAll( final Map< ? extends K, ? extends Double > map )
	{
		for ( final Map.Entry< ? extends K, ? extends Double > entry : map.entrySet() )
		{
            indexmap.put( pool.getId( entry.getKey() ), entry.getValue().intValue() );
		}
	}

	@SuppressWarnings( "unchecked" )
	@Override
	public double remove( final Object key )
	{
		if ( keyType.isInstance( key ) )
			return indexmap.remove( pool.getId( ( K ) key ) );
		else
			return indexmap.getNoEntryValue();
	}

	@Override
	public double getNoEntryValue()
	{
		return indexmap.getNoEntryValue();
	}

	@Override
	public int size()
	{
		return indexmap.size();
	}

	@Override
	public double[] values()
	{
		return indexmap.values();
	}

	@Override
	public double[] values( final double[] array )
	{
		return indexmap.values( array );
	}

	@Override
	public TDoubleCollection valueCollection()
	{
		return indexmap.valueCollection();
	}

	@Override
	public void putAll( final TObjectDoubleMap< ? extends K > map )
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
	public TObjectDoubleIterator< K > iterator()
	{
		return new RefDoubleIterator();
	}

	@Override
	public boolean increment( final K key )
	{
		return indexmap.increment( pool.getId( key ) );
	}

	@Override
	public boolean adjustValue( final K key, final double amount )
	{
		return indexmap.adjustValue( pool.getId( key ), amount );
	}

	@Override
	public double adjustOrPutValue( final K key, final double adjust_amount, final double put_amount )
	{
		return indexmap.adjustOrPutValue( pool.getId( key ), adjust_amount, put_amount );
	}

	@Override
	public boolean forEachKey( final TObjectProcedure< ? super K > procedure, final K ref )
	{
		for ( final int id : indexmap.keys() )
		{
			final K key = pool.getObject( id, ref );
			if ( !procedure.execute( key ) )
				return false;
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
	public boolean forEachValue( final TDoubleProcedure procedure )
	{
		return indexmap.forEachValue( procedure );
	}

	@Override
	public boolean forEachEntry( final TObjectDoubleProcedure< ? super K > procedure, final K ref )
	{
		for ( final int id : indexmap.keys() )
		{
			final K key = pool.getObject( id, ref );
			final double value = indexmap.get( id );
			if ( !procedure.execute( key, value ) )
				return false;
		}
		return true;
	}

	@Override
	public boolean forEachEntry( final TObjectDoubleProcedure< ? super K > procedure )
	{
		final K ref = createRef();
		return forEachEntry( procedure, ref );
	}

	@Override
	public void transformValues( final TDoubleFunction function )
	{
		indexmap.transformValues( function );
	}

	@Override
	public boolean retainEntries( final TObjectDoubleProcedure< ? super K > procedure, final K ref )
	{
		boolean modified = false;
		for ( final int id : indexmap.keys() )
		{
			final K key = pool.getObject( id, ref );
			final double value = indexmap.get( id );
			if ( !procedure.execute( key, value ) )
			{
				remove( ref );
				modified = true;
			}
		}
		return modified;
	}

	@Override
	public boolean retainEntries( final TObjectDoubleProcedure< ? super K > procedure )
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

	private class RefDoubleIterator implements TObjectDoubleIterator< K >
	{

		private final TIntDoubleIterator it;

		private final K obj;

		public RefDoubleIterator()
		{
			this.it = indexmap.iterator();
			this.obj = createRef();
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
			return pool.getObject( id, obj );
		}

		@Override
		public double value()
		{
			return it.value();
		}

		@Override
		public double setValue( final double val )
		{
			return it.setValue( val );
		}
	}
}
