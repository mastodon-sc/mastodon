package net.trackmate.collection.ref;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import gnu.trove.function.TObjectFunction;
import gnu.trove.impl.Constants;
import gnu.trove.iterator.TIntIntIterator;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.procedure.TIntObjectProcedure;
import gnu.trove.procedure.TIntProcedure;
import gnu.trove.procedure.TObjectProcedure;
import gnu.trove.set.TIntSet;
import net.trackmate.Ref;
import net.trackmate.RefPool;
import net.trackmate.collection.IntRefMap;


/**
 * An {@link IntRefMap} implementation for {@link Ref} objects, based on a Trove
 * TIntIntHashMap.
 * <p>
 * This implementation is best chosen when the <code>int</code> keys are not
 * ordered, and can have values much greater than this map cardinality. For
 * instance to store about ~100 mappings with keys anywhere from 0 to 1e9. When
 * the <code>int</code> keys typically range from 0 to the cardinality, it is
 * best to use the {@link IntRefArrayMap} implementation.
 *
 * @param <V>
 *            value type.
 *
 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
 * @author Jean-Yves Tinevez &lt;jeanyves.tinevez@gmail.com&gt;
 */
public class IntRefHashMap< V > implements IntRefMap< V >
{
	private final TIntIntMap keyToIndexMap;

	private final RefPool< V > pool;

	private final Class< V > valueType;

	public IntRefHashMap( final RefPool< V > pool, final int noEntryKey )
	{
		this( pool, noEntryKey, Constants.DEFAULT_CAPACITY );
	}

	public IntRefHashMap( final RefPool< V > pool, final int noEntryKey, final int initialCapacity )
	{
		this.pool = pool;
		valueType = pool.getRefClass();
		keyToIndexMap = new TIntIntHashMap( initialCapacity, Constants.DEFAULT_LOAD_FACTOR, noEntryKey, -1 )
		{
			// We need to do this to honor exactly the contract on toArray(int[]).
			@Override
			public TIntSet keySet()
			{
				return new TIntIntHashMap.TKeyView()
				{
					@Override
					public int[] toArray( final int[] dest )
					{
						final int[] arr = super.toArray( dest );
						for ( int i = size(); i < arr.length; i++ )
						{
							arr[ i ] = noEntryKey;
						}
						return arr;
					}
				};
			}
		};
	}

	@Override
	public V createRef()
	{
		return pool.createRef();
	}

	@Override
	public void releaseRef( final V obj )
	{
		pool.releaseRef( obj );
	}

	@Override
	public void clear()
	{
		keyToIndexMap.clear();
	}

	@Override
	public V get( final int key )
	{
		return get( key, pool.createRef() );
	}

	@Override
	public V get( final int key, final V obj )
	{
		final int index = keyToIndexMap.get( key );
		if ( index >= 0 )
			return pool.getObject( index, obj );
		else
			return null;
	}

	@Override
	public boolean isEmpty()
	{
		return keyToIndexMap.isEmpty();
	}

	@Override
	public V put( final int key, final V obj )
	{
		return put( key, obj, pool.createRef() );
	}

	@Override
	public V put( final int key, final V obj, final V replacedObj )
	{
		final int old = keyToIndexMap.put( key, pool.getId( obj ) );
		if ( old >= 0 )
			return pool.getObject( old, replacedObj );
		else
			return null;
	}

	@Override
	public V remove( final int key )
	{
		return remove( key, pool.createRef() );
	}

	@Override
	public V remove( final int key, final V obj )
	{
		final int old = keyToIndexMap.remove( key );
		if ( old >= 0 )
			return pool.getObject( old, obj );
		else
			return null;
	}

	@Override
	public int size()
	{
		return keyToIndexMap.size();
	}

	@Override
	public int getNoEntryKey()
	{
		return keyToIndexMap.getNoEntryKey();
	}

	@Override
	public boolean containsKey( final int key )
	{
		return keyToIndexMap.containsKey( key );
	}

	@SuppressWarnings( "unchecked" )
	@Override
	public boolean containsValue( final Object value )
	{
		if ( valueType.isInstance( value ) )
			return keyToIndexMap.containsValue( pool.getId( ( V ) value ) );
		else
			return false;
	}

	@Override
	public V putIfAbsent( final int key, final V value )
	{
		return put( key, value, createRef() );
	}

	@Override
	public V putIfAbsent( final int key, final V value, final V obj )
	{
		final int replaced = keyToIndexMap.putIfAbsent( key, pool.getId( value ) );
		if ( replaced >= 0 )
			return pool.getObject( replaced, obj );
		else
			return null;
	}

	@Override
	public void putAll( final Map< ? extends Integer, ? extends V > m )
	{
		final V ref = pool.createRef();
		for ( final Integer k : m.keySet() )
		{
			final V val = m.get( k );
			put( k, val, ref );
		}
		pool.releaseRef( ref );
	}

	@Override
	public void putAll( final TIntObjectMap< ? extends V > map )
	{
		final V ref = pool.createRef();
		for ( final int key : map.keys() )
		{
			final V val = map.get( key );
			put( key, val, ref );
		}
		pool.releaseRef( ref );
	}

	@Override
	public TIntSet keySet()
	{
		return keyToIndexMap.keySet();
	}

	@Override
	public int[] keys()
	{
		return keyToIndexMap.keys();
	}

	@Override
	public int[] keys( final int[] array )
	{
		return keyToIndexMap.keys( array );
	}

