package net.trackmate.spatial;

import net.imglib2.neighborsearch.NearestNeighborSearch;
import net.trackmate.kdtree.ClipConvexPolytope;

/**
 * Maintain a collection of objects which need to be searched and partitioned
 * spatially.
 *
 * @param <T>
 *            the type of objects managed by this class.
 * @author Tobias Pietzsch
 * @see SpatioTemporalIndex
 */
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
	 * @return {@code true} if this index is empty.
	 */
	public boolean isEmpty();

	/**
	 * Returns a {@link NearestNeighborSearch} for the objects of this index,
	 * able to perform efficiently spatial searches.
	 *
	 * @return a {@link NearestNeighborSearch}.
	 */
	public NearestNeighborSearch< T > getNearestNeighborSearch();

	/**
	 * Returns a {@link ClipConvexPolytope} for the objects of this index, able
	 * to spatially partition the objects of this index in an efficient manner.
	 *
	 * @return a {@link ClipConvexPolytope}.
	 */
	public ClipConvexPolytope< T > getClipConvexPolytope();
}
