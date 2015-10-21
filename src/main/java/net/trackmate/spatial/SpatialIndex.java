package net.trackmate.spatial;

import net.imglib2.neighborsearch.NearestNeighborSearch;

public interface SpatialIndex< T > extends Iterable< T >
{
	public NearestNeighborSearch< T > getNearestNeighborSearch();

	public ClipConvexPolytope< T > getClipConvexPolytope();
}
