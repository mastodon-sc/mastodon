package gnu.trove.map;

import java.util.ArrayList;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

import gnu.trove.TIntCollection;
import gnu.trove.function.TObjectFunction;
import gnu.trove.impl.Constants;
import gnu.trove.iterator.TIntIterator;
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
		return new KeySetView();
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
		return new ValueCollection();
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

	/*
	 * PRIVATE CLASSES
	 */

	private final class KeySetView implements TIntSet
	{

		@Override
		public int getNoEntryValue()
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
		public boolean contains( final int entry )
		{
			if ( entry == NO_ENTRY_KEY )
				return false;
			return keyToObjMap.size() > entry && keyToObjMap.get( entry ) != null;
		}

		@Override
		public TIntIterator iterator()
		{
			return new TIntIterator()
			{

				/**
				 * Index of element to be returned by subsequent call to next.
				 */
				private int cursor = 0;

				/**
				 * Index of element returned by most recent call to next or
				 * previous. Reset to -1 if this element is deleted by a call to
				 * remove.
				 */
				int lastRet = -1;

				/** {@inheritDoc} */
				@Override
				public boolean hasNext()
				{
					return cursor < keyToObjMap.size();
				}

				/** {@inheritDoc} */
				@Override
				public int next()
				{
					try
					{
						while ( keyToObjMap.get( cursor ) == null )
						{
							cursor++;
						}
						final int next = cursor;
						lastRet = cursor++;
						// Advance to next now.
						while ( cursor < keyToObjMap.size() && keyToObjMap.get( cursor ) == null )
						{
							cursor++;
						}
						if ( cursor >= keyToObjMap.size() )
							cursor = Integer.MAX_VALUE;
						return next;
					}
					catch ( final IndexOutOfBoundsException e )
					{
						throw new NoSuchElementException();
					}
				}

				/** {@inheritDoc} */
				@Override
				public void remove()
				{
					if ( lastRet == -1 )
						throw new IllegalStateException();

					try
					{
						TIntObjectArrayMap.this.remove( lastRet );
						if ( lastRet < cursor )
							cursor--;
						lastRet = -1;
					}
					catch ( final IndexOutOfBoundsException e )
					{
						throw new ConcurrentModificationException();
					}
				}
			};
		}

		@Override
		public int[] toArray()
		{
			return keys();
		}

		@Override
		public int[] toArray( final int[] dest )
		{
			return keys( dest );
		}

		@Override
		public boolean add( final int entry )
		{
			throw new UnsupportedOperationException( "add is not supported for keyset view." );
		}

		@Override
		public boolean remove( final int entry )
		{
			final V removed = TIntObjectArrayMap.this.remove( entry );
			return ( removed != null );
		}

		@Override
		public boolean containsAll( final Collection< ? > collection )
		{
			final Iterator< ? > it = collection.iterator();
			while ( it.hasNext() )
			{
				final Object obj = it.next();
				if ( !( obj instanceof Integer ) )
					return false;

				if ( !TIntObjectArrayMap.this.containsKey( ( Integer ) obj ) )
					return false;
			}
			return true;
		}

		@Override
		public boolean containsAll( final TIntCollection collection )
		{
			final TIntIterator it = collection.iterator();
			while ( it.hasNext() )
			{
				if ( !TIntObjectArrayMap.this.containsKey( it.next() ) )
					return false;
			}
			return true;
		}

		@Override
		public boolean containsAll( final int[] array )
		{
			for ( final int key : array )
			{
				if ( !TIntObjectArrayMap.this.containsKey( key ) )
					return false;
			}
			return true;
		}

		@Override
		public boolean addAll( final Collection< ? extends Integer > collection )
		{
			throw new UnsupportedOperationException( "addAll is not supported for keyset view." );
		}

		@Override
		public boolean addAll( final TIntCollection collection )
		{
			throw new UnsupportedOperationException( "addAll is not supported for keyset view." );
		}

		@Override
		public boolean addAll( final int[] array )
		{
			throw new UnsupportedOperationException( "addAll is not supported for keyset view." );
		}

		@Override
		public boolean retainAll( final Collection< ? > collection )
		{
			boolean changed = false;
			for ( final int entry : keys() )
			{
				if ( !collection.contains( entry ) )
				{
					final V removed = TIntObjectArrayMap.this.remove( entry );
					if ( removed != null )
						changed = true;
				}
			}
			return changed;
		}

		@Override
		public boolean retainAll( final TIntCollection collection )
		{
			boolean changed = false;
			for ( final int entry : keys() )
			{
				if ( !collection.contains( entry ) )
				{
					final V removed = TIntObjectArrayMap.this.remove( entry );
					if ( removed != null )
						changed = true;
				}
			}
			return changed;
		}

		@Override
		public boolean retainAll( final int[] array )
		{
			boolean changed = false;
			for ( final int entry : keys() )
			{
				boolean found = false;
				for ( final int in : array )
				{
					if ( entry == in )
					{
						found = true;
						break;
					}
				}
				if ( !found )
				{
					final V removed = TIntObjectArrayMap.this.remove( entry );
					if ( removed != null )
						changed = true;
				}
			}
			return changed;
		}

		@Override
		public boolean removeAll( final Collection< ? > collection )
		{
			boolean changed = false;
			for ( final Object obj : collection )
			{
				if ( obj instanceof Integer )
				{
					final boolean removed = remove( ( int ) obj );
					if ( removed )
						changed = true;
				}
			}
			return changed;
		}

		@Override
		public boolean removeAll( final TIntCollection collection )
		{
			boolean changed = false;
			final TIntIterator it = collection.iterator();
			while ( it.hasNext() )
			{
				final int entry = it.next();
				final boolean removed = remove( entry );
				if ( removed )
					changed = true;
			}
			return changed;
		}

		@Override
		public boolean removeAll( final int[] array )
		{
			boolean changed = false;
			for ( final int entry : array )
			{
				final boolean removed = remove( entry );
				if ( removed )
					changed = true;
			}
			return changed;
		}

		@Override
		public void clear()
		{
			TIntObjectArrayMap.this.clear();
		}

		@Override
		public boolean forEach( final TIntProcedure procedure )
		{
			for ( int i = 0; i < keyToObjMap.size(); i++ )
			{
				final V val = keyToObjMap.get( i );
				if ( val == null )
					continue;
				final boolean ok = procedure.execute( i );
				if ( !ok )
					return false;
			}
			return true;
		}
	}

	private class ValueCollection implements Collection< V >
	{

		@Override
		public boolean add( final V value )
		{
			throw new UnsupportedOperationException( "add is not supported for valueCollection view." );
		}

		@Override
		public boolean addAll( final Collection< ? extends V > c )
		{
			throw new UnsupportedOperationException( "addAll is not supported for valueCollection view." );
		}

		@Override
		public void clear()
		{
			TIntObjectArrayMap.this.clear();
		}

		@Override
		public boolean contains( final Object value )
		{
			return TIntObjectArrayMap.this.containsValue( value );
		}

		@Override
		public boolean containsAll( final Collection< ? > c )
		{
			for ( final Object value : c )
			{
				if ( !contains( value ) )
					return false;
			}
			return true;
		}

		@Override
		public boolean isEmpty()
		{
			return TIntObjectArrayMap.this.isEmpty();
		}

		@Override
		public Iterator< V > iterator()
		{
			return new Iterator< V >()
			{

				/**
				 * Index of element to be returned by subsequent call to next.
				 */
				private int cursor = 0;

				/**
				 * Index of element returned by most recent call to next or
				 * previous. Reset to -1 if this element is deleted by a call to
				 * remove.
				 */
				int lastRet = -1;

				/** {@inheritDoc} */
				@Override
				public boolean hasNext()
				{
					return cursor < keyToObjMap.size();
				}

				/** {@inheritDoc} */
				@Override
				public V next()
				{
					try
					{
						while ( keyToObjMap.get( cursor ) == null )
						{
							cursor++;
						}
						final V next = keyToObjMap.get( cursor );
						lastRet = cursor++;
						// Advance to next now.
						while ( cursor < keyToObjMap.size() && keyToObjMap.get( cursor ) == null )
						{
							cursor++;
						}
						if ( cursor >= keyToObjMap.size() )
							cursor = Integer.MAX_VALUE;

						return next;
					}
					catch ( final IndexOutOfBoundsException e )
					{
						throw new NoSuchElementException();
					}
				}

				/** {@inheritDoc} */
				@Override
				public void remove()
				{
					if ( lastRet == -1 )
						throw new IllegalStateException();

					try
					{
						TIntObjectArrayMap.this.remove( lastRet );
						if ( lastRet < cursor )
							cursor--;
						lastRet = -1;
					}
					catch ( final IndexOutOfBoundsException e )
					{
						throw new ConcurrentModificationException();
					}
				}
			};
		}

		@Override
		public boolean remove( final Object obj )
		{
			final int key = keyToObjMap.indexOf( obj );
			if ( key < 0 )
				return false;

			--size;
			keyToObjMap.set( key, null );
			return true;
		}

		@Override
		public boolean removeAll( final Collection< ? > c )
		{
			boolean changed = false;
			for ( final Object obj : c )
			{
				changed = remove( obj ) || changed;
			}
			return changed;
		}

		@Override
		public boolean retainAll( final Collection< ? > c )
		{
			boolean changed = false;
			for ( final Object obj : this )
			{
				if ( c.contains( obj ) )
					continue;
				changed = remove( obj ) || changed;
			}
			return changed;
		}

		@Override
		public int size()
		{
			return TIntObjectArrayMap.this.size();
		}

		@Override
		public Object[] toArray()
		{
			return toArray( new Object[ size() ] );
		}

		@SuppressWarnings( "unchecked" )
		@Override
		public < T > T[] toArray( final T[] a )
		{
			final Object[] arr;
			if ( a.length < size() )
			{
				arr = new Object[ size() ];
			}
			else
			{
				arr = a;
			}

			int i = 0;
			for ( final int key : keys() )
			{
				arr[ i++ ] = get( key );
			}
			// nullify the rest.
			for ( int j = i; j < arr.length; j++ )
			{
				arr[ j ] = null;
			}
			return ( T[] ) arr;
		}
	}
}
