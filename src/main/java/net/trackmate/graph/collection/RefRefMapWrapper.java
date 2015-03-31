package net.trackmate.graph.collection;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Wraps a standard {@link Map} in a {@link RefRefMap}.
 */
public class RefRefMapWrapper< O, P > implements RefRefMap< O, P >
{
	private final Map< O, P > map;

	public RefRefMapWrapper( final Map< O, P > map )
	{
		this.map = map;
	}

	@Override
	public void clear()
	{
		map.clear();
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
	public Set< java.util.Map.Entry< O, P >> entrySet()
	{
		return map.entrySet();
	}

	@Override
	public P get( final Object key )
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
	public P put( final O key, final P value )
	{
		return map.put( key, value );
	}

	@Override
	public void putAll( final Map< ? extends O, ? extends P > m )
	{
		map.putAll( m );
	}

	@Override
	public P remove( final Object key )
	{
		return map.remove( key );
	}

	@Override
	public int size()
	{
		return map.size();
	}

	@Override
	public Collection< P > values()
	{
		return map.values();
	}

	@Override
	public O createRef()
	{
		return null;
	}

	@Override
	public void releaseRef( final O obj )
	{}

	@Override
	public P createValueRef()
	{
		return null;
	}

	@Override
	public void releaseValueRef( final P obj )
	{}
}
