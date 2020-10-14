package org.mastodon.views.bvv.scene;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * GPU resources for parameters of instances.
 * Associated to a pool.
 * Updates when associated to a new pool, or when the current pool is modified.
 *
 * @param <K>
 */
abstract class ReusableResource< K >
{
	// TODO: should be a WeakReference
	protected K key = null;

	protected AtomicBoolean needsUpdate = new AtomicBoolean();

	int lru = -1;

	void associate( K key )
	{
		this.key = key;
		needsUpdate.set( true );
	}
}
