package org.mastodon.revised.model.mamut;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.mastodon.graph.GraphListener;
import org.mastodon.spatial.SpatialIndex;

import gnu.trove.map.hash.TIntObjectHashMap;

/**
 * A class that serves statistics about the maximum bounding radius amongst all
 * the spots of a time-point in a model. This class keeps up to date with
 * changes in the graph it monitors by registering as a {@link GraphListener}.
 * <p>
 * Read and write (changes to the monitored graph) operations are protected with
 * a {@link ReentrantReadWriteLock}. Multiple clients can hold the read lock
 * simultaneously (but this blocks updates to the graph).
 *
 * @author Tobias Pietzsch
 */
public class BoundingSphereRadiusStatistics implements GraphListener< Spot, Link >, SpotRadiusListener
{
	/**
	 * Int value used to declare that the requested timepoint is not in a map.
	 * Timepoints are always &gt;= 0, so -1 works...
	 */
	private final static int NO_ENTRY_KEY = -1;

	private final Model model;

	private final ModelGraph graph;

	private final TIntObjectHashMap< Stats > timepointToStats;

	private final Lock readLock;

    private final Lock writeLock;

	/**
	 * Creates a new statistics object for the specified model. After this
	 * constructor returns, statistics are immediately available. The returned
	 * instance is registered as a listener to changes in the graph the
	 * specified model wraps.
	 *
	 * @param model
	 *            the model to build statistics for.
	 */
	public BoundingSphereRadiusStatistics( final Model model )
	{
		this.model = model;
		this.graph = model.getGraph();
		timepointToStats = new TIntObjectHashMap< Stats >( 10, 0.5f, NO_ENTRY_KEY );
		graph.addGraphListener( this );
		graph.addSpotRadiusListener( this );
		final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
	    readLock = rwl.readLock();
	    writeLock = rwl.writeLock();
		init();
	}

	/**
	 * Exposes the read {@link Lock} of this class. Clients of the class should
	 * {@link Lock#lock() acquire} the lock before
	 * {@link #getMaxBoundingSphereRadiusSquared(int) using} it and
	 * {@link Lock#unlock() release} the lock afterwards.
	 *
	 * @return the read lock.
	 */
	public Lock readLock()
	{
		return readLock;
	}

	/**
	 * Returns the maximal bounding sphere radius squared amongst all the spots
	 * of the specified time-point.
	 * <p>
	 * It is best to acquire the {@link #readLock() read lock} prior to
	 * executing this method, in case another thread updates this statistics
	 * object. A typical call would be wrapped as follow:
	 *
	 * <pre>
	 * radiusStats.readLock().lock();
	 * double maxRadius;
	 * try
	 * {
	 * 	maxRadius = radiusStats.getMaxBoundingSphereRadiusSquared( timepoint );
	 * }
	 * finally
	 * {
	 * 	radiusStats.readLock().unlock();
	 * }
	 * </pre>
	 *
	 * @param timepoint
	 * @return
	 */
	public double getMaxBoundingSphereRadiusSquared( final int timepoint )
	{
		final Stats stats = timepointToStats.get( timepoint );
		if ( stats == null )
			return -1;
		else
			return stats.getMaxRadiusSquared();
	}

	private void init()
	{
	    timepointToStats.clear();
	    for ( final Spot v : graph.vertices() )
	    {
	    	final int t = v.getTimepoint();
	    	Stats stats = timepointToStats.get( t );
	    	if ( stats == null )
	    	{
	    		stats = new Stats( graph.vertexRef() );
	    		timepointToStats.put( t, stats );
	    	}
	    	stats.add( v );
	    }
	}

	@Override
	public void vertexAdded( final Spot v )
	{
		writeLock.lock();
		try
		{
	    	final int t = v.getTimepoint();
	    	Stats stats = timepointToStats.get( t );
	    	if ( stats == null )
	    	{
	    		stats = new Stats( graph.vertexRef() );
	    		timepointToStats.put( t, stats );
	    	}
	    	stats.add( v );
		}
		finally
		{
			writeLock.unlock();
		}
	}

	@Override
	public void vertexRemoved( final Spot v )
	{
		writeLock.lock();
		try
		{
			final int t = v.getTimepoint();
	    	final SpatialIndex< Spot > spatialIndex = model.getSpatioTemporalIndex().getSpatialIndex( t );
	    	if ( spatialIndex.isEmpty() )
	    		timepointToStats.remove( t );
	    	else
	    	{
		    	final Stats stats = timepointToStats.get( t );
	    		stats.remove( v, model.getSpatioTemporalIndex().getSpatialIndex( t ) );
	    	}
		}
		finally
		{
			writeLock.unlock();
		}
	}

	@Override
	public void edgeAdded( final Link edge )
	{}

	@Override
	public void edgeRemoved( final Link edge )
	{}

	@Override
	public void graphRebuilt()
	{
		writeLock.lock();
		try
		{
			init();
		}
		finally
		{
			writeLock.unlock();
		}
	}

	@Override
	public void radiusChanged( final Spot v )
	{
		writeLock.lock();
		try
		{
			final int t = v.getTimepoint();
	    	final Stats stats = timepointToStats.get( t );
	    	stats.radiusChanged( v, model.getSpatioTemporalIndex().getSpatialIndex( t ) );
		}
		finally
		{
			writeLock.unlock();
		}
	}

	static class Stats
	{
		private double maxRadiusSquared;

		private final Spot spotWithMaxRadiusSquared;

		public Stats( final Spot ref )
		{
			this.spotWithMaxRadiusSquared = ref;
			maxRadiusSquared = 0;
		}

		public void add( final Spot spot )
		{
			final double r2 = spot.getBoundingSphereRadiusSquared();
			if ( r2 > maxRadiusSquared )
			{
				maxRadiusSquared = r2;
				spotWithMaxRadiusSquared.refTo( spot );
			}
		}

		/**
		 * If {@code spot} is the representative for the max radius, we have to
		 * find the new max radius from all spots. Otherwise, we're lucky and
		 * we're done. Assumes that {@code spots} does not contain {@code spot}.
		 */
		public void remove( final Spot spot, final Iterable< Spot > spots )
		{
			if ( spotWithMaxRadiusSquared.equals( spot ) )
			{
				maxRadiusSquared = 0;
				for ( final Spot v : spots )
					add( v );
			}
		}

		/**
		 * If {@code spot} is the representative for the max radius and its
		 * radius decreased, we have to find the new max radius from all spots.
		 */
		public void radiusChanged( final Spot spot, final Iterable< Spot > spots )
		{
			final double r2 = spot.getBoundingSphereRadiusSquared();
			if ( r2 > maxRadiusSquared )
			{
				maxRadiusSquared = r2;
				spotWithMaxRadiusSquared.refTo( spot );
			}
			else if ( spotWithMaxRadiusSquared.equals( spot ) && r2 < maxRadiusSquared )
			{
				// this does the right thing though it doesn't sound like it
				remove( spot, spots );
			}
		}

		public double getMaxRadiusSquared()
		{
			return maxRadiusSquared;
		}
	}
}
