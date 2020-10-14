package org.mastodon.views.bvv.scene;

/**
 * GPU resources for parameters of instances.
 * Associated to a pool.
 * Updates when associated to a new pool, or when the current pool is modified.
 *
 * @param <K>
 */
abstract class ReusableInstanceArray< K >
{
	// TODO: should be a WeakReference
	protected K key = null;

	protected boolean needsUpdate = false;

	int lru = -1;

	void associate( K key )
	{
		this.key = key;
		needsUpdate = true;
	}
}
