package net.trackmate.graph;

import gnu.trove.function.TObjectFunction;
import gnu.trove.impl.Constants;
import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.procedure.TIntObjectProcedure;
import gnu.trove.procedure.TIntProcedure;
import gnu.trove.procedure.TObjectProcedure;
import gnu.trove.set.TIntSet;

import java.util.Collection;
import java.util.Map;

import net.trackmate.graph.mempool.MappedElement;


/**
 * WARNING: THIS IS VERY INCOMPLETE!
 *
 * @param <O>
 * @param <T>
 *
 * @author Tobias Pietzsch <tobias.pietzsch@gmail.com>
 */
public class IntPoolObjectMap< O extends PoolObject< O, T >, T extends MappedElement > implements TIntObjectMap< O >
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
	public int size()
	{
		return keyToIndexMap.size();
	}



	// === TODO === UNIMPLEMENTED ========





	@Override
	public int getNoEntryKey()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean containsKey( final int key )
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean containsValue( final Object value )
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public O putIfAbsent( final int key, final O value )
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public O remove( final int key )
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void putAll( final Map< ? extends Integer, ? extends O > m )
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void putAll( final TIntObjectMap< ? extends O > map )
	{
		// TODO Auto-generated method stub

	}

	@Override
	public TIntSet keySet()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int[] keys()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int[] keys( final int[] array )
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection< O > valueCollection()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object[] values()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public O[] values( final O[] array )
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TIntObjectIterator< O > iterator()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean forEachKey( final TIntProcedure procedure )
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean forEachValue( final TObjectProcedure< ? super O > procedure )
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean forEachEntry( final TIntObjectProcedure< ? super O > procedure )
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void transformValues( final TObjectFunction< O, O > function )
	{
		// TODO Auto-generated method stub

	}

	@Override
	public boolean retainEntries( final TIntObjectProcedure< ? super O > procedure )
	{
		// TODO Auto-generated method stub
		return false;
	}
}
