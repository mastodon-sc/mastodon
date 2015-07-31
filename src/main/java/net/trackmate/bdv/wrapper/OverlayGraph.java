package net.trackmate.bdv.wrapper;

import net.trackmate.graph.ReadOnlyGraph;
import net.trackmate.graph.collection.RefSet;

public interface OverlayGraph< V extends OverlayVertex< V, E >, E extends OverlayEdge< E, V > >
		extends ReadOnlyGraph< V, E >
{
	public RefSet< V > getSpots( final int timepoint );

	public SpatialSearch< V > getSpatialSearch( final int timepoint );
}
