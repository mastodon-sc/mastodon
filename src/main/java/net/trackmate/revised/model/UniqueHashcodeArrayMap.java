package net.trackmate.revised.model;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import gnu.trove.map.TIntObjectArrayMap;

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
 * This is used for the look up of feature-maps for {@link VertexFeature} in
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
		for ( final Map.Entry< ? extends K, ? extends V > kv : m.entrySet() )
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
	public Set< Map.Entry< K, V > > entrySet()
	{
		throw new UnsupportedOperationException( "not implemented (yet?)" );
	}
}