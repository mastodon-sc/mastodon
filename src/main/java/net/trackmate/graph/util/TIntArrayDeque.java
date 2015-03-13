package net.trackmate.graph.util;

import static gnu.trove.impl.Constants.DEFAULT_CAPACITY;
import gnu.trove.TIntCollection;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.procedure.TIntProcedure;

import java.util.Collection;
import java.util.NoSuchElementException;

public class TIntArrayDeque implements TIntCollection
{
	public void debugprint()
	{
		StringBuilder sb = new StringBuilder();
		sb.append( "[ " );
		int tailPos = 0;
		int headPos = 0;
		for ( int i = 0; i < elements.length; ++i )
		{
			if ( tail == i )
				tailPos = sb.length();
			if ( head == i )
				headPos = sb.length();
			if ( ( i >= tail && i < head ) || ( tail >= head && ( i >= tail || i < head ) ) )
				sb.append( "  ." );
			else
				sb.append( String.format( "%3d", elements[ i ] ) );
			if ( i < elements.length - 1 )
				sb.append( ", " );
		}
		sb.append( " ]" );
		final String elementsString = sb.toString();

		sb = new StringBuilder();
		for ( int i = 0; i < headPos; ++i )
			sb.append( " " );
		sb.append( "^^^" );
		final String headString = sb.toString();

		sb = new StringBuilder();
		for ( int i = 0; i < tailPos; ++i )
			sb.append( " " );
		sb.append( "___" );
		final String tailString = sb.toString();

		System.out.println( tailString );
		System.out.println( elementsString );
		System.out.println( headString );
	}

	private int[] elements;

	/**
	 * index of the head element.
	 */
	private int head;

	/**
	 * index of the element after the last.
	 */
	private int tail;

	private final int no_entry_value;

	public TIntArrayDeque()
	{
		this( DEFAULT_CAPACITY, 0 );
	}

	public TIntArrayDeque( final int capacity )
	{
		this( capacity, 0 );
	}

	public TIntArrayDeque( final int capacity, final int no_entry_value )
	{
		this.no_entry_value = no_entry_value;
		elements = new int[ capacity ];
		head = 0;
		tail = 0;
	}

	private void growIfNecessary()
	{
		if ( head == tail )
		{
			if ( elements.length == Integer.MAX_VALUE )
				throw new ArrayIndexOutOfBoundsException( "cannot grow elements array any further" );
			final int new_capacity = ( int ) Math.min( Integer.MAX_VALUE, ( long ) elements.length * 3 / 2 );
			final int[] old = elements;
			elements = new int[ new_capacity ];
			System.arraycopy( old, 0, elements, 0, tail );
			final int oldhead = head;
			head += ( new_capacity - old.length );
			System.arraycopy( old, oldhead, elements, head, old.length - oldhead );
		}
	}

	public void addFirst( final int entry )
	{
		if ( head == 0 )
			head = elements.length;
		elements[ --head ] = entry;
		growIfNecessary();
	}

	public void addLast( final int entry )
	{
		elements[ tail++ ] = entry;
		if ( tail == elements.length )
			tail = 0;
		growIfNecessary();
	}

	private int pollFirstUnchecked()
	{
		final int element = elements[ head++ ];
		if ( head == elements.length )
			head = 0;
		return element;
	}

	private int pollLastUnchecked()
	{
		if ( tail == 0 )
			tail = elements.length;
		return elements[ --tail ];
	}

	private int peekFirstUnchecked()
	{
		return elements[ head ];
	}

	private int peekLastUnchecked()
	{
		return elements[ ( tail == 0 ) ? elements.length - 1 : tail ];
	}

	public int pollFirst()
	{
		return isEmpty() ? no_entry_value : pollFirstUnchecked();
	}

	public int pollLast()
	{
		return isEmpty() ? no_entry_value : pollLastUnchecked();
	}

	public int removeFirst()
	{
		if ( isEmpty() )
			throw new NoSuchElementException();
		return pollFirstUnchecked();
	}

	public int removeLast()
	{
		if ( isEmpty() )
			throw new NoSuchElementException();
		return pollLastUnchecked();
	}

	public int peekFirst()
	{
		return isEmpty() ? no_entry_value : peekFirstUnchecked();
	}

	public int peekLast()
	{
		return isEmpty() ? no_entry_value : peekLastUnchecked();
	}

	public int getFirst()
	{
		if ( isEmpty() )
			throw new NoSuchElementException();
		return peekFirstUnchecked();
	}

	public int getLast()
	{
		if ( isEmpty() )
			throw new NoSuchElementException();
		return peekLastUnchecked();
	}

	@Override
	public int getNoEntryValue()
	{
		return no_entry_value;
	}

	@Override
	public int size()
	{
		final int s = tail - head;
		return s < 0 ? elements.length + s : s;
	}

	@Override
	public boolean isEmpty()
	{
		return tail == head;
	}

	@Override
	public void clear()
	{
		head = tail = 0;
	}

	/**
	 * For internal use. Remove element at index {@code i} of the underlying
	 * array. Adjust head and tail indices accordingly.
	 *
	 * @param i
	 * @return {@code true} if the next element (after the one {@code i} was
	 *         pointing to) is now at index i. {@code false} if the previous
	 *         element (before the one {@code i} was pointing to) is now at
	 *         index i.
	 */
	boolean removeAt( final int i )
	{
		if ( tail < head )
		{
			if ( i < tail )
			{
				System.arraycopy( elements, i + 1, elements, i, tail - i - 1 );
				--tail;
				return true;
			}
			else
			{
				System.arraycopy( elements, head, elements, head + 1, i - head );
				if ( ++head == elements.length )
					head = 0;
				return false;
			}
		}
		else
		{
			final int moveFwdLen = i - head;
			final int moveBckLen = tail - i - 1;
			if ( moveFwdLen < moveBckLen )
			{
				System.arraycopy( elements, head, elements, head + 1, moveFwdLen );
				++head;
				return false;
			}
			else
			{
				System.arraycopy( elements, i + 1, elements, i, moveBckLen );
				--tail;
				return true;
			}
		}
	}

