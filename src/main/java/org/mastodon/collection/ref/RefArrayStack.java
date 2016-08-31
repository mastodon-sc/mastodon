package org.mastodon.collection.ref;

import org.mastodon.RefPool;
import org.mastodon.collection.RefStack;
import org.mastodon.pool.PoolObject;

/**
 * A {@link RefStack} implementation for {@link Ref}s entirely based on a
 * {@link RefArrayList}.
 *
 * @author Jean-Yves Tinevez
 *
 * @param <O>
 *            recursive type of the {@link PoolObject}s stored in this stack.
 */
public class RefArrayStack< O > extends RefArrayList< O > implements RefStack< O >
{

	/*
	 * CONSTRUCTOR
	 */

	/**
	 * Instantiates an empty stack for the specified pool with default capacity.
	 *
	 * @param pool
	 *            the pool to draw objects from in order to build this stack.
	 */
	public RefArrayStack( final RefPool< O > pool )
	{
		super( pool );
	}

	/**
	 * Instantiates an empty stack for the specified pool.
	 *
	 * @param pool
	 *            the pool to draw objects from in order to build this stack.
	 * @param initialCapacity
	 *            the initial capacity.
	 */
	public RefArrayStack( final RefPool< O > pool, final int initialCapacity )
	{
		super( pool, initialCapacity );
	}

	/*
	 * METHODS
	 */

	@Override
	public O peek()
	{
		return get( size() - 1 );
	}

	@Override
	public O peek( final O obj )
	{
		return get( size() - 1, obj );
	}

	@Override
	public O pop()
	{
		return remove( size() - 1 );
	}

	@Override
	public O pop( final O obj )
	{
		return remove( size() - 1, obj );
	}

	@Override
	public void push( final O obj )
	{
		add( obj );
	}

	@Override
	public int search( final Object obj )
	{
		if ( !( elementType.isInstance( obj ) ) )
			return -1;

		@SuppressWarnings( "unchecked" )
		final int value = pool.getId( ( O ) obj );
		final int index = getIndexCollection().lastIndexOf( value );
		if ( index < 0 )
		{
			return -1;
		}
		else
		{
			return size() - index;
		}
	}
}
