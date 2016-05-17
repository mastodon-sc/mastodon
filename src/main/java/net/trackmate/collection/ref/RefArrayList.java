package net.trackmate.collection.ref;

import java.util.Collection;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Random;

import gnu.trove.TIntCollection;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.list.array.TIntArrayList;
import net.trackmate.Ref;
import net.trackmate.RefPool;
import net.trackmate.collection.RefList;

public class RefArrayList< O extends Ref< O > > implements IntBackedRefCollection< O >, RefList< O >
{
	private final TIntArrayList indices;

	private final RefPool< O > pool;

	public RefArrayList( final RefPool< O > pool )
	{
		this.pool = pool;
		indices = new TIntArrayList();
	}

	public RefArrayList( final RefPool< O > pool, final int initialCapacity )
	{
		this.pool = pool;
		indices = new TIntArrayList( initialCapacity );
	}

	protected RefArrayList( final RefArrayList< O > list, final TIntArrayList indexSubList )
	{
		pool = list.pool;
		indices = indexSubList;
	}

	@Override
	public O createRef()
	{
		return pool.createRef();
	}

	@Override
	public void releaseRef( final O obj )
	{
		pool.releaseRef( obj );
	}

	@Override
	public TIntArrayList getIndexCollection()
	{
		return indices;
	}

	@Override
	public boolean add( final O obj )
	{
		return indices.add( obj.getInternalPoolIndex() );
	}

	@Override
	public void add( final int index, final O obj )
	{
		indices.insert( index, obj.getInternalPoolIndex() );
	}

	// TODO: consider throwing exception in addAll if objs are not from same pool
	@Override
	public boolean addAll( final Collection< ? extends O > objs )
	{
		if ( objs instanceof IntBackedRefCollection )
			return indices.addAll( ( ( IntBackedRefCollection< ? > ) objs ).getIndexCollection() );
		else
		{
			for ( final O obj : objs )
				indices.add( obj.getInternalPoolIndex() );
			return !objs.isEmpty();
		}
	}

	@Override
	public boolean addAll( final int index, final Collection< ? extends O > objs )
	{
		if ( objs instanceof IntBackedRefCollection )
		{
			final TIntCollection objIndices = ( ( IntBackedRefCollection< ? > ) objs ).getIndexCollection();
			indices.insert( index, objIndices.toArray() );
		}
		else
		{
			final int[] indicesToInsert = new int[ objs.size() ];
			int i = 0;
			for ( final O obj : objs )
				indicesToInsert[ i++ ] = obj.getInternalPoolIndex();
			indices.insert( index, indicesToInsert );
		}
		return !objs.isEmpty();
	}

	@Override
	public void clear()
	{
		indices.clear();
	}

	/**
	 * Sets the size of the list to 0, but does not change its capacity. This
	 * method can be used as an alternative to the {@link #clear()} method if
	 * you want to recycle a list without allocating new backing arrays.
	 *
	 * @see TIntArrayList#reset()
	 */
	public void reset()
	{
		indices.reset();
	}

	/**
	 * Sets the size of the list to 0, but does not change its capacity. This
	 * method can be used as an alternative to the {@link #clear()} method if
	 * you want to recycle a list without allocating new backing arrays. This
	 * method differs from {@link #reset()} in that it does not clear the old
	 * values in the backing array. Thus, it is possible for getQuick to return
	 * stale data if this method is used and the caller is careless about bounds
	 * checking.
	 *
	 * @see TIntArrayList#resetQuick()
	 */
	public void resetQuick()
	{
		indices.resetQuick();
	}

	@Override
	public boolean contains( final Object obj )
	{
		return ( obj instanceof Ref )
				? indices.contains( ( ( Ref< ? > ) obj ).getInternalPoolIndex() )
				: false;
	}

	@Override
	public boolean containsAll( final Collection< ? > objs )
	{
		if ( objs instanceof IntBackedRefCollection )
			return indices.containsAll( ( ( IntBackedRefCollection< ? > ) objs ).getIndexCollection() );
		else
		{
			for ( final Object obj : objs )
				if ( !contains( obj ) )
					return false;
			return true;
		}
	}

	public O getQuick( final int index, final O obj )
	{
		pool.getByInternalPoolIndex( indices.getQuick( index ), obj );
		return obj;
	}

