package net.trackmate.collection.ref;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.map.hash.TIntObjectHashMap;
import net.trackmate.RefPool;
import net.trackmate.collection.RefObjectMap;

/**
 * Incomplete!
 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
 */
public class RefObjectHashMap< K, O > implements Map< K, O >, RefObjectMap< K, O >
{
	private final TIntObjectHashMap< O > indexmap;

	private final RefPool< K > pool;

	private final Class< K > keyType;

	private EntrySet entrySet;

	public RefObjectHashMap( final RefPool< K > pool )
	{
		indexmap = new TIntObjectHashMap< O >();
		this.pool = pool;
		this.keyType = pool.getRefClass();
	}

	public RefObjectHashMap( final RefPool< K > pool, final int initialCapacity )
	{
		indexmap = new TIntObjectHashMap< O >( initialCapacity );
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
	public O get( final Object key )
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
	public O put( final K key, final O value )
	{
		return indexmap.put( pool.getId( key ), value );
	}

	@SuppressWarnings( "unchecked" )
	@Override
	public O remove( final Object key )
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
	public Collection< O > values()
	{
		return indexmap.valueCollection();
	}

	@Override
	public RefSetImp< K > keySet()
	{
		return new RefSetImp< K >( pool, indexmap.keySet() );
	}

	@Override
	public void putAll( final Map< ? extends K, ? extends O > m )
	{
		// TODO
		throw new UnsupportedOperationException();
	}

	@Override
	public Set< Entry< K, O > > entrySet()
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

	final class EntrySet extends AbstractSet< Entry< K, O > >
	{
		@Override
		public Iterator< Entry< K, O > > iterator()
		{
			final TIntObjectIterator< O > iter = indexmap.iterator();

			final Entry< K, O > entry = new Entry< K, O >()
			{
				final K ref = createKeyRef();

				@Override
				public K getKey()
				{
					return pool.getObject( iter.key(), ref );
				}

				@Override
				public O getValue()
				{
					return iter.value();
				}

				@Override
				public O setValue( final O value )
				{
					return iter.setValue( value );
				}
			};

			return new Iterator< Entry< K, O > >()
			{
				@Override
				public boolean hasNext()
				{
					return iter.hasNext();
				}

				@Override
				public Entry< K, O > next()
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
