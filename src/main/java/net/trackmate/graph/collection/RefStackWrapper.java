package net.trackmate.graph.collection;

import java.util.Deque;
import java.util.Iterator;

/**
 * Wraps a {@link Deque} as a {@link RefStack}.
 *
 * @param <O> the type of elements maintained by this stack.
 *
 * @author Tobias Pietzsch <tobias.pietzsch@gmail.com>
 */
public class RefStackWrapper< O > extends AbstractRefCollectionWrapper< O, Deque< O > > implements RefStack< O >
{
	RefStackWrapper( final Deque< O > deque )
	{
		super( deque );
	}

	@Override
	public void push( final O obj )
	{
		collection.push( obj );
	}

	@Override
	public O peek()
	{
		return collection.peek();
	}

	@Override
	public O peek( final O obj )
	{
		return collection.peek();
	}

	@Override
	public O pop()
	{
		return collection.pop();
	}

	@Override
	public O pop( final O obj )
	{
		return collection.pop();
	}

	@Override
	public int search( final Object obj )
	{
		final Iterator< O > iter = collection.descendingIterator();
		int i = 1;
		while ( iter.hasNext() )
		{
			if ( iter.next().equals( obj ) )
				return i;
			++i;
		}
		return -1;
	}
}
