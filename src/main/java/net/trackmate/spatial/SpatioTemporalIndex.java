package net.trackmate.spatial;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;

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
	 * Get a {@link SpatialIndex} for objects with the given timepoint.
	 *
	 * @param timepoint
	 *            timepoint
	 * @return index for objects with the given timepoint
	 */
	public SpatialIndex< T > getSpatialIndex( final int timepoint );

	/**
	 * Get a {@link SpatialIndex} for objects in the given timepoint range.
	 *
	 * @param fromTimepoint
	 *            first timepoint (inclusive) of the range.
	 * @param toTimepoint
	 *            last timepoint (inclusive) of the range.
	 * @return index for objects in the given timepoint range.
	 */
	public SpatialIndex< T > getSpatialIndex( final int fromTimepoint, final int toTimepoint );
}
