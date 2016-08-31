package org.mastodon;

import org.mastodon.pool.PoolObject;

// TODO revise javadoc
public interface Ref< O extends Ref< O > >
{
	/**
	 * Get the element index that this {@link PoolObject} currently refers to.
	 *
	 * @return the element index that this {@link PoolObject} currently refers
	 *         to.
	 */
	public int getInternalPoolIndex();

	/**
	 * Make this {@link PoolObject} refer to the same data as {@code obj}
	 *
	 * @param obj
	 *            A {@link PoolObject}, usually of the same type as this one.
	 */
	public O refTo( final O obj );
}
