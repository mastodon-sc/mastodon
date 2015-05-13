package net.trackmate.graph;

import gnu.trove.iterator.TIntIterator;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;

import java.util.Collection;
import java.util.Iterator;

import net.trackmate.graph.collection.RefSet;

public class PoolObjectSet< O extends Ref< O > > implements PoolObjectCollection< O >, RefSet< O >
{
	private final TIntSet indices;

	private final RefPool< O > pool;

	public PoolObjectSet( final RefPool< O > pool )
	{
		this.pool = pool;
		indices = new TIntHashSet();
	}

	public PoolObjectSet( final RefPool< O > pool, final int initialCapacity )
	{
		this.pool = pool;
		indices = new TIntHashSet( initialCapacity );
	}

	protected PoolObjectSet( final RefPool< O > pool, final TIntSet indices )
	{
		this.pool = pool;
		this.indices = indices;
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
	public TIntSet getIndexCollection()
	{
		return indices;
	}

	@Override
	public boolean add( final O obj )
	{
		return indices.add( obj.getInternalPoolIndex() );
	}

	@Override
	public boolean addAll( final Collection< ? extends O > objs )
	{
		if ( objs instanceof PoolObjectCollection )
			return indices.addAll( ( ( PoolObjectCollection< ? > ) objs ).getIndexCollection() );
		else
		{
			boolean changed = false;
			for ( final O obj : objs )
				if ( indices.add( obj.getInternalPoolIndex() ) )
					changed = true;
			return changed;
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
			return indices.containsAll( ( ( PoolObjectCollection< ? > ) objs ).getIndexCollection() );
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
		if ( obj instanceof PoolObject )
		{
			final PoolObject< ?, ? > o = ( PoolObject< ?, ? > ) obj;
			return indices.remove( o.getInternalPoolIndex() );
		}
		else
			return false;
	}

	@Override
	public boolean removeAll( final Collection< ? > objs )
	{
		if ( objs instanceof PoolObjectCollection )
			return indices.removeAll( ( ( PoolObjectCollection< ? > ) objs ).getIndexCollection() );
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
			return indices.retainAll( ( ( PoolObjectCollection< ? > ) objs ).getIndexCollection() );
		else
		{
			// TODO
			throw new UnsupportedOperationException( "not yet implemented" );
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
		// TODO
		throw new UnsupportedOperationException( "not yet implemented" );
	}

	@Override
	public < A > A[] toArray( final A[] a )
	{
		// TODO
		throw new UnsupportedOperationException( "not yet implemented" );
	}

	@Override
	public String toString()
	{
		final Iterator< O > i = iterator();
		if ( !i.hasNext() )
			return "[]";

		final StringBuilder sb = new StringBuilder();
		sb.append( '[' );
		for ( ;; )
		{
			final O e = i.next();
			sb.append( e );
			if ( !i.hasNext() )
				return sb.append( ']' ).toString();
			sb.append( ", " );
		}
	}
}
