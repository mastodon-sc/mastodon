package net.trackmate.collection;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import gnu.trove.map.TIntObjectArrayMap;
import net.trackmate.graph.features.Feature;
import net.trackmate.revised.model.AbstractModel;

/**
 * A {@link Map} from {@code K} to {@code V} which is backed by a
 * {@link TIntObjectArrayMap} from {@code int} to {@code V}. To translate the
 * {@code K} key to an {@code int} key, the {@link Object#hashCode()} of the
 * {@code K} key is used. For this to work,
 * <ul>
 * <li>the hashcodes need to be unique (two objects have same hashcode if and
 * only if they are {@link Object#equals(Object) equal}.
 * <li>the hashcodes need to be small positive integers (such that the backing
 * array does not get large).
 * </ul>
 *
 * This is used for the look up of feature-maps for {@link Feature} in
 * {@link AbstractModel}.
 *
 * @param <K>
 * @param <V>
 *
 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
 */
public final class UniqueHashcodeArrayMap< K, V > implements Map< K, V >
{
	private final TIntObjectArrayMap< V > map;

	private final HashSet< K > keySet;

	private EntrySet entrySet;

	public UniqueHashcodeArrayMap()
	{
		map = new TIntObjectArrayMap<>();
		keySet = new HashSet<>();
	}

	@Override
	public int size()
	{
		return map.size();
	}

	@Override
	public boolean isEmpty()
	{
		return map.isEmpty();
	}

	@Override
	public boolean containsKey( final Object key )
	{
		if ( ( key == null ) )
			return false;

		return map.containsKey( key.hashCode() );
	}

	@Override
	public boolean containsValue( final Object value )
	{
		return map.containsValue( value );
	}

	@Override
	public V get( final Object key )
	{
		if ( ( key == null ) )
			return null;

		return map.get( key.hashCode() );
	}

	@Override
	public V put( final K key, final V value )
	{
		if ( ( key == null ) )
			return null;

		keySet.add( key );
		return map.put( key.hashCode(), value );
	}

	@Override
	public V remove( final Object key )
	{
		if ( ( key == null ) )
			return null;

		return map.remove( key.hashCode() );
	}

	@Override
	public void putAll( final Map< ? extends K, ? extends V > m )
	{
		for ( final Entry< ? extends K, ? extends V > kv : m.entrySet() )
			map.put( kv.getKey().hashCode(), kv.getValue() );
	}

	@Override
	public void clear()
	{
		map.clear();
	}

	@Override
	public Set< K > keySet()
	{
		return keySet;
	}

	@Override
	public Collection< V > values()
	{
		return map.valueCollection();
	}

	@Override
	public Set< Entry< K, V > > entrySet()
	{
		return ( entrySet == null ) ? ( entrySet = new EntrySet() ) : entrySet;
	}

	final class EntrySet extends AbstractSet< Entry< K, V > >
	{
		@Override
		public Iterator< Entry< K, V > > iterator()
		{
			final Iterator< K > kiter = keySet().iterator();

			return new Iterator< Entry< K, V > >()
			{
				@Override
				public boolean hasNext()
				{
					return kiter.hasNext();
				}

				@Override
				public Entry< K, V > next()
				{
					final K key = kiter.next();
					return new Entry< K, V >()
					{
						@Override
						public K getKey()
						{
							return key;
						}

						@Override
						public V getValue()
						{
							return UniqueHashcodeArrayMap.this.get( key );
						}

						@Override
						public V setValue( final V value )
						{
							return UniqueHashcodeArrayMap.this.put( key, value );
						}
					};
				}
			};
		}

		@Override
		public int size()
		{
			return UniqueHashcodeArrayMap.this.size();
		}
	}
}
