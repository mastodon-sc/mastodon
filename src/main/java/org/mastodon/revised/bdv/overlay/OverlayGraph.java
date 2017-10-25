package org.mastodon.revised.bdv.overlay;

import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.mastodon.graph.GraphChangeListener;
import org.mastodon.graph.GraphChangeNotifier;
import org.mastodon.graph.ReadOnlyGraph;
import org.mastodon.spatial.SpatioTemporalIndex;

public interface OverlayGraph< V extends OverlayVertex< V, E >, E extends OverlayEdge< E, V > >
		extends ReadOnlyGraph< V, E >, GraphChangeNotifier
{
	public SpatioTemporalIndex< V > getIndex();

	public double getMaxBoundingSphereRadiusSquared( final int timepoint );

	public V addVertex( final int timepoint, final double[] position, final double radius, V ref );

	public V addVertex( final int timepoint, final double[] position, final double[][] covariance, V ref );

	public E addEdge( V source, V target, E ref );

	public void remove( E edge );

	public void remove( V vertex );

	public ReentrantReadWriteLock getLock();

	/**
	 * Triggers a {@link GraphChangeListener#graphChanged()} event.
	 *
	 * notifyGraphChanged() is not implicitly called in addVertex() etc because
	 * we want to support batches of add/remove with one final
	 * notifyGraphChanged() at the end.
	 */
	@Override
	public void notifyGraphChanged();
}
