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
import java.util.Map;
import java.util.NoSuchElementException;

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

	/**
	 * Int value for no entry. We use -1 because ref objects cannot have an
	 * internal pool index lower than 0.
	 */
	private static final int NO_ENTRY_VALUE = -1;

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
		if ( containsKey( key ) )
			return get( key );
		put( key, value );
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
		final TIntIterator it = keyToIndexMap.iterator();
		int index = 0;
		while ( it.hasNext() )
		{
			final int val = it.next();
			if ( val < 0 )
				continue;
			array[ index++ ] = val;
		}
		return array;
	}

	// === TODO === UNIMPLEMENTED ========

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
	 * PRIVATE CLASS
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
			return keyToIndexMap.contains( entry );
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
						final int next = keyToIndexMap.get( cursor );
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
				if ( val == NO_ENTRY_KEY )
					continue;
				final boolean ok = procedure.execute( val );
				if ( !ok )
					return false;
			}
			return true;
		}
	}
}