	@Override
	public TIntIterator iterator()
	{
		return new TIntIterator()
		{
			private int current = head;

			private int prev = 0;

			@Override
			public boolean hasNext()
			{
				if ( tail > head )
					return current < tail;
				else if ( head > tail )
					return current < tail || current >= head;
				else
					return false;
			}

			@Override
			public int next()
			{
				prev = current;
				final int element = elements[ current++ ];
				if ( current == elements.length )
					current = 0;
				return element;
			}

			@Override
			public void remove()
			{
				if ( removeAt( prev ) && --current < 0 )
					current = elements.length - 1;
			}
		};
	}

	public TIntIterator descendingIterator()
	{
		return new TIntIterator()
		{
			private int current = tail;

			@Override
			public boolean hasNext()
			{
				if ( tail > head )
					return current > head;
				else if ( head > tail )
					return current <= tail  || current > head;
				else
					return false;
			}

			@Override
			public int next()
			{
				if ( current == 0 )
					current = elements.length;
				final int element = elements[ --current ];
				return element;
			}

			@Override
			public void remove()
			{
				if ( !removeAt( current ) )
					++current;
			}
		};
	}

	@Override
	public boolean contains( final int entry )
	{
		if ( tail > head )
		{
			for ( int i = head; i < tail; ++i )
				if ( elements[ i ] == entry )
					return true;
			return false;
		}
		else if ( head > tail )
		{
			for ( int i = tail + 1; i < elements.length; ++i )
				if ( elements[ i ] == entry )
					return true;
			for ( int i = 0; i <= head; ++i )
				if ( elements[ i ] == entry )
					return true;
			return false;
		}
		else
			return false;
	}

	@Override
	public boolean add( final int entry )
	{
		addLast( entry );
		return true;
	}

	@Override
	public boolean remove( final int entry )
	{
        if ( entry == no_entry_value )
            return false;
		if ( tail > head )
		{
			for ( int i = head; i < tail; ++i )
				if ( elements[ i ] == entry )
				{
					removeAt( i );
					return true;
				}
			return false;
		}
		else if ( head > tail )
		{
			for ( int i = head; i < elements.length; ++i )
				if ( elements[ i ] == entry )
				{
					removeAt( i );
					return true;
				}
			for ( int i = 0; i < tail; ++i )
				if ( elements[ i ] == entry )
				{
					removeAt( i );
					return true;
				}
			return false;
		}
		else
			return false;
	}

	public boolean removeLastOccurrence( final int entry )
	{
        if ( entry == no_entry_value )
            return false;
		if ( tail > head )
		{
			for ( int i = tail - 1; i >= head; --i )
				if ( elements[ i ] == entry )
				{
					removeAt( i );
					return true;
				}
			return false;
		}
		else if ( head > tail )
		{
			for ( int i = tail - 1; i >= 0; --i )
				if ( elements[ i ] == entry )
				{
					removeAt( i );
					return true;
				}
			for ( int i = elements.length - 1; i >= head ; --i )
				if ( elements[ i ] == entry )
				{
					removeAt( i );
					return true;
				}
			return false;
		}
		else
			return false;
	}

	@Override
	public boolean containsAll( final Collection< ? > collection )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean containsAll( final TIntCollection collection )
	{
		final TIntIterator iter = collection.iterator();
		while ( iter.hasNext() )
			if ( !contains( iter.next() ) )
				return false;
		return true;
	}

	@Override
	public boolean containsAll( final int[] array )
	{
		for ( final int i : array )
			if ( !contains( i ) )
				return false;
		return true;
	}

	@Override
	public boolean addAll( final Collection< ? extends Integer > collection )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean addAll( final TIntCollection collection )
	{
		if ( collection.isEmpty() )
			return false;
		final TIntIterator iter = collection.iterator();
		while ( iter.hasNext() )
			addLast( iter.next() );
		return true;
	}

	@Override
	public boolean addAll( final int[] array )
	{
		if ( array.length == 0 )
			return false;
		for ( final int i : array )
			addLast( i );
		return true;
	}

	@Override
	public boolean retainAll( final Collection< ? > collection )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean retainAll( final TIntCollection collection )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean retainAll( final int[] array )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean removeAll( final Collection< ? > collection )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean removeAll( final TIntCollection collection )
	{
		if ( collection.isEmpty() )
			return false;
		final TIntIterator iter = collection.iterator();
		boolean ret = false;
		while ( iter.hasNext() )
			ret |= remove( iter.next() );
		return ret;
	}

	@Override
	public boolean removeAll( final int[] array )
	{
		if ( array.length == 0 )
			return false;
		boolean ret = false;
		for ( final int i : array )
			ret |= remove( i );
		return ret;
	}

	@Override
	public boolean forEach( final TIntProcedure procedure )
	{
		final TIntIterator iter = iterator();
		while ( iter.hasNext() )
			if ( !procedure.execute( iter.next() ) )
				return false;
		return true;
	}

	@Override
	public int[] toArray()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public int[] toArray( final int[] dest )
	{
		throw new UnsupportedOperationException();
	}
}