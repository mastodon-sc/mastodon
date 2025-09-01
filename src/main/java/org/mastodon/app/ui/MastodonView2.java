package org.mastodon.app.ui;

import org.mastodon.app.ViewGraph;
import org.mastodon.graph.Edge;
import org.mastodon.graph.Vertex;
import org.mastodon.grouping.GroupHandle;
import org.mastodon.model.MastodonModel;

/**
 * Interface for views of a {@link MastodonModel}, that display a view-graph
 * derived from the model's graph.
 * 
 * @param <M>
 *            the type of the mastodon model to display.
 * @param <VG>
 *            the type of the view-graph this view displays.
 * @param <MV>
 *            the type of vertices in the mastodon model.
 * @param <ME>
 *            the type of edges in the mastodon model.
 * @param <V>
 *            the type of vertices in the view-graph.
 * @param <E>
 *            the type of edges in the view-graph.
 */
public interface MastodonView2<
	M extends MastodonModel< ?, MV, ME >,
	VG extends ViewGraph< MV, ME, V, E >,
	MV extends Vertex< ME >,
	ME extends Edge< MV >,
	V extends Vertex< E >,
	E extends Edge< V > >
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