	@Override
	public O get( final int index, final O obj )
	{
		pool.getByInternalPoolIndex( indices.get( index ), obj );
		return obj;
	}

	@Override
	public O get( final int index )
	{
		return get( index, pool.createRef() );
	}

	@Override
	public int indexOf( final Object obj )
	{
		return ( obj instanceof Ref )
				? indices.indexOf( ( ( Ref< ? > ) obj ).getInternalPoolIndex() )
				: -1;
	}

	@Override
	public boolean isEmpty()
	{
		return indices.isEmpty();
	}

	@Override
	public Iterator< O > iterator()
	{
		return new Iterator< O >()
		{
			final TIntIterator ii = indices.iterator();

			final O obj = pool.createRef();

			@Override
			public boolean hasNext()
			{
				return ii.hasNext();
			}

			@Override
			public O next()
			{
				pool.getByInternalPoolIndex( ii.next(), obj );
				return obj;
			}

			@Override
			public void remove()
			{
				ii.remove();
			}
		};
	}

	@Override
	public int lastIndexOf( final Object obj )
	{
		return ( obj instanceof Ref )
				? indices.lastIndexOf( ( ( Ref< ? > ) obj ).getInternalPoolIndex() )
				: -1;
	}

	// TODO: modCount is not updated currently.
	private final int modCount = 0;

	/**
	 * Shamelessly stolen from java.util.AbstractList
	 */
	private class ListItr implements ListIterator< O >
	{
		final O obj = pool.createRef();

		/**
		 * Index of element to be returned by subsequent call to next.
		 */
		int cursor;

		/**
		 * Index of element returned by most recent call to next or previous.
		 * Reset to -1 if this element is deleted by a call to remove.
		 */
		int lastRet = -1;

		/**
		 * The modCount value that the iterator believes that the backing List
		 * should have. If this expectation is violated, the iterator has
		 * detected concurrent modification.
		 */
		int expectedModCount = modCount;

		ListItr( final int index )
		{
			cursor = index;
		}

		@Override
		public boolean hasNext()
		{
			return cursor != size();
		}

		@Override
		public O next()
		{
			checkForComodification();
			try
			{
				final int i = cursor;
				final O next = get( i, obj );
				lastRet = i;
				cursor = i + 1;
				return next;
			}
			catch ( final IndexOutOfBoundsException e )
			{
				checkForComodification();
				throw new NoSuchElementException();
			}
		}

		@Override
		public void remove()
		{
			if ( lastRet < 0 )
				throw new IllegalStateException();
			checkForComodification();

			try
			{
				RefArrayList.this.remove( lastRet );
				if ( lastRet < cursor )
					cursor--;
				lastRet = -1;
				expectedModCount = modCount;
			}
			catch ( final IndexOutOfBoundsException e )
			{
				throw new ConcurrentModificationException();
			}
		}

		@Override
		public boolean hasPrevious()
		{
			return cursor != 0;
		}

		@Override
		public O previous()
		{
			checkForComodification();
			try
			{
				final int i = cursor - 1;
				final O previous = get( i, obj );
				lastRet = cursor = i;
				return previous;
			}
			catch ( final IndexOutOfBoundsException e )
			{
				checkForComodification();
				throw new NoSuchElementException();
			}
		}

		@Override
		public int nextIndex()
		{
			return cursor;
		}

		@Override
		public int previousIndex()
		{
			return cursor - 1;
		}

		@Override
		public void set( final O o )
		{
			if ( lastRet < 0 )
				throw new IllegalStateException();
			checkForComodification();

			try
			{
				RefArrayList.this.set( lastRet, o );
				expectedModCount = modCount;
			}
			catch ( final IndexOutOfBoundsException ex )
			{
				throw new ConcurrentModificationException();
			}
		}

		@Override
		public void add( final O o )
		{
			checkForComodification();

			try
			{
				final int i = cursor;
				RefArrayList.this.add( i, o );
				lastRet = -1;
				cursor = i + 1;
				expectedModCount = modCount;
			}
			catch ( final IndexOutOfBoundsException ex )
			{
				throw new ConcurrentModificationException();
			}
		}

