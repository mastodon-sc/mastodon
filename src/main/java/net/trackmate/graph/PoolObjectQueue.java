package net.trackmate.graph;

import gnu.trove.TIntCollection;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.list.linked.TIntLinkedList;

import java.util.Collection;
import java.util.Iterator;

public class PoolObjectQueue< O extends Ref< O > > implements PoolObjectCollection< O >
{

	private final RefPool< O > pool;

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
	public PoolObjectQueue( final RefPool< O > pool )
	{
		this.pool = pool;
		this.queue = new TIntLinkedList();
	}

	protected PoolObjectQueue( final PoolObjectQueue< O > queue, final TIntLinkedList indexSubList )
	{
		this.pool = queue.pool;
		this.queue = indexSubList;
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
		pool.getByInternalPoolIndex( index, obj );
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
		queue.add( obj.getInternalPoolIndex() );
		return obj;
	}

	/**
	 * Retrieves, but does not remove the head of this queue.
	 *
	 * @param obj
	 *            the pool object ref to update with the content of the top
	 *            item.
	 * @return the given object, updated to be the item at the top of this
	 *         queue, or <code>null</code> if this queue is empty.
	 */
	public O peek( final O obj )
	{
		if ( queue.isEmpty() ) { return null; }
		final int index = queue.get( 0 );
		pool.getByInternalPoolIndex( index, obj );
		return obj;
	}

	/**
	 * Retrieves and removes the head of this queue, or returns
	 * <code>null</code> if this queue is empty.
	 *
	 * @return the head of this queue, or <code>null</code> if this queue is
	 *         empty
	 */
	public O poll( final O obj )
	{
		if ( queue.isEmpty() ) { return null; }
		return element( obj );
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
	public boolean add( final O obj )
	{
		return queue.add( obj.getInternalPoolIndex() );
	}

	@Override
	public boolean addAll( final Collection< ? extends O > objs )
	{
		if ( objs instanceof PoolObjectCollection )
			return queue.addAll( ( ( PoolObjectCollection< ? > ) objs ).getIndexCollection() );
		else
		{
			for ( final O obj : objs )
				queue.add( obj.getInternalPoolIndex() );
			return !objs.isEmpty();
		}
	}

	@Override
	public void clear()
	{
		queue.clear();
	}

	@Override
	public boolean contains( final Object obj )
	{
		return ( obj instanceof PoolObject )
				? queue.contains( ( ( PoolObject< ?, ? > ) obj ).getInternalPoolIndex() )
				: false;
	}

	@Override
	public boolean containsAll( final Collection< ? > objs )
	{
		if ( objs instanceof PoolObjectCollection )
			return queue.containsAll( ( ( PoolObjectCollection< ? > ) objs ).getIndexCollection() );
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
				pool.getByInternalPoolIndex( ii.next(), obj );
				return obj;
			}

			@Override
			public void remove()
			{
				throw new UnsupportedOperationException();
			}
		};
	}

	@Override
	public boolean remove( final Object obj )
	{
		return ( obj instanceof PoolObject )
				? queue.remove( ( ( PoolObject< ?, ? > ) obj ).getInternalPoolIndex() )
				: false;
	}

	public O remove( final int index, final O obj )
	{
		pool.getByInternalPoolIndex(  queue.removeAt( index ), obj );
		return obj;
	}

	@Override
	public boolean removeAll( final Collection< ? > objs )
	{
		if ( objs instanceof PoolObjectCollection )
			return queue.removeAll( ( ( PoolObjectCollection< ? > ) objs ).getIndexCollection() );
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
		if ( objs instanceof PoolObjectCollection )
			return queue.retainAll( ( ( PoolObjectCollection< ? > ) objs ).getIndexCollection() );
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
		{
			final O ref = createRef();
			ref.refTo( obj );
			arr[ index++ ] = ref;
		}
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
			{
				final O ref = createRef();
				ref.refTo( obj );
				arr[ index++ ] = ( U ) ref;
			}
			for ( int i = index; i < arr.length; i++ )
			{
				arr[ i ] = null;
			}
			return arr;
		}
		return ( U[] ) toArray();
	}

	@Override
	public TIntCollection getIndexCollection()
	{
		return queue;
	}

}
