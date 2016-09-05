package org.mastodon.collection.ref;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.mastodon.RefPool;
import org.mastodon.collection.RefObjectMap;

import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.map.hash.TIntObjectHashMap;

/**
 * Incomplete!
 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
 */
public class RefObjectHashMap< K, V > implements Map< K, V >, RefObjectMap< K, V >
{
	private final TIntObjectHashMap< V > indexmap;

	private final RefPool< K > pool;

	private final Class< K > keyType;

	private EntrySet entrySet;

	public RefObjectHashMap( final RefPool< K > pool )
	{
		indexmap = new TIntObjectHashMap<>();
		this.pool = pool;
		this.keyType = pool.getRefClass();
	}

	public RefObjectHashMap( final RefPool< K > pool, final int initialCapacity )
	{
		indexmap = new TIntObjectHashMap<>( initialCapacity );
		this.pool = pool;
		this.keyType = pool.getRefClass();
	}

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
	public boolean containsValue( final Object value )
	{
		return indexmap.containsValue( value );
	}

	@SuppressWarnings( "unchecked" )
	@Override
	public V get( final Object key )
	{
		if ( keyType.isInstance( key ) )
			return indexmap.get( pool.getId( ( K ) key ) );
		else
			return null;
	}

	@Override
	public boolean isEmpty()
	{
		return indexmap.isEmpty();
	}

	@Override
	public V put( final K key, final V value )
	{
		return indexmap.put( pool.getId( key ), value );
	}

	@SuppressWarnings( "unchecked" )
	@Override
	public V remove( final Object key )
	{
		if ( keyType.isInstance( key ) )
			return indexmap.remove( pool.getId( ( K ) key ) );
		else
			return null;
	}

	@Override
	public int size()
	{
		return indexmap.size();
	}

	@Override
	public Collection< V > values()
	{
		return indexmap.valueCollection();
	}

	@Override
	public RefSetImp< K > keySet()
	{
		return new RefSetImp<>( pool, indexmap.keySet() );
	}

	@Override
	public void putAll( final Map< ? extends K, ? extends V > m )
	{
		// TODO
		throw new UnsupportedOperationException();
	}

	@Override
	public Set< Entry< K, V > > entrySet()
	{
		return ( entrySet == null ) ? ( entrySet = new EntrySet() ) : entrySet;
	}

	@Override
	public K createKeyRef()
	{
		return pool.createRef();
	}

	@Override
	public void releaseKeyRef( final K obj )
	{
		pool.releaseRef( obj );
	}

	final class EntrySet extends AbstractSet< Entry< K, V > >
	{
		@Override
		public Iterator< Entry< K, V > > iterator()
		{
			final TIntObjectIterator< V > iter = indexmap.iterator();

			final Entry< K, V > entry = new Entry< K, V >()
			{
				final K ref = createKeyRef();

				@Override
				public K getKey()
				{
					return pool.getObject( iter.key(), ref );
				}

				@Override
				public V getValue()
				{
					return iter.value();
				}

				@Override
				public V setValue( final V value )
				{
					return iter.setValue( value );
				}
			};

			return new Iterator< Entry< K, V > >()
			{
				@Override
				public boolean hasNext()
				{
					return iter.hasNext();
				}

				@Override
				public Entry< K, V > next()
				{
					iter.advance();
					return entry;
				}
			};
		}

		@Override
		public int size()
		{
			return RefObjectHashMap.this.size();
		}
	}
}
