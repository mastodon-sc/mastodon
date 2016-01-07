package gnu.trove.map;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import gnu.trove.function.TObjectFunction;
import gnu.trove.impl.Constants;
import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.procedure.TIntObjectProcedure;
import gnu.trove.procedure.TIntProcedure;
import gnu.trove.procedure.TObjectProcedure;
import gnu.trove.set.TIntSet;

public class TIntObjectArrayMap< V > implements TIntObjectMap< V >
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
		return size == 0;
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
		for ( final int key : m.keySet() )
			put( key, m.get( key ) );
	}

	@Override
	public void putAll( final TIntObjectMap< ? extends V > map )
	{
		for ( final int key : map.keys() )
			put( key, map.get( key ) );
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
		final int[] array = new int[ size ];
		return keys( array );
	}

	@Override
	public int[] keys( final int[] array )
	{
		final int[] arr;
		if ( array.length < size() )
		{
			arr = new int[ size() ];
		}
		else
		{
			arr = array;
		}
		int index = 0;
		for ( int i = 0; i < keyToObjMap.size(); i++ )
		{
			final V val = keyToObjMap.get( i );
			if ( val == null )
				continue;
			arr[ index++ ] = i;
		}
		for ( int i = index; i < array.length; i++ )
		{
			arr[ i ] = NO_ENTRY_KEY;
		}
		return arr;
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
		return valueCollection().toArray();
	}

	@Override
	public V[] values( final V[] array )
	{
		return valueCollection().toArray( array );
	}

	@Override
	public TIntObjectIterator< V > iterator()
	{
		return new TIntObjectIterator< V >()
		{
			private int cursor = -1;

			@Override
			public void advance()
			{
				cursor++;
				while ( keyToObjMap.get( cursor ) == null )
				{
					cursor++;
				}
			}

			@Override
			public boolean hasNext()
			{
				int explorer = cursor + 1;
				while ( explorer < keyToObjMap.size() )
				{
					if ( keyToObjMap.get( explorer ) != null )
						return true;
					explorer++;
				}
				return false;
			}

			@Override
			public void remove()
			{
				TIntObjectArrayMap.this.remove( cursor );
			}

			@Override
			public int key()
			{
				return cursor;
			}

			@Override
			public V value()
			{
				return keyToObjMap.get( cursor );
			}

			@Override
			public V setValue( final V val )
			{
				final V v = put( cursor, val );
				return v;
			}
		};
	}

	@Override
	public boolean forEachKey( final TIntProcedure procedure )
	{
		return keySet().forEach( procedure );
	}

	@Override
	public boolean forEachValue( final TObjectProcedure< ? super V > procedure )
	{
		final TIntObjectIterator< V > it = iterator();
		while ( it.hasNext() )
		{
			it.advance();
			if ( !procedure.execute( it.value() ) )
				return false;
		}
		return true;
	}

	@Override
	public boolean forEachEntry( final TIntObjectProcedure< ? super V > procedure )
	{
		final TIntObjectIterator< V > it = iterator();
		while ( it.hasNext() )
		{
			it.advance();
			if ( !procedure.execute( it.key(), it.value() ) )
				return false;
		}
		return true;
	}

	@Override
	public void transformValues( final TObjectFunction< V, V > function )
	{
		final TIntObjectIterator< V > it = iterator();
		while ( it.hasNext() )
		{
			it.advance();
			final V newValue = function.execute( it.value() );
			it.setValue( newValue );
		}
	}

	@Override
	public boolean retainEntries( final TIntObjectProcedure< ? super V > procedure )
	{
		final TIntObjectIterator< V > it = iterator();
		boolean changed = false;
		while ( it.hasNext() )
		{
			it.advance();
			if ( !procedure.execute( it.key(), it.value() ) )
			{
				it.remove();
				changed = true;

			}
		}
		return changed;
	}

	@Override
	public String toString()
	{
		if ( size < 1 )
			return super.toString() + " {}";

		final StringBuilder str = new StringBuilder();
		str.append( super.toString() );
		str.append( " { " );
		final int[] keys = keys();
		str.append( keys[ 0 ] + " -> " + get( keys[ 0 ] ) );
		for ( int i = 1; i < keys.length; i++ )
		{
			final int key = keys[ i ];
			str.append( ", " + key + " -> " + get( key ) );
		}
		str.append( " }" );
		return str.toString();
	}
}

