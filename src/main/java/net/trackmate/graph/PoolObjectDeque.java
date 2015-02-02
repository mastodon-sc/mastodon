package net.trackmate.graph;

import gnu.trove.iterator.TIntIterator;
import gnu.trove.list.array.TIntArrayList;

import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.NoSuchElementException;

import net.trackmate.graph.mempool.MappedElement;
import net.trackmate.graph.mempool.MemPool;

public class PoolObjectDeque< O extends PoolObject< O, T >, T extends MappedElement > implements PoolObjectCollection< O, T >, Deque< O >
{

	private final Pool< O, T > pool;

	private final TIntArrayList indices;

	public PoolObjectDeque( final Pool< O, T > pool )
	{
		this.pool = pool;
		this.indices = new TIntArrayList();
	}

	public PoolObjectDeque( final Pool< O, T > pool, final int initialCapacity )
	{
		this.pool = pool;
		indices = new TIntArrayList( initialCapacity );
	}

	protected PoolObjectDeque( final PoolObjectDeque< O, T > deque, final TIntArrayList indexSubList )
	{
		pool = deque.pool;
		indices = indexSubList;
	}

	/*
	 * POOL COLLECTION METHODS
	 */

	public O createRef()
	{
		return pool.createRef();
	}

	public void releaseRef( final O obj )
	{
		pool.releaseRef( obj );
	}

	@Override
	public TIntArrayList getIndexCollection()
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
				throw new UnsupportedOperationException();
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
		indices.insert( 0, obj.getInternalPoolIndex() );
	}

	@Override
	public void addLast( final O obj )
	{
		add( obj );
	}

	@Override
	public Iterator< O > descendingIterator()
	{
		return new Iterator< O >()
		{
			final MemPool< T > memPool = pool.getMemPool();
			
			int index = indices.size() - 1;

			final O obj = pool.createRef();

			@Override
			public boolean hasNext()
			{
				return index >= 0;
			}

			@Override
			public O next()
			{
				final int id = indices.get( index-- );
				obj.updateAccess( memPool, id );
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
	public O element()
	{
		return getFirst();
	}

	@Override
	public O getFirst()
	{
		if ( size() > 0 ) { return getFirst(); }
		throw new NoSuchElementException();
	}

	@Override
	public O getLast()
	{
		if ( size() > 0 ) { return peekLast(); }
		throw new NoSuchElementException();
	}

	@Override
	public boolean offer( final O obj )
	{
		return add( obj );
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
	public O peek()
	{
		return peekFirst();
	}

	@Override
	public O peekFirst()
	{
		if ( size() > 0 ) { return getFirst(); }
		return null;
	}

	@Override
	public O peekLast()
	{
		if ( size() > 0 ) { return getLast(); }
		return null;
	}

	@Override
	public O poll()
	{
		return pollFirst();
	}

	@Override
	public O pollFirst()
	{
		if ( size() > 0 ) { return removeFirst(); }
		return null;
	}

	@Override
	public O pollLast()
	{
		if ( size() > 0 ) { return removeLast(); }
		return null;
	}

	@Override
	public O pop()
	{
		return removeFirst();
	}

	@Override
	public void push( final O obj )
	{
		addFirst( obj );
	}

	@Override
	public O remove()
	{
		return removeFirst();
	}

	@Override
	public O removeFirst()
	{
		final int index = indices.removeAt( 0 );
		final O ref = pool.createRef();
		pool.getByInternalPoolIndex( index, ref );
		return ref;
	}


	@Override
	public O removeLast()
	{
		final int index = indices.removeAt( indices.size() - 1 );
		final O ref = pool.createRef();
		pool.getByInternalPoolIndex( index, ref );
		return ref;
	}

	@Override
	public boolean removeFirstOccurrence( final Object obj )
	{
		if ( !( obj instanceof PoolObjectCollection ) ) { return false; }
		@SuppressWarnings( "unchecked" )
		final O o = ( O ) obj;
		final int id = o.getInternalPoolIndex();
		final int lastIndexOf = indices.indexOf( id );
		if ( lastIndexOf < 0 ) { return false; }
		indices.removeAt( lastIndexOf );
		return true;
	}

	@Override
	public boolean removeLastOccurrence( final Object obj )
	{
		if ( !( obj instanceof PoolObjectCollection ) ) { return false; }
		@SuppressWarnings( "unchecked" )
		final O o = ( O ) obj;
		final int id = o.getInternalPoolIndex();
		final int lastIndexOf = indices.lastIndexOf( id );
		if ( lastIndexOf < 0 ) { return false; }
		indices.removeAt( lastIndexOf );
		return true;
	}
}
