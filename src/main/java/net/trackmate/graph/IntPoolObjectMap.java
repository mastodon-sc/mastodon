package net.trackmate.graph;

import gnu.trove.impl.Constants;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import net.trackmate.graph.mempool.MappedElement;


/**
 * WARNING: THIS IS VERY INCOMPLETE!
 *
 * @param <O>
 * @param <T>
 *
 * @author Tobias Pietzsch <tobias.pietzsch@gmail.com>
 */
public class IntPoolObjectMap< O extends PoolObject< O, T >, T extends MappedElement > implements Map< Integer, O >
{
	private final TIntIntMap keyToIndexMap;

	private final Pool< O, T > pool;

	public IntPoolObjectMap( final Pool< O, T > pool )
	{
		this( pool, Constants.DEFAULT_CAPACITY );
	}

	public IntPoolObjectMap( final Pool< O, T > pool, final int initialCapacity )
	{
		this.pool = pool;
		keyToIndexMap = new TIntIntHashMap( initialCapacity, Constants.DEFAULT_LOAD_FACTOR, -1, -1 );
	}


	@Override
	public void clear()
	{
		keyToIndexMap.clear();
	}

	@Override
	public boolean containsKey( final Object arg0 )
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean containsValue( final Object arg0 )
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Set< java.util.Map.Entry< Integer, O > > entrySet()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public O get( final Object arg0 )
	{
		// TODO Auto-generated method stub
		return null;
	}

	public O get( final int key )
	{
		return get( key, pool.createRef() );
	}

	public O get( final int key, final O obj )
	{
		final int index = keyToIndexMap.get( key );
		if ( index >= 0 )
		{
			pool.getByInternalPoolIndex( index, obj );
			return obj;
		}
		else
			return null;
	}

	@Override
	public boolean isEmpty()
	{
		return keyToIndexMap.isEmpty();
	}

	@Override
	public Set< Integer > keySet()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public O put( final Integer arg0, final O arg1 )
	{
		// TODO Auto-generated method stub
		return null;
	}

	public O put( final int key, final O obj )
	{
		return put( key, obj, pool.createRef() );
	}

	public O put( final int key, final O obj, final O replacedObj )
	{
		final int old = keyToIndexMap.put( key, obj.getInternalPoolIndex() );
		if ( old >= 0 )
		{
			pool.getByInternalPoolIndex( old, replacedObj );
			return replacedObj;
		}
		else
			return null;
	}

	@Override
	public void putAll( final Map< ? extends Integer, ? extends O > arg0 )
	{
		// TODO Auto-generated method stub

	}

	@Override
	public O remove( final Object arg0 )
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int size()
	{
		return keyToIndexMap.size();
	}

	@Override
	public Collection< O > values()
	{
		// TODO Auto-generated method stub
		return null;
	}

}
