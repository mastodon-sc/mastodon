package net.trackmate.revised.bdv.overlay;

import net.trackmate.graph.ReadOnlyGraph;
import net.trackmate.graph.collection.RefSet;
import net.trackmate.spatial.SpatioTemporalIndex;

public interface OverlayGraph< V extends OverlayVertex< V, E >, E extends OverlayEdge< E, V > >
		extends ReadOnlyGraph< V, E >
{
	// TODO remove? (or at least rename tp getVerticesForTimepoint)
	public RefSet< V > getSpots( final int timepoint );

	public SpatioTemporalIndex< V > getIndex();
}
