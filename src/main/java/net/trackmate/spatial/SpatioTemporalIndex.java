package net.trackmate.spatial;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;

/**
 * Manages a collection of {@link SpatialIndex}es, arranged by time-points,
 * naturally ordered.
 *
 * @param <T>
 *            the type of objects in the {@link SpatialIndex}es of this
 *            collection.
 */
public interface SpatioTemporalIndex< T > extends Iterable< T >
{
	/**
	 * A {@link ReadLock} for this index. The lock should be acquired before
	 * doing any searches on the index.
	 *
	 * @return a reentrant {@link ReadLock} for this index.
	 */
	public Lock readLock();

	/**
	 * Get a {@link SpatialIndex} for objects with the given time-point.
	 *
	 * @param timepoint
	 *            the time-point.
	 * @return index for objects with the given time-point.
	 */
	public SpatialIndex< T > getSpatialIndex( final int timepoint );

	/**
	 * Get a {@link SpatialIndex} for objects in the given time-point range.
	 *
	 * @param fromTimepoint
	 *            first time-point (inclusive) of the range.
	 * @param toTimepoint
	 *            last time-point (inclusive) of the range.
	 * @return index for objects in the given time-point range.
	 */
	public SpatialIndex< T > getSpatialIndex( final int fromTimepoint, final int toTimepoint );
}
