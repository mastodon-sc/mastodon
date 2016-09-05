package org.mastodon.collection.ref;

import java.util.Collection;
import java.util.Iterator;

import org.mastodon.RefPool;

import gnu.trove.TIntCollection;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.list.linked.TIntLinkedList;

public class RefLinkedQueue< O > implements IntBackedRefCollection< O >, RefPoolBackedRefCollection< O >
{
	private final RefPool< O > pool;

	private final Class< O > elementType;

	private final TIntLinkedList queue;

	/*
	 * CONSTRUCTOR
	 */

	/**
	 * Instantiates an empty queue for the specified pool with default capacity.
	 *
	 * @param pool
	 *            the pool to draw objects from in order to build this queue.
	 */
	public RefLinkedQueue( final RefPool< O > pool )
	{
		this.pool = pool;
		this.elementType = pool.getRefClass();
		this.queue = new TIntLinkedList();
	}

	protected RefLinkedQueue( final RefLinkedQueue< O > queue, final TIntLinkedList indexSubList )
	{
		this.pool = queue.pool;
		this.elementType = queue.elementType;
		this.queue = indexSubList;
	}

	/*
	 * REFCOLLECTION METHODS
	 */

	@Override
	public TIntCollection getIndexCollection()
	{
		return queue;
	}

	@Override
	public RefPool< O > getRefPool()
	{
		return pool;
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

	/*
	 * QUEUE METHODS
	 */

	/**
	 * Retrieves and removes the head of this queue. This method differs from
	 * {@link #poll} only in that it throws an exception if this queue is empty.
	 *
	 * @param obj
	 *            the pool object ref to update with the content of the top
	 *            item.
	 * @return the given object, updated to be the item at the top of this
	 *         queue.
	 * @throws ArrayIndexOutOfBoundsException
	 *             if this queue is empty.
	 */
	public O element( final O obj )
	{
		final int index = queue.removeAt( 0 );
		pool.getObject( index, obj );
		return obj;
	}

	/**
	 * Inserts the specified element into this queue.
	 *
	 * @param obj
	 *            the item to be pushed onto this queue.
	 * @return the given object.
	 */
	public O offer( final O obj )
	{
		queue.add( pool.getId( obj ) );
		return obj;
	}

	/**
	 * Retrieves, but does not remove the head of this queue.
	 *
	 * @param obj
	 *            the pool object ref to update with the content of the top
	 *            item.
	 * @return the given object, updated to be the item at the top of this
	 *         queue, or {@code null} if this queue is empty.
	 */
	public O peek( final O obj )
	{
		if ( queue.isEmpty() ) { return null; }
		final int index = queue.get( 0 );
		pool.getObject( index, obj );
		return obj;
	}

	/**
	 * Retrieves and removes the head of this queue, or returns
	 * {@code null} if this queue is empty.
	 *
	 * @return the head of this queue, or {@code null} if this queue is
	 *         empty
	 */
	public O poll( final O obj )
	{
		if ( queue.isEmpty() ) { return null; }
		return element( obj );
	}

	@Override
	public boolean add( final O obj )
	{
		return queue.add( pool.getId( obj ) );
	}

	@Override
	public boolean addAll( final Collection< ? extends O > objs )
	{
		if ( objs instanceof IntBackedRefCollection )
			return queue.addAll( ( ( IntBackedRefCollection< ? > ) objs ).getIndexCollection() );
		else
		{
			for ( final O obj : objs )
				queue.add( pool.getId( obj ) );
			return !objs.isEmpty();
		}
	}

	@Override
	public void clear()
	{
		queue.clear();
	}

	@SuppressWarnings( "unchecked" )
	@Override
	public boolean contains( final Object obj )
	{
		return ( elementType.isInstance( obj ) )
				? queue.contains( pool.getId( ( O ) obj ) )
				: false;
	}

	@Override
	public boolean containsAll( final Collection< ? > objs )
	{
		if ( objs instanceof IntBackedRefCollection )
			return queue.containsAll( ( ( IntBackedRefCollection< ? > ) objs ).getIndexCollection() );
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
		return queue.isEmpty();
	}

	@Override
	public Iterator< O > iterator()
	{
		return new Iterator< O >()
		{
			final TIntIterator ii = queue.iterator();

			final O obj = pool.createRef();

			@Override
			public boolean hasNext()
			{
				return ii.hasNext();
			}

			@Override
			public O next()
			{
				pool.getObject( ii.next(), obj );
				return obj;
			}

			@Override
			public void remove()
			{
				throw new UnsupportedOperationException();
			}
		};
	}

	@SuppressWarnings( "unchecked" )
	@Override
	public boolean remove( final Object obj )
	{
		return ( elementType.isInstance( obj ) )
				? queue.remove( pool.getId( ( O ) obj ) )
				: false;
	}

	public O remove( final int index, final O obj )
	{
		pool.getObject(  queue.removeAt( index ), obj );
		return obj;
	}

	@Override
	public boolean removeAll( final Collection< ? > objs )
	{
		if ( objs instanceof IntBackedRefCollection )
			return queue.removeAll( ( ( IntBackedRefCollection< ? > ) objs ).getIndexCollection() );
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
			return queue.retainAll( ( ( IntBackedRefCollection< ? > ) objs ).getIndexCollection() );
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
		return queue.size();
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
}
