package net.trackmate.graph.collection;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Wrap a standard {@link Map} in a {@link RefObjectMap}.
 * 
 */
public class RefObjectMapWrapper< O, V > implements RefObjectMap< O, V >
{
	private final Map< O, V > map;

	public RefObjectMapWrapper( final Map< O, V > map )
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
	public Set< Entry< O, V >> entrySet()
	{
		return map.entrySet();
	}

	@Override
	public V get( final Object key )
	{
		return map.get( key );
	}

	@Override
	public boolean isEmpty()
	{
		return map.isEmpty();
	}

	@Override
	public Set< O > keySet()
	{
		return map.keySet();
	}

	@Override
	public V put( final O key, final V value )
	{
		return map.put( key, value );
	}

	@Override
	public void putAll( final Map< ? extends O, ? extends V > m )
	{
		map.putAll( m );
	}

	@Override
	public V remove( final Object key )
	{
		return map.remove( key );
	}

	@Override
	public int size()
	{
		return map.size();
	}

	@Override
	public Collection< V > values()
	{
		return map.values();
	}

	@Override
	public O createRef()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void releaseRef( final O obj )
	{
		// TODO Auto-generated method stub
	}

	@Override
	public void clear()
	{
		// TODO Auto-generated method stub
	}
}
