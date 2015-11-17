package net.trackmate.revised;

import gnu.trove.iterator.TIntIterator;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;

public class TIntAlternatingIterator implements TIntIterator
{
	private final TIntList list;

	private final int startValue;

	private boolean hasNext;

	private int inc;

	private int dec;

	private boolean nextIsIncrement;

	private int index;

	private int startIndex;

	/**
	 *
	 * @param list
	 *            the list to iterate. <b>Must be sorted.</b>
	 * @param startValue
	 *            the <b>value</b> to start the iterator with. It will be
	 *            searched for in the list, and the closest inferior value will
	 *            be used if not found.
	 */
	public TIntAlternatingIterator( final TIntList list, final int startValue )
	{
		this.list = list;
		this.startValue = startValue;
		init();
	}

	private void init()
	{
		if ( list.isEmpty() )
		{
			hasNext = false;
			return;
		}

		startIndex = list.binarySearch( startValue );
		if ( startIndex < 0 )
		{
			startIndex = -( 1 + startIndex );
		}

		dec = 1;
		inc = 0;
		nextIsIncrement = true;
		hasNext = true;
		prepare();
	}

	private void prepare()
	{
		if ( startIndex - dec < 0 && startIndex + inc >= list.size() )
		{
			hasNext = false;
			return;
		}

		if ( startIndex - dec < 0 || ( nextIsIncrement && startIndex + inc < list.size() ) )
		{
			index = startIndex + inc;
			inc++;
			nextIsIncrement = false;
			return;
		}
		if ( startIndex + inc >= list.size() || ( !nextIsIncrement && startIndex - dec >= 0 ) )
		{
			index = startIndex - dec;
			dec++;
			nextIsIncrement = true;
			return;
		}
	}

	@Override
	public boolean hasNext()
	{
		return hasNext;
	}

	@Override
	public void remove()
	{
		throw new UnsupportedOperationException( "remove() is not supported for this iterator." );
	}

	@Override
	public int next()
	{
		final int next = list.get( index );
		prepare();
		return next;
	}

	public static void main( final String[] args )
	{
		final TIntArrayList list = new TIntArrayList( 11 );
		for ( int i = 0; i < 11; i++ )
		{
			list.add( i );
		}

		System.out.println( "From 3:" );
		TIntAlternatingIterator it = new TIntAlternatingIterator( list, 3 );
		while ( it.hasNext )
			System.out.println( "  " + it.next() );

		System.out.println( "From 8:" );
		it = new TIntAlternatingIterator( list, 8 );
		while ( it.hasNext )
			System.out.println( "  " + it.next() );

		System.out.println( "From 0:" );
		it = new TIntAlternatingIterator( list, 0 );
		while ( it.hasNext )
			System.out.println( "  " + it.next() );

		System.out.println( "From 10:" );
		it = new TIntAlternatingIterator( list, 10 );
		while ( it.hasNext )
			System.out.println( "  " + it.next() );

		System.out.println( "From 5:" );
		it = new TIntAlternatingIterator( list, 5 );
		while ( it.hasNext )
			System.out.println( "  " + it.next() );

	}

}
