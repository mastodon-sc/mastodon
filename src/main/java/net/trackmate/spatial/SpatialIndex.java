package net.trackmate.spatial;

import net.imglib2.neighborsearch.NearestNeighborSearch;

public interface SpatialIndex< T > extends Iterable< T >
{
	/**
	 * Get number of objects contained in this {@link SpatialIndex}.
	 *
	 * @return number of objects in the index.
	 */
	public int size();

	/**
	 * Check whether this index contains no objects.
	 *
	 * @return {@code true} if {@code {@link #size()} == null}.
	 */
	public boolean isEmpty();

	public NearestNeighborSearch< T > getNearestNeighborSearch();

	public ClipConvexPolytope< T > getClipConvexPolytope();
}
