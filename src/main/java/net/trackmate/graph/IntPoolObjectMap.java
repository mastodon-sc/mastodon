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

import net.trackmate.graph.collection.IntRefMap;


/**
 * WARNING: THIS IS VERY INCOMPLETE!
 *
 * @param <V>
 *            value type.
 *
 * @author Tobias Pietzsch <tobias.pietzsch@gmail.com>
 */
public class IntPoolObjectMap< V extends Ref< V > > implements IntRefMap< V >
{
	private final TIntIntMap keyToIndexMap;

	private final RefPool< V > pool;

	public IntPoolObjectMap( final RefPool< V > pool, final int noEntryKey )
	{
		this( pool, noEntryKey, Constants.DEFAULT_CAPACITY );
	}

	public IntPoolObjectMap( final RefPool< V> pool, final int noEntryKey, final int initialCapacity )
	{
		this.pool = pool;
		keyToIndexMap = new TIntIntHashMap( initialCapacity, Constants.DEFAULT_LOAD_FACTOR, noEntryKey, -1 );
	}

	@Override
	public V createRef()
	{
		return pool.createRef();
	}

	@Override
	public void releaseRef( final V obj )
	{
		pool.releaseRef( obj );
	}

	@Override
	public void clear()
	{
		keyToIndexMap.clear();
	}

	@Override
	public V get( final int key )
	{
		return get( key, pool.createRef() );
	}

	@Override
	public V get( final int key, final V obj )
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
	public V put( final int key, final V obj )
	{
		return put( key, obj, pool.createRef() );
	}

	@Override
	public V put( final int key, final V obj, final V replacedObj )
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
	public V putIfAbsent( final int key, final V value )
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public V remove( final int key )
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void putAll( final Map< ? extends Integer, ? extends V > m )
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void putAll( final TIntObjectMap< ? extends V > map )
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
	public Collection< V > valueCollection()
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
	public V[] values( final V[] array )
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TIntObjectIterator< V > iterator()
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
	public boolean forEachValue( final TObjectProcedure< ? super V > procedure )
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean forEachEntry( final TIntObjectProcedure< ? super V > procedure )
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void transformValues( final TObjectFunction< V, V > function )
	{
		// TODO Auto-generated method stub

	}

	@Override
	public boolean retainEntries( final TIntObjectProcedure< ? super V > procedure )
	{
		// TODO Auto-generated method stub
		return false;
	}
}
