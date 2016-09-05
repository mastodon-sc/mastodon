package org.mastodon.collection.ref;

import java.util.Collection;
import java.util.Iterator;

import org.mastodon.RefPool;
import org.mastodon.collection.RefSet;

import gnu.trove.iterator.TIntIterator;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;

/**
 * A {@link RefSet} implementation for {@link Ref} objects, based on a Trove
 * {@link TIntSet} (a {@link TIntHashSet} if not specified otherwise).
 *
 * @param <O>
 *            the type of elements maintained by this set.
 *
 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
 */
public class RefSetImp< O > implements IntBackedRefCollection< O >, RefPoolBackedRefCollection< O >, RefSet< O >
{
	private final TIntSet indices;

	private final RefPool< O > pool;

	private final Class< O > elementType;

	public RefSetImp( final RefPool< O > pool )
	{
		this.pool = pool;
		elementType = pool.getRefClass();
		indices = new TIntHashSet();
	}

	public RefSetImp( final RefPool< O > pool, final int initialCapacity )
	{
		this.pool = pool;
		elementType = pool.getRefClass();
		indices = new TIntHashSet( initialCapacity );
	}

	protected RefSetImp( final RefPool< O > pool, final TIntSet indices )
	{
		this.pool = pool;
		elementType = pool.getRefClass();
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
	public RefPool< O > getRefPool()
	{
		return pool;
	}

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
			boolean changed = false;
			for ( final O obj : objs )
				if ( indices.add( pool.getId( obj ) ) )
					changed = true;
			return changed;
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

	@SuppressWarnings( "unchecked" )
	@Override
	public boolean remove( final Object obj )
	{
		return elementType.isInstance( obj )
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
			final Iterator< O > it = iterator();
			while ( it.hasNext() )
			{
				final O o = it.next();
				if ( !objs.contains( o ) )
				{
					it.remove();
					changed = true;
				}
			}
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
		final Object[] objs = new Object[ size() ];
		return toArray( objs );
	}

	@SuppressWarnings( "unchecked" )
	@Override
	public < A > A[] toArray( final A[] a )
	{
		final A[] array;
		if ( a.length < size() )
		{
			array = ( A[] ) new Object[ size() ];
		}
		else
		{
			array = a;
		}
		final TIntIterator it = indices.iterator();
		int index = 0;
		while ( it.hasNext() )
		{
			final int poolIndex = it.next();
			array[ index++ ] = ( A ) pool.getObject( poolIndex, createRef() );
		}

		// nullify the rest
		for ( int i = index; i < array.length; i++ )
		{
			array[ i ] = null;
		}
		return array;
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
