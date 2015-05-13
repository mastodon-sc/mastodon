package net.trackmate.graph;

import gnu.trove.map.hash.TIntObjectHashMap;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import net.trackmate.graph.collection.RefObjectMap;

/**
 * Incomplete!
 * @author Tobias Pietzsch <tobias.pietzsch@gmail.com>
 */
public class PoolObjectObjectMap< K extends Ref< K >, O > implements Map< K, O >, RefObjectMap< K, O >
{
	private final TIntObjectHashMap< O > indexmap;

	private final RefPool< K > pool;

	public PoolObjectObjectMap( final RefPool< K > pool )
	{
		indexmap = new TIntObjectHashMap< O >();
		this.pool = pool;
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
		if ( key != null && key instanceof PoolObject )
			return indexmap.containsKey( ( ( K ) key ).getInternalPoolIndex() );
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
		if ( key != null && key instanceof PoolObject )
			return indexmap.get( ( ( K ) key ).getInternalPoolIndex() );
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
		return indexmap.put( key.getInternalPoolIndex(), value );
	}

	@SuppressWarnings( "unchecked" )
	@Override
	public O remove( final Object key )
	{
		if ( key != null && key instanceof PoolObject )
			return indexmap.remove( ( ( K ) key ).getInternalPoolIndex() );
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
	public PoolObjectSet< K > keySet()
	{
		return new PoolObjectSet< K >( pool, indexmap.keySet() );
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
		// TODO
		throw new UnsupportedOperationException();
	}

	@Override
	public K createRef()
	{
		return pool.createRef();
	}

	@Override
	public void releaseRef( final K obj )
	{
		pool.releaseRef( obj );
	}
}
