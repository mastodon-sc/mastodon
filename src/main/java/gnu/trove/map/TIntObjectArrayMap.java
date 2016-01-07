package gnu.trove.map;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import gnu.trove.function.TObjectFunction;
import gnu.trove.impl.Constants;
import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.procedure.TIntObjectProcedure;
import gnu.trove.procedure.TIntProcedure;
import gnu.trove.procedure.TObjectProcedure;
import gnu.trove.set.TIntSet;

public class TIntObjectArrayMap< V > implements TIntObjectMap< V >, Externalizable
{

	/**
	 * Int value for no key.
	 */
	private static final int NO_ENTRY_KEY = -1;

	private final ArrayList< V > keyToObjMap;

	private int size;

	/*
	 * CONSTRUCTORS
	 */

	public TIntObjectArrayMap()
	{
		this( Constants.DEFAULT_CAPACITY );
	}

	public TIntObjectArrayMap( final int initialCapacity )
	{
		this.keyToObjMap = new ArrayList< V >( initialCapacity );
		this.size = 0;
	}

	/*
	 * METHODS
	 */



	@Override
	public int getNoEntryKey()
	{
		return NO_ENTRY_KEY;
	}

	@Override
	public int size()
	{
		return size;
	}

	@Override
	public boolean isEmpty()
	{
		return keyToObjMap.isEmpty();
	}

	@Override
	public boolean containsKey( final int key )
	{
		return keyToObjMap.size() > key && keyToObjMap.get( key ) != null;
	}

	@Override
	public boolean containsValue( final Object value )
	{
		return keyToObjMap.contains( value );
	}

	@Override
	public V get( final int key )
	{
		if ( keyToObjMap.size() <= key )
			return null;

		return keyToObjMap.get( key );
	}

	@Override
	public V put( final int key, final V value )
	{
		while ( key >= keyToObjMap.size() )
			keyToObjMap.add( null );

		final V old = keyToObjMap.set( key, value );
		if ( null != old )
		{
			return old;
		}
		else
		{
			++size;
			return null;
		}
	}

	@Override
	public V putIfAbsent( final int key, final V value )
	{
		if ( containsKey( key ) )
			return get( key );
		put( key, value );
		return null;
	}

	@Override
	public V remove( final int key )
	{
		if ( containsKey( key ) )
		{
			size--;
			return put( key, null );
		}
		return null;
	}

	@Override
	public void clear()
	{
		keyToObjMap.clear();
		size = 0;
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

	/*
	 * EXTERNALIZABLE
	 */

	@Override
	public void readExternal( final ObjectInput in ) throws IOException, ClassNotFoundException
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void writeExternal( final ObjectOutput out ) throws IOException
	{
		// TODO Auto-generated method stub

	}
}
