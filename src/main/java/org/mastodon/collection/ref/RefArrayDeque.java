package org.mastodon.collection.ref;

import java.util.Collection;
import java.util.Iterator;

import org.mastodon.Ref;
import org.mastodon.RefPool;
import org.mastodon.collection.RefDeque;

import gnu.trove.deque.TIntArrayDeque;
import gnu.trove.iterator.TIntIterator;

// TODO rename RefArrayDeque
/**
 * A {@link RefDeque} implementation for {@link Ref} objects, based on a Trove
 * {@link TIntArrayDeque}.
 *
 * @param <O>
 *            the type of elements maintained by this deque.
 *
 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
 */
public class RefArrayDeque< O > implements IntBackedRefCollection< O >, RefDeque< O >
{
	private final RefPool< O > pool;

	private final TIntArrayDeque indices;

	private final Class< O > elementType;

	public RefArrayDeque( final RefPool< O > pool )
	{
		this.pool = pool;
		this.indices = new TIntArrayDeque();
		elementType = pool.getRefClass();
	}

	public RefArrayDeque( final RefPool< O > pool, final int initialCapacity )
	{
		this.pool = pool;
		indices = new TIntArrayDeque( initialCapacity );
		elementType = pool.getRefClass();
	}

	/*
	 * POOL COLLECTION METHODS
	 */

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
	public TIntArrayDeque getIndexCollection()
	{
		return indices;
	}

	/*
	 * DEQUE METHODS
	 */

	@Override
	public boolean add( final O obj )
	{
		return indices.add( pool.getId( obj ) );
	}

	@Override
	public boolean addAll( final Collection< ? extends O > objs )
	{
		if ( objs instanceof IntBackedRefCollection )
			return indices.addAll( ( ( IntBackedRefCollection< ? > ) objs ).getIndexCollection() );
		else
		{
			for ( final O obj : objs )
				indices.add( pool.getId( obj ) );
			return !objs.isEmpty();
		}
	}

	@Override
	public void clear()
	{
		indices.clear();
	}

	@SuppressWarnings( "unchecked" )
	@Override
	public boolean contains( final Object obj )
	{
		return ( elementType.isInstance( obj ) )
				? indices.contains( pool.getId( ( O ) obj ) )
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
				return pool.getObject( ii.next(), obj );
			}

			@Override
			public void remove()
			{
				ii.remove();
			}
		};
	}

	@Override
	public Iterator< O > descendingIterator()
	{
		return new Iterator< O >()
		{
			final TIntIterator ii = indices.descendingIterator();

			final O obj = pool.createRef();

			@Override
			public boolean hasNext()
			{
				return ii.hasNext();
			}

			@Override
			public O next()
			{
				return pool.getObject( ii.next(), obj );
			}

			@Override
			public void remove()
			{
				ii.remove();
			}
		};
	}

	@SuppressWarnings( "unchecked" )
	@Override
	public boolean remove( final Object obj )
	{
		return ( elementType.isInstance( obj ) )
				? indices.remove( pool.getId( ( O ) obj ) )
				: false;
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
			boolean changed = false;
			for ( final Object obj : this )
				if ( !objs.contains( obj ) && remove( obj ) )
					changed = true;
			return changed;
		}
	}

	@Override
	public int size()
	{
		return indices.size();
	}

	@Override
	public Object[] toArray()
	{
		final Object[] arr = new Object[ size() ];
		int index = 0;
		for ( final O obj : this )
			arr[ index++ ] = pool.getObject( pool.getId( obj ), createRef() );
		return arr;
	}

	@SuppressWarnings( "unchecked" )
	@Override
	public < U > U[] toArray( final U[] arr )
	{
		if ( arr.length >= size() )
		{
			int index = 0;
			for ( final O obj : this )
				arr[ index++ ] = ( U ) pool.getObject( pool.getId( obj ), createRef() );
			for ( int i = index; i < arr.length; i++ )
				arr[ i ] = null;
			return arr;
		}
		return ( U[] ) toArray();
	}

	@Override
	public void addFirst( final O obj )
	{
		indices.addFirst( pool.getId( obj ) );
	}

	@Override
	public void addLast( final O obj )
	{
		indices.addLast( pool.getId( obj ) );
	}

	@Override
	public O pollFirst( final O obj )
	{
		return pool.getObject( indices.pollFirst(), obj );
	}

	@Override
	public O pollFirst()
	{
		return pollFirst( pool.createRef() );
	}

	@Override
	public O pollLast( final O obj )
	{
		return pool.getObject( indices.pollLast(), obj );
	}

	@Override
	public O pollLast()
	{
		return pollLast( pool.createRef() );
	}

	@Override
	public O peekFirst( final O obj )
	{
		return pool.getObject( indices.peekFirst(), obj );
	}

	@Override
	public O peekFirst()
	{
		return peekFirst( pool.createRef() );
	}

	@Override
	public O peekLast( final O obj )
	{
		return pool.getObject( indices.peekLast(), obj );
	}

	@Override
	public O peekLast()
	{
		return peekLast( pool.createRef() );
	}

	@Override
	public O removeFirst( final O obj )
	{
		return pool.getObject( indices.removeFirst(), obj );
	}

	@Override
	public O removeFirst()
	{
		return removeFirst( pool.createRef() );
	}

	@Override
	public O removeLast( final O obj )
	{
		return pool.getObject( indices.removeLast(), obj );
	}

	@Override
	public O removeLast()
	{
		return removeLast( pool.createRef() );
	}

	@Override
	public O getFirst( final O obj )
	{
		return pool.getObject( indices.getFirst(), obj );
	}

	@Override
	public O getFirst()
	{
		return getFirst( pool.createRef() );
	}

	@Override
	public O getLast( final O obj )
	{
		return pool.getObject( indices.getLast(), obj );
	}

	@Override
	public O getLast()
	{
		return getLast( pool.createRef() );
	}

	@Override
	public boolean offerFirst( final O obj )
	{
		addFirst( obj );
		return true;
	}

	@Override
	public boolean offerLast( final O obj )
	{
		addLast( obj );
		return true;
	}

	@Override
	public boolean offer( final O obj )
	{
        return offerLast( obj );
	}

	@Override
	public O remove()
	{
		return removeFirst();
	}

	@Override
	public boolean removeFirstOccurrence( final Object obj )
	{
		if ( !( obj instanceof IntBackedRefCollection ) )
			return false;
		@SuppressWarnings( "unchecked" )
		final O o = ( O ) obj;
		final int id = pool.getId( o );
		return indices.remove( id );
	}

	@Override
	public boolean removeLastOccurrence( final Object obj )
	{
		if ( !( obj instanceof IntBackedRefCollection ) )
			return false;
		@SuppressWarnings( "unchecked" )
		final O o = ( O ) obj;
		final int id = pool.getId( o );
		return indices.removeLastOccurrence( id );
	}

	@Override
	public O poll()
	{
		return pollFirst();
	}

	@Override
	public O poll( final O obj )
	{
		return pollFirst( obj );
	}

	@Override
	public O element()
	{
		return getFirst();
	}

	@Override
	public O element( final O obj )
	{
		return getFirst( obj );
	}

	@Override
	public O peek()
	{
		return peekFirst();
	}

	@Override
	public O peek( final O obj )
	{
		return peekFirst( obj );
	}

	@Override
	public void push( final O obj )
	{
		addFirst( obj );
	}

	@Override
	public O pop()
	{
		return removeFirst();
	}

	@Override
	public O pop( final O obj )
	{
		return removeFirst( obj );
	}
}
