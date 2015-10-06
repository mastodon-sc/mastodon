package net.trackmate.graph.collection;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Wraps a standard {@link Map} in a {@link RefRefMap}.
 */
public class RefMapWrapper< K, L > implements RefRefMap< K, L >
{
	private final Map< K, L > map;

	public RefMapWrapper( final Map< K, L > map )
	{
		this.map = map;
	}

	@Override
	public boolean containsKey( final Object key )
	{
		return map.containsKey( key );
	}

	@Override
	public boolean containsValue( final Object value )
	{
		return map.containsValue( value );
	}

	@Override
	public Set< Entry< K, L >> entrySet()
	{
		return map.entrySet();
	}

	@Override
	public L get( final Object key )
	{
		return map.get( key );
	}

	@Override
	public boolean isEmpty()
	{
		return map.isEmpty();
	}

	@Override
	public Set< K > keySet()
	{
		return map.keySet();
	}

	@Override
	public L put( final K key, final L value )
	{
		return map.put( key, value );
	}

	@Override
	public void putAll( final Map< ? extends K, ? extends L > m )
	{
		map.putAll( m );
	}

	@Override
	public L remove( final Object key )
	{
		return map.remove( key );
	}

	@Override
	public int size()
	{
		return map.size();
	}

	@Override
	public Collection< L > values()
	{
		return map.values();
	}

	@Override
	public K createRef()
	{
		return null;
	}

	@Override
	public void releaseRef( final K obj )
	{}

	@Override
	public void clear()
	{
		map.clear();
	}

	@Override
	public L createValueRef()
	{
		return null;
	}

	@Override
	public void releaseValueRef( final L obj )
	{}

	@Override
	public L put( final K key, final L value, final L ref )
	{
		return put( key, value );
	}

	@Override
	public L removeWithRef( final Object key, final L ref )
	{
		return remove( key );
	}

	@Override
	public L get( final Object key, final L ref )
	{
		return get( key );
	}
}
