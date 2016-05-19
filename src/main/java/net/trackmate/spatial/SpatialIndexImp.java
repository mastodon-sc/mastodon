package net.trackmate.spatial;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import net.imglib2.RealLocalizable;
import net.imglib2.neighborsearch.NearestNeighborSearch;
import net.trackmate.RefPool;

/**
 * Spatial index of {@link RealLocalizable} objects.
 *
 * TODO: figure out locking and locking API.
 *
 * @param <O>
 *            type of objects in the index
 *
 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
 */
public class SpatialIndexImp< O extends RealLocalizable > implements SpatialIndex< O >
{
	private SpatialIndexData< O > data;

    private final Lock readLock;

    private final Lock writeLock;

	public SpatialIndexImp( final Collection< O > objs, final RefPool< O > objPool )
	{
		data = new SpatialIndexData< O >( objs, objPool );
		final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
	    readLock = rwl.readLock();
	    writeLock = rwl.writeLock();
	}

	void rebuild()
	{
		readLock.lock();
		try
		{
			data = new SpatialIndexData< O >( data );
		}
		finally
		{
			readLock.unlock();
		}
	}

	@Override
	public Iterator< O > iterator()
	{
		return data.iterator();
	}

	@Override
	public NearestNeighborSearch< O > getNearestNeighborSearch()
	{
		return data.getNearestNeighborSearch();
	}

	@Override
	public ClipConvexPolytope< O > getClipConvexPolytope()
	{
		return data.getClipConvexPolytope();
	}

	@Override
	public int size()
	{
		return data.size();
	}

	@Override
	public boolean isEmpty()
	{
		return size() == 0;
	}

	/**
	 * Add a new object to the index. Also use this to indicate that an existing
	 * object was moved.
	 *
	 * @param obj
	 *            object to add.
	 * @return {@code true} if this index did not already contain the specified
	 *         object.
	 */
	boolean add( final O obj )
	{
		writeLock.lock();
		try
		{
			return data.add( obj );
		}
		finally
		{
			writeLock.unlock();
		}
	}

	/**
	 * Remove an object from the index.
	 *
	 * @param obj object to remove.
	 * @return {@code true} if this index contained the specified object.
	 */
	boolean remove( final O obj )
	{
		writeLock.lock();
		try
		{
			return data.remove( obj );
		}
		finally
		{
			writeLock.unlock();
		}
	}

	int modCount()
	{
		return data.modCount();
	}
}