		final void checkForComodification()
		{
			if ( modCount != expectedModCount )
				throw new ConcurrentModificationException();
		}
	}

    @Override
	public ListIterator< O > listIterator()
	{
		return new ListItr( 0 );
	}

	@Override
	public ListIterator< O > listIterator( final int index )
	{
		return new ListItr( index );
	}

	@Override
	public boolean remove( final Object obj )
	{
		return ( obj instanceof Ref )
				? indices.remove( ( ( Ref< ? > ) obj ).getInternalPoolIndex() )
				: false;
	}

	@Override
	public O remove( final int index, final O obj )
	{
		pool.getByInternalPoolIndex( indices.removeAt( index ), obj );
		return obj;
	}

	@Override
	public O remove( final int index )
	{
		return remove( index, pool.createRef() );
	}

	@Override
	public boolean removeAll( final Collection< ? > objs )
	{
		if ( objs instanceof IntBackedRefCollection )
			return indices.removeAll( ( ( IntBackedRefCollection< ? > ) objs ).getIndexCollection() );
		else
		{
			boolean changed = false;
			for ( final Object obj : objs )
				if ( remove( obj ) )
					changed = true;
			return changed;
		}
	}

	@Override
	public boolean retainAll( final Collection< ? > objs )
	{
		if ( objs instanceof IntBackedRefCollection )
			return indices.retainAll( ( ( IntBackedRefCollection< ? > ) objs ).getIndexCollection() );
		else
		{
			// TODO
			throw new UnsupportedOperationException( "not yet implemented" );
		}
	}

	@Override
	public O set( final int index, final O obj, final O replacedObj )
	{
		pool.getByInternalPoolIndex(
				indices.set( index, obj.getInternalPoolIndex() ),
				replacedObj );
		return replacedObj;
	}

	@Override
	public O set( final int index, final O obj )
	{
		return set( index, obj, pool.createRef() );
	}

	@Override
	public int size()
	{
		return indices.size();
	}

	@Override
	public List< O > subList( final int fromIndex, final int toIndex )
	{
		return new RefArrayList< O >( this, ( TIntArrayList ) indices.subList( fromIndex, toIndex ) );
	}

	@Override
	public void shuffle( final Random rand )
	{
		indices.shuffle( rand );
	}

	@Override
	public void sort( final Comparator< ? super O > comparator )
	{
		if ( indices.size() < 2 )
			return;
		quicksort( 0, size() - 1, comparator, createRef(), createRef() );
	}

	private void quicksort( final int low, final int high, final Comparator< ? super O > comparator, final O tmpRef1, final O tmpRef2 )
	{
		final O pivot = get( ( low + high ) / 2, tmpRef1 );

		int i = low;
		int j = high;

		do
		{
			while ( comparator.compare( get( i, tmpRef2 ), pivot ) < 0 )
				i++;
			while ( comparator.compare( pivot, get( j, tmpRef2 ) ) < 0 )
				j--;
			if ( i <= j )
			{
				swap( i, j );
				i++;
				j--;
			}
		}
		while ( i <= j );

		if ( low < j )
			quicksort( low, j, comparator, tmpRef1, tmpRef2 );
		if ( i < high )
			quicksort( i, high, comparator, tmpRef1, tmpRef2 );
	}

	@Override
	public void swap( final int i, final int j )
	{
		final int tmp = indices.get( i );
		indices.set( i, indices.get( j ) );
		indices.set( j, tmp );
	}

	@Override
	public Object[] toArray()
	{
		final Object[] obj = new Object[ indices.size() ];
		for ( int i = 0; i < obj.length; i++ )
		{
			obj[ i ] = get( i );
		}
		return obj;
	}

	@SuppressWarnings( "unchecked" )
	@Override
	public < A > A[] toArray( final A[] a )
	{
		return ( A[] ) toArray();
	}

	@Override
	public String toString()
	{
		if ( isEmpty() ) { return "( )"; }
		final StringBuffer sb = new StringBuffer();
		final Iterator< ? > it = iterator();
		sb.append( "( " + it.next().toString() );
		while ( it.hasNext() )
		{
			sb.append( " , " + it.next().toString() );
		}
		sb.append( " )" );
		return sb.toString();
	}
}
