package net.trackmate.graph;

import gnu.trove.function.TObjectFunction;
import gnu.trove.impl.Constants;
import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.TIntObjectMap;
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
 * <p>
 * A {@link IntRefMap} implementation backed by a {@link TIntArrayList}. It is
 * assumed that keys are internal pool indices of some {@link RefPool}, i.e.,
 * keys are <em>&ge;0</em> and not arbitrarily large. This is intended to
 * provide efficient mappings between graphs, for example a model graph and the
 * corresponding trackscheme graph.
 *
 * @param <V>
 *            value type.
 *
 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
 */
public class IntPoolObjectArrayMap< V extends Ref< V > > implements IntRefMap< V >
{
	private final TIntArrayList keyToIndexMap;

	private final RefPool< V > pool;

	private int size;

	public IntPoolObjectArrayMap( final RefPool< V > pool )
	{
		this( pool, Constants.DEFAULT_CAPACITY );
	}

	public IntPoolObjectArrayMap( final RefPool< V> pool, final int initialCapacity )
	{
		this.pool = pool;
		keyToIndexMap = new TIntArrayList( initialCapacity );
		size = 0;
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
		size = 0;
	}

	@Override
	public V get( final int key )
	{
		return get( key, pool.createRef() );
	}

	@Override
	public V get( final int key, final V obj )
	{
		if ( key < 0 || key >= keyToIndexMap.size() )
			return null;

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
		return size == 0;
	}

	@Override
	public V put( final int key, final V obj )
	{
		return put( key, obj, pool.createRef() );
	}

	private V putIndex( final int key, final int objInternalPoolIndex, final V replacedObj )
	{
		while ( key >= keyToIndexMap.size() )
			keyToIndexMap.add( -1 );

		if ( objInternalPoolIndex < 0 )
			--size;

		final int old = keyToIndexMap.set( key, objInternalPoolIndex );
		if ( old >= 0 )
		{
			pool.getByInternalPoolIndex( old, replacedObj );
			return replacedObj;
		}
		else
		{
			++size;
			return null;
		}
	}

	@Override
	public V put( final int key, final V obj, final V replacedObj )
	{
		return putIndex( key, obj.getInternalPoolIndex(), replacedObj );
	}

	@Override
	public V remove( final int key )
	{
		return remove( key, pool.createRef() );
	}

	@Override
	public V remove( final int key, final V obj )
	{
		return putIndex( key, -1, obj );
	}

	@Override
	public int size()
	{
		return size;
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
