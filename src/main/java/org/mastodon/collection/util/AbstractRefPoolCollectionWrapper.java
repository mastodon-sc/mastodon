package org.mastodon.collection.util;

import java.util.Collection;
import java.util.Iterator;

import org.mastodon.RefPool;
import org.mastodon.collection.RefCollection;
import org.mastodon.collection.RefCollections;
import org.mastodon.collection.ref.RefPoolBackedRefCollection;

/**
 * Abstract base class for wrappers of {@link RefPool} as a
 * {@link RefCollection}. This allows for querying the underlying pool using
 * basic {@link Collection} methods. Only the {@code isEmpty(),} {@code size(),}
 * {@code iterator()} methods are guaranteed to be implemented. The remaining
 * {@link Collection} methods are unsuited for pools and throw
 * {@link UnsupportedOperationException}.
 * <p>
 * Moreover, pools wrapped like this can be passed to {@link RefCollections}
 * {@code .create...()} methods for creating specialized {@link RefCollection}s
 * of objects in the pool.
 * <p>
 * Derived classes must implement the {@code size()} and {@code iterator()}
 * methods.
 *
 * @param <O>
 *            the type of the pool object used in the wrapped {@link RefPool}.
 *
 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
 */
public abstract class AbstractRefPoolCollectionWrapper< O, P extends RefPool< O > > implements RefPoolBackedRefCollection< O >
{
	protected final P pool;

	public AbstractRefPoolCollectionWrapper( final P pool )
	{
		this.pool = pool;
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
	public RefPool< O > getRefPool()
	{
		return pool;
	}

	@Override
	public boolean isEmpty()
	{
		return size() == 0;
	}

	@Override
	public String toString()
	{
		if ( isEmpty() ) { return "[]"; }
		final StringBuffer sb = new StringBuffer();
		final Iterator< ? > it = iterator();
		sb.append( "[" + it.next().toString() );
		while ( it.hasNext() )
			sb.append( ", " + it.next().toString() );
		sb.append( "]" );
		return sb.toString();
	}

	/*
	 * The remaining RefCollection methods throw UnsupportedOperationException.
	 * Some of them could be implemented, but it is probably not a good idea to
	 * use the Pool as a Collection in this way.
	 */

	/**
	 * This method is inapplicable to {@link RefPool}s and throw an
	 * {@link UnsupportedOperationException}.
	 */
	@Override
	public boolean contains( final Object o )
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * This method is inapplicable to {@link RefPool}s and throw an
	 * {@link UnsupportedOperationException}.
	 */
	@Override
	public Object[] toArray()
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * This method is inapplicable to {@link RefPool}s and throw an
	 * {@link UnsupportedOperationException}.
	 */
	@Override
	public < T > T[] toArray( final T[] a )
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * This method is inapplicable to {@link RefPool}s and throw an
	 * {@link UnsupportedOperationException}.
	 */
	@Override
	public boolean add( final O e )
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * This method is inapplicable to {@link RefPool}s and throw an
	 * {@link UnsupportedOperationException}.
	 */
	@Override
	public boolean remove( final Object o )
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * This method is inapplicable to {@link RefPool}s and throw an
	 * {@link UnsupportedOperationException}.
	 */
	@Override
	public boolean containsAll( final Collection< ? > c )
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * This method is inapplicable to {@link RefPool}s and throw an
	 * {@link UnsupportedOperationException}.
	 */
	@Override
	public boolean addAll( final Collection< ? extends O > c )
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * This method is inapplicable to {@link RefPool}s and throw an
	 * {@link UnsupportedOperationException}.
	 */
	@Override
	public boolean removeAll( final Collection< ? > c )
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * This method is inapplicable to {@link RefPool}s and throw an
	 * {@link UnsupportedOperationException}.
	 */
	@Override
	public boolean retainAll( final Collection< ? > c )
	{
		throw new UnsupportedOperationException();
	}

	/**
	 * This method is inapplicable to {@link RefPool}s and throw an
	 * {@link UnsupportedOperationException}.
	 */
	@Override
	public void clear()
	{
		throw new UnsupportedOperationException();
	}
}
