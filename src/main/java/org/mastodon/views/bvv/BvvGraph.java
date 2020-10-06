package org.mastodon.views.bvv;

import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.mastodon.graph.Graph;
import org.mastodon.graph.GraphChangeListener;
import org.mastodon.graph.GraphChangeNotifier;
import org.mastodon.spatial.SpatioTemporalIndex;

public interface BvvGraph< V extends BvvVertex< V, E >, E extends BvvEdge< E, V > >
		extends Graph< V, E >, GraphChangeNotifier
{
	SpatioTemporalIndex< V > getIndex();

	double getMaxBoundingSphereRadiusSquared( final int timepoint );

	ReentrantReadWriteLock getLock();

	EllipsoidsPerTimepoint< V, E > getEllipsoids();

	/**
	 * Triggers a {@link GraphChangeListener#graphChanged()} event.
	 *
	 * notifyGraphChanged() is not implicitly called in addVertex() etc because
	 * we want to support batches of add/remove with one final
	 * notifyGraphChanged() at the end.
	 */
	@Override
	void notifyGraphChanged();
}
