package net.trackmate.graph;

import gnu.trove.TIntCollection;
import gnu.trove.function.TObjectFunction;
import gnu.trove.impl.Constants;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.procedure.TIntObjectProcedure;
import gnu.trove.procedure.TIntProcedure;
import gnu.trove.procedure.TObjectProcedure;
import gnu.trove.set.TIntSet;

import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

import net.trackmate.graph.collection.IntRefMap;

/**
 * A {@link IntRefMap} implementation backed by a {@link TIntArrayList}.
 * <p>
 * It is assumed that keys are internal pool indices of some {@link RefPool},
 * <i>i.e.</i>, keys are <em>&ge;0</em> and not arbitrarily large. This is
 * intended to provide efficient mappings between graphs, for example a model
 * graph and the corresponding TrackScheme graph.
 * <p>
 * Another side effect of the backing implementation is that the order of
 * iteration is deterministic, and corresponds to the natural order of the
 * <code>int</code> keys.
 *
 * @param <V>
 *            value type.
 *
 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
 * @author Jean-Yves Tinevez &lt;jeanyves.tinevez@gmail.com&gt;
 */
public class IntPoolObjectArrayMap< V extends Ref< V > > implements IntRefMap< V >
{

	/**
	 * Int value for no entry. We use a negative value because ref objects
	 * cannot have an internal pool index lower than 0.
	 */
	private static final int NO_ENTRY_VALUE = -2;

	/**
	 * Int value for no key.
	 */
	private static final int NO_ENTRY_KEY = -1;

	private final TIntArrayList keyToIndexMap;

	private final RefPool< V > pool;

	private int size;

	public IntPoolObjectArrayMap( final RefPool< V > pool )
	{
		this( pool, Constants.DEFAULT_CAPACITY );
	}

	public IntPoolObjectArrayMap( final RefPool< V > pool, final int initialCapacity )
	{
		this.pool = pool;
		keyToIndexMap = new TIntArrayList( initialCapacity, NO_ENTRY_VALUE );
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
			keyToIndexMap.add( NO_ENTRY_VALUE );

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
		return putIndex( key, NO_ENTRY_VALUE, obj );
	}

	@Override
	public int size()
	{
		return size;
	}

	@Override
	public int getNoEntryKey()
	{
		return NO_ENTRY_KEY;
	}

	@Override
	public boolean containsKey( final int key )
	{
		return keyToIndexMap.size() > key && keyToIndexMap.get( key ) >= 0;
	}

	@Override
	public boolean containsValue( final Object value )
	{
		if ( value != null && value instanceof Ref )
			return keyToIndexMap.contains( ( ( Ref< ? > ) value ).getInternalPoolIndex() );
		else
			return false;
	}

	@Override
	public V putIfAbsent( final int key, final V value )
	{
		return put( key, value, createRef() );
	}

	@Override
	public V putIfAbsent( final int key, final V value, final V obj )
	{
		if ( containsKey( key ) )
			return get( key, obj );
		put( key, value, obj );
		return null;
	}

	@Override
	public void putAll( final Map< ? extends Integer, ? extends V > m )
	{
		final V ref = pool.createRef();
		for ( final Integer key : m.keySet() )
		{
			put( key, m.get( key ), ref );
		}
		pool.releaseRef( ref );
	}

