package net.trackmate.graph;

import gnu.trove.iterator.TIntIterator;
import gnu.trove.map.hash.TIntIntHashMap;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import net.trackmate.graph.collection.RefRefMap;
import net.trackmate.graph.mempool.MappedElement;

public class PoolObjectPoolObjectMap< K extends PoolObject< K, TK >, L extends PoolObject< L, TL >, TK extends MappedElement, TL extends MappedElement > implements RefRefMap< K, L >
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

	final TIntIntHashMap indexmap;

	private final Pool< K, TK > keyPool;

	private final Pool< L, TL > valuePool;

	/*
	 * CONSTRUCTORS
	 */

	public PoolObjectPoolObjectMap( final Pool< K, TK > keyPool, final Pool< L, TL > valuePool, final int initialCapacity, final float loadFactor )
	{
		this.indexmap = new TIntIntHashMap( initialCapacity, loadFactor, NO_ENTRY_KEY, NO_ENTRY_VALUE );
		this.keyPool = keyPool;
		this.valuePool = valuePool;
	}

	public PoolObjectPoolObjectMap( final Pool< K, TK > keyPool, final Pool< L, TL > valuePool, final int initialCapacity )
	{
		this( keyPool, valuePool, initialCapacity, 0.5f );
	}

	public PoolObjectPoolObjectMap( final Pool< K, TK > keyPool, final Pool< L, TL > valuePool )
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
		if ( key != null && key instanceof PoolObject )
			return indexmap.containsKey( ( ( K ) key ).getInternalPoolIndex() );
		else
			return false;
	}

	@SuppressWarnings( "unchecked" )
	@Override
	public boolean containsValue( final Object value )
	{
		if ( value != null && value instanceof PoolObject )
			return indexmap.containsValue( ( ( L ) value ).getInternalPoolIndex() );
		else
			return false;
	}

	@Override
	public Set< java.util.Map.Entry< K, L >> entrySet()
	{
		// TODO
		throw new UnsupportedOperationException();
	}

	@Override
	public L get( final Object key )
	{
		return get( key, valuePool.createRef() );
	}

	@Override
	public L get( final Object key, final L ref )
	{
		if ( key != null && key instanceof PoolObject )
		{
			@SuppressWarnings( "unchecked" )
			final int index = indexmap.get( ( ( K ) key ).getInternalPoolIndex() );
			if ( index == NO_ENTRY_VALUE ) { return null; }
			ref.updateAccess( valuePool.getMemPool(), index );
			return ref;
		}
		else
		{
			return null;
		}
	}

	@Override
	public boolean isEmpty()
	{
		return indexmap.isEmpty();
	}

	@Override
	public Set< K > keySet()
	{
		return new PoolObjectSet< K, TK >( keyPool, indexmap.keySet() );
	}

	@Override
	public L put( final K key, final L value, final L ref )
	{
		final int index = indexmap.put( key.getInternalPoolIndex(), value.getInternalPoolIndex() );
		if ( index == NO_ENTRY_VALUE ) { return null; }
		ref.updateAccess( valuePool.getMemPool(), index );
		return ref;
	}

	@Override
	public L put( final K key, final L value )
	{
		return put( key, value, valuePool.createRef() );
	}

	@Override
	public void putAll( final Map< ? extends K, ? extends L > m )
	{
		// TODO
		throw new UnsupportedOperationException();
	}

	@Override
	public L remove( final Object key, final L ref )
	{
		if ( key != null && key instanceof PoolObject )
		{
			@SuppressWarnings( "unchecked" )
			final int index = indexmap.remove( ( ( K ) key ).getInternalPoolIndex() );
			if ( index == NO_ENTRY_VALUE ) { return null; }
			ref.updateAccess( valuePool.getMemPool(), index );
			return ref;
		}
		else
		{
			return null;
		}
	}

	@Override
	public L remove( final Object key )
	{
		return remove( key, valuePool.createRef() );
	}

	@Override
	public int size()
	{
		return indexmap.size();
	}

	@Override
	public Collection< L > values()
	{
		return new CollectionValuesView();
	}

	@Override
	public K createRef()
	{
		return keyPool.createRef();
	}

	@Override
	public void releaseRef( final K obj )
	{
		keyPool.releaseRef( obj );
	}

	@Override
	public L createValueRef()
	{
		return valuePool.createRef();
	}

	@Override
	public void releaseValueRef( final L obj )
	{
		valuePool.releaseRef( obj );
	}

	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		final L ref = createValueRef();
		final Iterator< K > it = keySet().iterator();
		sb.append( "{ " );
		while ( it.hasNext() )
		{
			final K key = it.next();
			final L val = get( key, ref );
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

	private class CollectionValuesView implements Collection< L >
	{

		@Override
		public boolean add( final L e )
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean addAll( final Collection< ? extends L > c )
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public void clear()
		{
			PoolObjectPoolObjectMap.this.clear();
		}

		@Override
		public boolean contains( final Object o )
		{
			return PoolObjectPoolObjectMap.this.containsValue( o );
		}

		@Override
		public boolean containsAll( final Collection< ? > collection )
		{
			final Iterator< ? > iter = collection.iterator();
			while ( iter.hasNext() )
			{
				if ( !PoolObjectPoolObjectMap.this.containsValue( iter.next() ) ) { return false; }
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
		public Iterator< L > iterator()
		{
			final TIntIterator it = indexmap.valueCollection().iterator();
			final L ref = createValueRef();
			return new Iterator< L >()
			{
				@Override
				public boolean hasNext()
				{
					return it.hasNext();
				}

				@Override
				public L next()
				{
					final int index = it.next();
					ref.updateAccess( valuePool.getMemPool(), index );
					return ref;
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
			if ( value != null && value instanceof PoolObject )
			{
				return indexmap.valueCollection().remove(
						( ( L ) value ).getInternalPoolIndex() );
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
			final Iterator< L > it = iterator();
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
			{
				final L ref = createValueRef();
				ref.updateAccess( valuePool.getMemPool(), indices[ i ] );
				obj[ i ] = ref;
			}
			return obj;
		}

		@SuppressWarnings( "unchecked" )
		@Override
		public < T > T[] toArray( final T[] a )
		{
			if ( a.length < size() ) { return ( T[] ) toArray(); }

			final int[] indices = indexmap.values();
			for ( int i = 0; i < indices.length; i++ )
			{
				final L ref = createValueRef();
				ref.updateAccess( valuePool.getMemPool(), indices[ i ] );
				a[ i ] = ( T ) ref;
			}
			for ( int i = indices.length; i < a.length; i++ )
			{
				a[ i ] = null;
			}
			return a;
		}
	}
}