	@Override
	public Collection< V > valueCollection()
	{
		return new ValueCollection();
	}

	@Override
	public Object[] values()
	{
		return valueCollection().toArray();
	}

	@Override
	public V[] values( final V[] array )
	{
		return valueCollection().toArray( array );
	}

	@Override
	public TIntObjectIterator< V > iterator()
	{
		return new TIntObjectIterator< V >()
		{
			private final TIntIntIterator it = keyToIndexMap.iterator();

			private final V ref = pool.createRef();

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
			public int key()
			{
				return it.key();
			}

			@Override
			public V value()
			{
				final int poolIndex = it.value();
				return pool.getObject( poolIndex, ref );
			}

			@Override
			public V setValue( final V val )
			{
				return put( it.key(), val, ref );
			}
		};
	}

	@Override
	public boolean forEachKey( final TIntProcedure procedure )
	{
		return keyToIndexMap.forEachKey( procedure );
	}

	@Override
	public boolean forEachValue( final TObjectProcedure< ? super V > procedure )
	{
		final TIntObjectIterator< V > it = iterator();
		while ( it.hasNext() )
		{
			it.advance();
			if ( !procedure.execute( it.value() ) )
				return false;
		}
		return true;
	}

	@Override
	public boolean forEachEntry( final TIntObjectProcedure< ? super V > procedure )
	{
		final TIntObjectIterator< V > it = iterator();
		while ( it.hasNext() )
		{
			it.advance();
			if ( !procedure.execute( it.key(), it.value() ) )
				return false;
		}
		return true;
	}

	@Override
	public void transformValues( final TObjectFunction< V, V > function )
	{
		final TIntObjectIterator< V > it = iterator();
		while ( it.hasNext() )
		{
			it.advance();
			final V newValue = function.execute( it.value() );
			it.setValue( newValue );
		}
	}

	@Override
	public boolean retainEntries( final TIntObjectProcedure< ? super V > procedure )
	{
		final TIntObjectIterator< V > it = iterator();
		boolean changed = false;
		while ( it.hasNext() )
		{
			it.advance();
			if ( !procedure.execute( it.key(), it.value() ) )
			{
				it.remove();
				changed = true;

			}
		}
		return changed;
	}

	/*
	 * PRIVATE CLASS
	 */

	private class ValueCollection implements Collection< V >
	{

		@Override
		public int size()
		{
			return IntRefHashMap.this.size();
		}

		@Override
		public boolean isEmpty()
		{
			return IntRefHashMap.this.isEmpty();
		}

		@SuppressWarnings( "unchecked" )
		@Override
		public boolean contains( final Object value )
		{
			if ( valueType.isInstance( value ) )
				return keyToIndexMap.containsValue( pool.getId( ( V ) value ) );
			else
				return false;
		}

		@Override
		public Iterator< V > iterator()
		{
			return new Iterator< V >()
			{
				private final TIntIterator it = keyToIndexMap.valueCollection().iterator();

				private final V ref = pool.createRef();

				@Override
				public boolean hasNext()
				{
					return it.hasNext();
				}

				@Override
				public V next()
				{
					final int poolIndex = it.next();
					return pool.getObject( poolIndex, ref );
				}

				@Override
				public void remove()
				{
					it.remove();
				}
			};
		}

		@Override
		public Object[] toArray()
		{
			final Object[] arr = new Object[ size() ];
			return toArray( arr );
		}

		@SuppressWarnings( "unchecked" )
		@Override
		public < T > T[] toArray( final T[] a )
		{
			final Object[] arr;
			if ( a.length < size() )
			{
				arr = new Object[ size() ];
			}
			else
			{
				arr = a;
			}

			int i = 0;
			for ( final int key : keys() )
			{
				final V ref = pool.createRef();
				arr[ i++ ] = get( key, ref );
			}
			// nullify the rest.
			for ( int j = i; j < arr.length; j++ )
			{
				arr[ j ] = null;
			}
			return ( T[] ) arr;
		}

		@Override
		public boolean remove( final Object o )
		{
			// Use iterator
			final Iterator< V > it = iterator();
			while ( it.hasNext() )
			{
				if ( it.next().equals( o ) )
				{
					it.remove();
					return true;
				}
			}
			return false;
		}

		@Override
		public boolean containsAll( final Collection< ? > c )
		{
			for ( final Object obj : c )
			{
				if ( !contains( obj ) )
					return false;
			}
			return true;
		}


		@Override
		public boolean removeAll( final Collection< ? > c )
		{
			boolean changed = false;
			final Iterator< V > it = iterator();
			while ( it.hasNext() )
			{
				if ( c.contains( it.next() ) )
				{
					it.remove();
					changed = true;
				}
			}
			return changed;
		}

		@Override
		public boolean retainAll( final Collection< ? > c )
		{
			boolean changed = false;
			final Iterator< V > it = iterator();
			while ( it.hasNext() )
			{
				if ( !c.contains( it.next() ) )
				{
					it.remove();
					changed = true;
				}
			}
			return changed;
		}

		@Override
		public void clear()
		{
			IntRefHashMap.this.clear();
		}

		@Override
		public boolean add( final V e )
		{
			throw new UnsupportedOperationException( "add is not supported for valueCollection view." );
		}

		@Override
		public boolean addAll( final Collection< ? extends V > c )
		{
			throw new UnsupportedOperationException( "addAll is not supported for valueCollection view." );
		}
	}
}
