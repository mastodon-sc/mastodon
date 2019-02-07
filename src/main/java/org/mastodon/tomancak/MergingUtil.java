package org.mastodon.tomancak;

import bdv.spimdata.SpimDataMinimal;
import bdv.spimdata.XmlIoSpimDataMinimal;
import java.util.concurrent.locks.Lock;
import mpicbg.spim.data.SpimDataException;
import org.mastodon.project.MamutProject;
import org.mastodon.properties.ObjPropertyMap;
import org.mastodon.revised.model.mamut.Model;
import org.mastodon.revised.model.mamut.Spot;
import org.mastodon.revised.model.mamut.SpotPool;
import org.mastodon.revised.util.DummySpimData;
import org.mastodon.spatial.SpatialIndex;
import org.mastodon.spatial.SpatioTemporalIndex;

public class MergingUtil
{
	/**
	 * Returns true if {@code spot} has a set label (vs label generated from id).
	 */
	public static boolean hasLabel( final Spot spot )
	{
		final SpotPool pool = ( SpotPool ) spot.getModelGraph().vertices().getRefPool();
		ObjPropertyMap< Spot, String > labels = ( ObjPropertyMap< Spot, String > ) pool.labelProperty();
		return labels.isSet( spot );
	}

	public static String spotToString( final Spot spot )
	{
		return spotToString( spot, false );
	}

	public static String spotToString( final Spot spot, boolean onlyTrueLabels )
	{
		return String.format( "Spot( id=%3d, tp=%3d",
				spot.getInternalPoolIndex(),
				spot.getTimepoint() )
				+ ( !onlyTrueLabels || hasLabel( spot )
						? String.format( ", label='%s' )", spot.getLabel() )
						: " )" );
	}

	/**
	 * Returns number of timepoints in {@code project}.
	 * To to that, loads spimdata for {@code project}.
	 */
	public static int getNumTimepoints( MamutProject project )
	{
		try
		{
			final String spimDataXmlFilename = project.getDatasetXmlFile().getAbsolutePath();
			SpimDataMinimal spimData = DummySpimData.tryCreate( project.getDatasetXmlFile().getName() );
			if ( spimData == null )
				spimData = new XmlIoSpimDataMinimal().load( spimDataXmlFilename );
			return spimData.getSequenceDescription().getTimePoints().size();
		}
		catch ( final SpimDataException e )
		{
			throw new RuntimeException( e );
		}
	}

	// Helper: max timepoint that has at least one spot

	/**
	 * Returns the largest timepoint (index) where model has a least one spot.
	 */
	public static int getMaxNonEmptyTimepoint( final Model model, final int numTimepoints )
	{
		int maxNonEmptyTimepoint = 0;
		final SpatioTemporalIndex< Spot > spatioTemporalIndex = model.getSpatioTemporalIndex();
		spatioTemporalIndex.readLock().lock();
		try
		{
			for ( int t = 0; t < numTimepoints; ++t )
			{
				final SpatialIndex< Spot > index = spatioTemporalIndex.getSpatialIndex( t );
				if ( index.size() > 0 )
					maxNonEmptyTimepoint = t;
			}
		}
		finally
		{
			spatioTemporalIndex.readLock().unlock();
		}
		return maxNonEmptyTimepoint;
	}

	/**
	 * Prints number of spots per timepoint of two datasets for comparison
	 */
	public static void printSpotsPerTimepoint( final Dataset ds1, final Dataset ds2 )
	{
		final int numTimepoints = 1 + Math.max( ds1.maxNonEmptyTimepoint(), ds2.maxNonEmptyTimepoint() );
		final SpatioTemporalIndex< Spot > stIndex1 = ds1.model().getSpatioTemporalIndex();
		final SpatioTemporalIndex< Spot > stIndex2 = ds2.model().getSpatioTemporalIndex();
		for ( int t = 0; t < numTimepoints; ++t )
		{
			final SpatialIndex< Spot > index1 = stIndex1.getSpatialIndex( t );
			final SpatialIndex< Spot > index2 = stIndex2.getSpatialIndex( t );
			final int s1 = index1.size();
			final int s2 = index2.size();
			System.out.println( String.format( "tp %3d: %3d / %3d spots (diff = %d)", t, s1, s2, Math.abs( s1 - s2 ) ) );
		}
	}

	public static void runLocked( final Dataset ds1, final Dataset ds2, final Runnable runnable )
	{
		runLocked( ds1, () -> runLocked( ds2, runnable ) );
	}

	public static void runLocked( final Dataset ds1, final Runnable runnable )
	{
		final Lock lock = ds1.model().getSpatioTemporalIndex().readLock();
		lock.lock();
		try
		{
			runnable.run();
		}
		finally
		{
			lock.unlock();
		}
	}
}
