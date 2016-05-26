package net.trackmate.revised.bdv.overlay;

import net.trackmate.graph.ReadOnlyGraph;
import net.trackmate.spatial.SpatioTemporalIndex;

public interface OverlayGraph< V extends OverlayVertex< V, E >, E extends OverlayEdge< E, V > >
		extends ReadOnlyGraph< V, E >
{
	public SpatioTemporalIndex< V > getIndex();

	public double getMaxBoundingSphereRadiusSquared( final int timepoint );

	public V addVertex( final int timepoint, final double[] position, final double radius, V ref );

	public E addEdge( V source, V target, E ref );

	public void notifyGraphChanged();

}
