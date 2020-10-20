package org.mastodon.views.bvv.scene;

import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Base class for GPU resources for parameters of instances.
 * <p>
 * A {@code ReusableResource} is associated to a key of type {@code K}, typically a pool of instances.
 * It has a {@code needsUpdate} flag, that is set when when the {@code ReusableResource} is associated
 * to a new key.
 * Derived classes use this to determin that the GPU arrays have to be updated.
 * (Other reasons for update are that the content of the currently associated pool changed.
 * This is tracked in the derived classes themselves).
 *
 * @param <K>
 */
abstract class ReusableResource< K >
{
	private WeakReference< K > key = null;

	protected AtomicBoolean needsUpdate = new AtomicBoolean();

	/**
	 * {@code lru} is incremented when this {@code ReusableResource} is retrieved from
	 * {@link ReusableResources}, to determine the most stale resource when associating
	 * a new key.
	 */
	int lru = -1;

	/**
	 * Associate this {@code ReusableResource} to a new key.
	 * This sets the {@code needsUpdate} flag
	 *
	 * @param key
	 * 		the new key
	 */
	void associate( K key )
	{
		this.key = new WeakReference<>( key );
		needsUpdate.set( true );
	}

	protected K key()
	{
		return key == null ? null : key.get();
	}
}
