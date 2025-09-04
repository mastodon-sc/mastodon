package org.mastodon.app.ui;

import org.mastodon.grouping.GroupHandle;
import org.mastodon.model.MastodonModel;

/**
 * Interface for views of a {@link MastodonModel}, that display a view-graph
 * derived from the model's graph.
 *
 */
public interface MastodonView2
{
	/**
	 * Exposes the {@link GroupHandle} of this view.
	 *
	 * @return the {@link GroupHandle} of this view.
	 */
	GroupHandle getGroupHandle();

	/**
	 * Adds the specified {@link Runnable} to the list of runnables to execute
	 * when this view is closed.
	 *
	 * @param runnable
	 *            the {@link Runnable} to add.
	 */
	void onClose( Runnable runnable );

	void close();

}
