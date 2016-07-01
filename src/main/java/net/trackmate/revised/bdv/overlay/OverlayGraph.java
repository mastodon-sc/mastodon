package net.trackmate.revised.bdv.overlay;

import net.trackmate.graph.GraphChangeListener;
import net.trackmate.graph.GraphChangeNotifier;
import net.trackmate.graph.ReadOnlyGraph;
import net.trackmate.spatial.SpatioTemporalIndex;

public interface OverlayGraph< V extends OverlayVertex< V, E >, E extends OverlayEdge< E, V > >
		extends ReadOnlyGraph< V, E >, GraphChangeNotifier
{
	public SpatioTemporalIndex< V > getIndex();

	public double getMaxBoundingSphereRadiusSquared( final int timepoint );

	public V addVertex( final int timepoint, final double[] position, final double radius, V ref );

	public E addEdge( V source, V target, E ref );

	public void remove( E edge );

	public void remove( V vertex );

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
