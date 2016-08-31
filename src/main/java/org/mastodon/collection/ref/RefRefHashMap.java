package org.mastodon.collection.ref;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.mastodon.RefPool;
import org.mastodon.collection.RefRefMap;

import gnu.trove.iterator.TIntIterator;
import gnu.trove.map.hash.TIntIntHashMap;

public class RefRefHashMap< K, V > implements RefRefMap< K, V >
{
	/**
	 * Int value used to declare that the requested key is not in the map.
	 * Negative, so that it cannot be an index in the pool.
	 */
	private static final int NO_ENTRY_KEY = -1;

	/**
	 * Int value used to declare that the requested value is not in the map.
	 * Negative, so that it cannot be an index in the pool.
	 */
	private static final int NO_ENTRY_VALUE = -2;

	private final TIntIntHashMap indexmap;

	private final RefPool< K > keyPool;

	private final RefPool< V > valuePool;

	private final Class< K > keyType;

	private final Class< V > valueType;

	/*
	 * CONSTRUCTORS
	 */

	public RefRefHashMap( final RefPool< K > keyPool, final RefPool< V > valuePool, final int initialCapacity, final float loadFactor )
	{
		this.indexmap = new TIntIntHashMap( initialCapacity, loadFactor, NO_ENTRY_KEY, NO_ENTRY_VALUE );
		this.keyPool = keyPool;
		this.valuePool = valuePool;
		this.keyType = keyPool.getRefClass();
		this.valueType = valuePool.getRefClass();
	}

	public RefRefHashMap( final RefPool< K > keyPool, final RefPool< V > valuePool, final int initialCapacity )
	{
		this( keyPool, valuePool, initialCapacity, 0.5f );
	}

	public RefRefHashMap( final RefPool< K > keyPool, final RefPool< V > valuePool )
	{
		this( keyPool, valuePool, 10 );
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
			return indexmap.containsKey( keyPool.getId( ( K ) key ) );
		else
			return false;
	}

	@SuppressWarnings( "unchecked" )
	@Override
	public boolean containsValue( final Object value )
	{
		if ( valueType.isInstance( value ) )
			return indexmap.containsValue( valuePool.getId( ( V ) value ) );
		else
			return false;
	}

	@Override
	public Set< Entry< K, V > > entrySet()
	{
		// TODO implement
		throw new UnsupportedOperationException();
	}

	@Override
	public V get( final Object key )
	{
		return get( key, valuePool.createRef() );
	}

	@Override
	public V get( final Object key, final V ref )
	{
		if ( keyType.isInstance( key ) )
		{
			@SuppressWarnings( "unchecked" )
			final int index = indexmap.get( keyPool.getId( ( K ) key ) );
			if ( index != NO_ENTRY_VALUE )
				return valuePool.getObject( index, ref );
		}
		return null;
	}

	@Override
	public boolean isEmpty()
	{
		return indexmap.isEmpty();
	}

	@Override
	public Set< K > keySet()
	{
		return new RefSetImp<>( keyPool, indexmap.keySet() );
	}

	@Override
	public V put( final K key, final V value, final V ref )
	{
		final int index = indexmap.put( keyPool.getId( key ), valuePool.getId( value ) );
		if ( index != NO_ENTRY_VALUE )
			return valuePool.getObject( index, ref );
		else
			return null;
	}

	@Override
	public V put( final K key, final V value )
	{
		return put( key, value, valuePool.createRef() );
	}

	// TODO revise after implementing entrySet()
	@Override
	public void putAll( final Map< ? extends K, ? extends V > m )
	{
		if ( m instanceof RefRefMap )
		{
			@SuppressWarnings( "unchecked" )
			final RefRefMap< K, V > rm = ( RefRefMap< K, V > ) m;
			final V ref = createValueRef();
			for ( final K key : rm.keySet() )
			{
				indexmap.put( keyPool.getId( key ), valuePool.getId( rm.get( key, ref ) ) );
			}
			rm.releaseValueRef( ref );
		}
		else
		{
			for ( final K key : m.keySet() )
			{
				indexmap.put( keyPool.getId( key ), valuePool.getId( m.get( key ) ) );
			}
		}
	}

