package net.trackmate.graph;

import gnu.trove.iterator.TIntIterator;

import java.util.Collection;
import java.util.Iterator;

import net.trackmate.graph.collection.RefDeque;
import net.trackmate.graph.mempool.MappedElement;
import net.trackmate.graph.mempool.MemPool;
import net.trackmate.graph.util.TIntArrayDeque;

public class PoolObjectDeque< O extends PoolObject< O, T >, T extends MappedElement > implements PoolObjectCollection< O, T >, RefDeque< O >
{

	private final Pool< O, T > pool;

	private final TIntArrayDeque indices;

	public PoolObjectDeque( final Pool< O, T > pool )
	{
		this.pool = pool;
		this.indices = new TIntArrayDeque();
	}

	public PoolObjectDeque( final Pool< O, T > pool, final int initialCapacity )
	{
		this.pool = pool;
		indices = new TIntArrayDeque( initialCapacity );
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
		return indices.add( obj.getInternalPoolIndex() );
	}

	@Override
	public boolean addAll( final Collection< ? extends O > objs )
	{
		if ( objs instanceof PoolObjectCollection )
			return indices.addAll( ( ( PoolObjectCollection< ?, ? > ) objs ).getIndexCollection() );
		else
		{
			for ( final O obj : objs )
				indices.add( obj.getInternalPoolIndex() );
			return !objs.isEmpty();
		}
	}

	@Override
	public void clear()
	{
		indices.clear();
	}

	@Override
	public boolean contains( final Object obj )
	{
		return ( obj instanceof PoolObject )
				? indices.contains( ( ( PoolObject< ?, ? > ) obj ).getInternalPoolIndex() )
				: false;
	}

	@Override
	public boolean containsAll( final Collection< ? > objs )
	{
		if ( objs instanceof PoolObjectCollection )
			return indices.containsAll( ( ( PoolObjectCollection< ?, ? > ) objs ).getIndexCollection() );
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
			final MemPool< T > memPool = pool.getMemPool();

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
				final int index = ii.next();
				obj.updateAccess( memPool, index );
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
	public Iterator< O > descendingIterator()
	{
		return new Iterator< O >()
		{
			final MemPool< T > memPool = pool.getMemPool();

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
				final int index = ii.next();
				obj.updateAccess( memPool, index );
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
	public boolean remove( final Object obj )
	{
		return ( obj instanceof PoolObject )
				? indices.remove( ( ( PoolObject< ?, ? > ) obj ).getInternalPoolIndex() )
				: false;
	}

	@Override
	public boolean removeAll( final Collection< ? > objs )
	{
		if ( objs instanceof PoolObjectCollection )
			return indices.removeAll( ( ( PoolObjectCollection< ?, ? > ) objs ).getIndexCollection() );
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
			return indices.retainAll( ( ( PoolObjectCollection< ?, ? > ) objs ).getIndexCollection() );
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
	public void addFirst( final O obj )
	{
		indices.addFirst( obj.getInternalPoolIndex() );
	}

	@Override
	public void addLast( final O obj )
	{
		indices.addLast( obj.getInternalPoolIndex() );
	}

	@Override
	public O pollFirst( final O obj )
	{
		obj.updateAccess( pool.getMemPool(), indices.pollFirst() );
		return obj;
	}

	@Override
	public O pollFirst()
	{
		return pollFirst( pool.createRef() );
	}

	@Override
	public O pollLast( final O obj )
	{
		obj.updateAccess( pool.getMemPool(), indices.pollLast() );
		return obj;
	}

	@Override
	public O pollLast()
	{
		return pollLast( pool.createRef() );
	}

	@Override
	public O peekFirst( final O obj )
	{
		obj.updateAccess( pool.getMemPool(), indices.peekFirst() );
		return obj;
	}

	@Override
	public O peekFirst()
	{
		return peekFirst( pool.createRef() );
	}

	@Override
	public O peekLast( final O obj )
	{
		obj.updateAccess( pool.getMemPool(), indices.peekLast() );
		return obj;
	}

	@Override
	public O peekLast()
	{
		return peekLast( pool.createRef() );
	}

	@Override
	public O removeFirst( final O obj )
	{
		obj.updateAccess( pool.getMemPool(), indices.removeFirst() );
		return obj;
	}

	@Override
	public O removeFirst()
	{
		return removeFirst( pool.createRef() );
	}

	@Override
	public O removeLast( final O obj )
	{
		obj.updateAccess( pool.getMemPool(), indices.removeLast() );
		return obj;
	}

	@Override
	public O removeLast()
	{
		return removeLast( pool.createRef() );
	}

	@Override
	public O getFirst( final O obj )
	{
		obj.updateAccess( pool.getMemPool(), indices.getFirst() );
		return obj;
	}

	@Override
	public O getFirst()
	{
		return getFirst( pool.createRef() );
	}

	@Override
	public O getLast( final O obj )
	{
		obj.updateAccess( pool.getMemPool(), indices.getLast() );
		return obj;
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
		if ( !( obj instanceof PoolObjectCollection ) )
			return false;
		@SuppressWarnings( "unchecked" )
		final O o = ( O ) obj;
		final int id = o.getInternalPoolIndex();
		return indices.remove( id );
	}

	@Override
	public boolean removeLastOccurrence( final Object obj )
	{
		if ( !( obj instanceof PoolObjectCollection ) )
			return false;
		@SuppressWarnings( "unchecked" )
		final O o = ( O ) obj;
		final int id = o.getInternalPoolIndex();
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