	@Override
	public void putAll( final TIntObjectMap< ? extends V > map )
	{
		final V ref = pool.createRef();
		final TIntIterator it = map.keySet().iterator();
		while ( it.hasNext() )
		{
			final int key = it.next();
			put( key, map.get( key ), ref );
		}
		pool.releaseRef( ref );
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
		for ( int i = 0; i < keyToIndexMap.size(); i++ )
		{
			final int val = keyToIndexMap.get( i );
			if ( val < 0 )
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

			private final V ref = pool.createRef();

			private int cursor = -1;

			@Override
			public void advance()
			{
				cursor++;
				while ( keyToIndexMap.get( cursor ) < 0 )
				{
					cursor++;
				}
			}

			@Override
			public boolean hasNext()
			{
				int explorer = cursor + 1;
				while ( explorer < keyToIndexMap.size() )
				{
					if ( keyToIndexMap.get( explorer ) != NO_ENTRY_VALUE )
						return true;
					explorer++;
				}
				return false;
			}

			@Override
			public void remove()
			{
				IntPoolObjectArrayMap.this.remove( cursor );
			}

			@Override
			public int key()
			{
				return cursor;
			}

			@Override
			public V value()
			{
				final int poolIndex = keyToIndexMap.get( cursor );
				pool.getByInternalPoolIndex( poolIndex, ref );
				return ref;
			}

			@Override
			public V setValue( final V val )
			{
				final V v = put( cursor, val, ref );
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
		final V ref = pool.createRef();
		str.append( keys[ 0 ] + " -> " + get( keys[ 0 ], ref ) );
		for ( int i = 1; i < keys.length; i++ )
		{
			final int key = keys[ i ];
			str.append( ", " + key + " -> " + get( key, ref ) );
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
			return keyToIndexMap.size() > entry && keyToIndexMap.get( entry ) != NO_ENTRY_VALUE;
		}

		@Override
		public TIntIterator iterator()
		{
			return new TIntIterator()
			{

				/** Index of element to be returned by subsequent call to next. */
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
					return cursor < keyToIndexMap.size() ;
				}

				/** {@inheritDoc} */
				@Override
				public int next()
				{
					try
					{
						while ( keyToIndexMap.get( cursor ) < 0 )
						{
							cursor++;
						}
						final int next = cursor;
						lastRet = cursor++;
						// Advance to next now.
						while ( cursor < keyToIndexMap.size() && keyToIndexMap.get( cursor ) < 0 )
						{
							cursor++;
						}
						if ( cursor >= keyToIndexMap.size() )
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
						final V ref = pool.createRef();
						IntPoolObjectArrayMap.this.remove( lastRet, ref );
						pool.releaseRef( ref );
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
			final V ref = pool.createRef();
			final V removed = IntPoolObjectArrayMap.this.remove( entry, ref );
			pool.releaseRef( ref );
			return ( removed != null );
		}

		@Override
		public boolean containsAll( final Collection< ? > collection )
		{
			return keyToIndexMap.containsAll( collection );
		}

		@Override
		public boolean containsAll( final TIntCollection collection )
		{
			return keyToIndexMap.containsAll( collection );
		}

		@Override
		public boolean containsAll( final int[] array )
		{
			return keyToIndexMap.containsAll( array );
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
			final V ref = pool.createRef();
			for ( final int entry : keys() )
			{
				if ( !collection.contains( entry ) )
				{
					final V removed = IntPoolObjectArrayMap.this.remove( entry, ref );
					if ( removed != null )
						changed = true;
				}
			}
			pool.releaseRef( ref );
			return changed;
		}

		@Override
		public boolean retainAll( final TIntCollection collection )
		{
			boolean changed = false;
			final V ref = pool.createRef();
			for ( final int entry : keys() )
			{
				if ( !collection.contains( entry ) )
				{
					final V removed = IntPoolObjectArrayMap.this.remove( entry, ref );
					if ( removed != null )
						changed = true;
				}
			}
			pool.releaseRef( ref );
			return changed;
		}

		@Override
		public boolean retainAll( final int[] array )
		{
			boolean changed = false;
			final V ref = pool.createRef();
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
					final V removed = IntPoolObjectArrayMap.this.remove( entry, ref );
					if ( removed != null )
						changed = true;
				}
			}
			pool.releaseRef( ref );
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
			IntPoolObjectArrayMap.this.clear();
		}

		@Override
		public boolean forEach( final TIntProcedure procedure )
		{
			for ( int i = 0; i < keyToIndexMap.size(); i++ )
			{
				final int val = keyToIndexMap.get( i );
				if ( val == NO_ENTRY_VALUE )
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
			IntPoolObjectArrayMap.this.clear();
		}

		@Override
		public boolean contains( final Object value )
		{
			return IntPoolObjectArrayMap.this.containsValue( value );
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
			return IntPoolObjectArrayMap.this.isEmpty();
		}

		@Override
		public Iterator< V > iterator()
		{
			return new Iterator< V >()
			{
				/** Reference to pass PoolObject instance. */
				private final V ref = pool.createRef();

				/** Index of element to be returned by subsequent call to next. */
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
					return cursor < keyToIndexMap.size();
				}

				/** {@inheritDoc} */
				@Override
				public V next()
				{
					try
					{
						while ( keyToIndexMap.get( cursor ) < 0 )
						{
							cursor++;
						}
						final int next = keyToIndexMap.get( cursor );
						lastRet = cursor++;
						// Advance to next now.
						while ( cursor < keyToIndexMap.size() && keyToIndexMap.get( cursor ) < 0 )
						{
							cursor++;
						}
						if ( cursor >= keyToIndexMap.size() )
							cursor = Integer.MAX_VALUE;

						pool.getByInternalPoolIndex( next, ref );
						return ref;
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
						final V ref = pool.createRef();
						IntPoolObjectArrayMap.this.remove( lastRet, ref );
						pool.releaseRef( ref );
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
			if ( obj instanceof PoolObject )
			{
				final PoolObject< ?, ? > o = ( PoolObject< ?, ? > ) obj;
				final int val = o.getInternalPoolIndex();
				final int key = keyToIndexMap.indexOf( val );
				if (key < 0) return false;

				--size;
				keyToIndexMap.set( key, NO_ENTRY_KEY );
				return true;
			}
			else
				return false;
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
			return IntPoolObjectArrayMap.this.size();
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
				final V ref = pool.createRef();
				arr[ i++ ] = get( key, ref );
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
