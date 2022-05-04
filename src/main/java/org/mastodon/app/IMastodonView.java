package org.mastodon.app;

import org.mastodon.grouping.GroupHandle;

public interface IMastodonView
{

	/**
	 * Adds the specified {@link Runnable} to the list of runnables to execute
	 * when this view is closed.
	 *
	 * @param runnable
	 *            the {@link Runnable} to add.
	 */
	void onClose( Runnable runnable );

	/**
	 * Exposes the {@link GroupHandle} of this view.
	 * 
	 * @return the {@link GroupHandle} of this view.
	 */
	GroupHandle getGroupHandle();

}
