package org.mastodon.collection.util;

import java.util.Collection;

import org.mastodon.RefPool;
import org.mastodon.collection.RefCollection;
import org.mastodon.collection.ref.RefPoolBackedRefCollection;

/**
 * Base class for wrappers of {@link RefPool} that offer access to collections.
 * <p>
 * This class wraps a {@link RefPool} and offers methods to generate various
 * collections based on the wrapped pool. It offers a bridge between the
 * {@link RefPool} framework and the Java {@link Collection} framework.
 * <p>
 * This class implements the {@link RefCollection} interface itself, and
 * therefore allows for questing the underlying pool using the
 * {@link Collection} methods. Only the {@code isEmpty(),} {@code size(),}
 * {@code iterator(),} {@code createRef()}, and {@code releaseRef()} methods are
 * guaranteed to be implemented.
 * <p>
 * The remaining {@link Collection} methods are unsuited for pools and throw an
 * {@link UnsupportedOperationException}. If these methods are needed, it is
 * probably best to create an adequate collection from the pool using the
 * <i>create*</i> methods.
 *
 * @param <O>
 *            the type of the pool object used in the wrapped {@link RefPool}.
 *
 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
 */
public abstract class AbstractRefPoolCollectionCreator< O, P extends RefPool< O > > implements RefPoolBackedRefCollection< O >
{
	protected final P pool;

	public AbstractRefPoolCollectionCreator( final P pool )
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
