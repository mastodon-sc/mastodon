package net.trackmate.collection.wrap;

import java.util.Deque;
import java.util.Iterator;

import net.trackmate.collection.RefDeque;

/**
 * Wraps a {@link Deque} as a {@link RefDeque}.
 *
 * @param <O>
 *            the type of elements maintained by this deque.
 *
 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
 */
public class RefDequeWrapper< O > extends AbstractRefCollectionWrapper< O, Deque< O > > implements RefDeque< O >
{
	public RefDequeWrapper( final Deque< O > deque )
	{
		super( deque );
	}

	@Override
	public void addFirst( final O e )
	{
		collection.addFirst( e );
	}

	@Override
	public void addLast( final O e )
	{
		collection.addLast( e );
	}

	@Override
	public boolean offerFirst( final O e )
	{
		return collection.offerFirst( e );
	}

	@Override
	public boolean offerLast( final O e )
	{
		return collection.offerLast( e );
	}

	@Override
	public O removeFirst()
	{
		return collection.removeFirst();
	}

	@Override
	public O removeLast()
	{
		return collection.removeLast();
	}

	@Override
	public O pollFirst()
	{
		return collection.pollFirst();
	}

	@Override
	public O pollLast()
	{
		return collection.pollLast();
	}

	@Override
	public O getFirst()
	{
		return collection.getFirst();
	}

	@Override
	public O getLast()
	{
		return collection.getLast();
	}

	@Override
	public O peekFirst()
	{
		return collection.peekFirst();
	}

	@Override
	public O peekLast()
	{
		return collection.peekLast();
	}

	@Override
	public boolean removeFirstOccurrence( final Object o )
	{
		return collection.removeFirstOccurrence( o );
	}

	@Override
	public boolean removeLastOccurrence( final Object o )
	{
		return collection.removeLastOccurrence( o );
	}

	@Override
	public boolean offer( final O e )
	{
		return collection.offer( e );
	}

	@Override
	public O remove()
	{
		return collection.remove();
	}

	@Override
	public O poll()
	{
		return collection.poll();
	}

	@Override
	public O element()
	{
		return collection.element();
	}

	@Override
	public O peek()
	{
		return collection.peek();
	}

	@Override
	public void push( final O e )
	{
		collection.push( e );
	}

	@Override
	public O pop()
	{
		return collection.pop();
	}

	@Override
	public Iterator< O > descendingIterator()
	{
		return collection.descendingIterator();
	}

	@Override
	public O removeFirst( final O obj )
	{
		return collection.removeFirst();
	}

	@Override
	public O removeLast( final O obj )
	{
		return collection.removeLast();
	}

	@Override
	public O pollFirst( final O obj )
	{
		return collection.pollFirst();
	}

	@Override
	public O pollLast( final O obj )
	{
		return collection.pollLast();
	}

	@Override
	public O getFirst( final O obj )
	{
		return collection.getFirst();
	}

	@Override
	public O getLast( final O obj )
	{
		return collection.getLast();
	}

	@Override
	public O peekFirst( final O obj )
	{
		return collection.peekFirst();
	}

	@Override
	public O peekLast( final O obj )
	{
		return collection.peekLast();
	}

	@Override
	public O poll( final O obj )
	{
		return collection.poll();
	}

	@Override
	public O element( final O obj )
	{
		return collection.element();
	}

	@Override
	public O peek( final O obj )
	{
		return collection.peek();
	}

	@Override
	public O pop( final O obj )
	{
		return collection.pop();
	}
}