	@Override
	public V removeWithRef( final Object key, final V ref )
	{
		if ( keyType.isInstance( key ) )
		{
			@SuppressWarnings( "unchecked" )
			final int index = indexmap.remove( keyPool.getId( ( K ) key ) );
			if ( index != NO_ENTRY_VALUE )
				return valuePool.getObject( index, ref );
		}
		return null;
	}

	@Override
	public V remove( final Object key )
	{
		return removeWithRef( key, valuePool.createRef() );
	}

	@Override
	public int size()
	{
		return indexmap.size();
	}

	@Override
	public Collection< V > values()
	{
		return new CollectionValuesView();
	}

	@Override
	public K createKeyRef()
	{
		return keyPool.createRef();
	}

	@Override
	public void releaseKeyRef( final K obj )
	{
		keyPool.releaseRef( obj );
	}

	@Override
	public V createValueRef()
	{
		return valuePool.createRef();
	}

	@Override
	public void releaseValueRef( final V obj )
	{
		valuePool.releaseRef( obj );
	}

	// TODO revise after implementing entrySet()
	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		final V ref = createValueRef();
		final Iterator< K > it = keySet().iterator();
		sb.append( "{ " );
		while ( it.hasNext() )
		{
			final K key = it.next();
			final V val = get( key, ref );
			sb.append( key );
			sb.append( '=' ).append( '"' );
			sb.append( val );
			sb.append( '"' );
			if ( it.hasNext() )
			{
				sb.append( ',' ).append( ' ' );
			}
		}
		sb.append( " }" );
		return sb.toString();
	}

	/*
	 * INNER CLASS
	 */

	private class CollectionValuesView implements Collection< V >
	{

		@Override
		public boolean add( final V e )
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean addAll( final Collection< ? extends V > c )
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public void clear()
		{
			RefRefHashMap.this.clear();
		}

		@Override
		public boolean contains( final Object o )
		{
			return RefRefHashMap.this.containsValue( o );
		}

		@Override
		public boolean containsAll( final Collection< ? > collection )
		{
			final Iterator< ? > iter = collection.iterator();
			while ( iter.hasNext() )
			{
				if ( !RefRefHashMap.this.containsValue( iter.next() ) ) { return false; }
			}
			return true;
		}

		@Override
		public boolean isEmpty()
		{
			return 0 == size();
		}

		/**
		 * Unsafe iterator.
		 */
		@Override
		public Iterator< V > iterator()
		{
			final TIntIterator it = indexmap.valueCollection().iterator();
			final V ref = createValueRef();
			return new Iterator< V >()
			{
				@Override
				public boolean hasNext()
				{
					return it.hasNext();
				}

				@Override
				public V next()
				{
					final int index = it.next();
					return valuePool.getObject( index, ref );
				}

				@Override
				public void remove()
				{
					it.remove();
				}
			};
		}

		@SuppressWarnings( "unchecked" )
		@Override
		public boolean remove( final Object value )
		{
			if ( valueType.isInstance( value ) )
			{
				return indexmap.valueCollection().remove(
						valuePool.getId( ( V ) value ) );
			}
			else
			{
				return false;
			}
		}

		@Override
		public boolean removeAll( final Collection< ? > collection )
		{
			boolean changed = false;
			for ( final Object value : collection )
			{
				changed = remove( value ) || changed;
			}
			return changed;
		}

		@Override
		public boolean retainAll( final Collection< ? > collection )
		{
			boolean changed = false;
			final Iterator< V > it = iterator();
			while ( it.hasNext() )
			{
				if ( !collection.contains( it.next() ) )
				{
					it.remove();
					changed = true;
				}
			}
			return changed;
		}

		@Override
		public int size()
		{
			return indexmap.size();
		}

		@Override
		public Object[] toArray()
		{
			final int[] indices = indexmap.values();
			final Object[] obj = new Object[ indices.length ];
			for ( int i = 0; i < obj.length; i++ )
				obj[ i ] = valuePool.getObject( indices[ i ], createValueRef() );
			return obj;
		}

		@SuppressWarnings( "unchecked" )
		@Override
		public < T > T[] toArray( final T[] a )
		{
			if ( a.length < size() ) { return ( T[] ) toArray(); }

			final int[] indices = indexmap.values();
			for ( int i = 0; i < indices.length; i++ )
				a[ i ] = ( T ) valuePool.getObject( indices[ i ], createValueRef() );
			for ( int i = indices.length; i < a.length; i++ )
				a[ i ] = null;
			return a;
		}
	}
}
