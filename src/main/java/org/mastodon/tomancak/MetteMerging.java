package org.mastodon.tomancak;

import bdv.spimdata.SpimDataMinimal;
import bdv.spimdata.XmlIoSpimDataMinimal;
import java.io.IOException;
import mpicbg.spim.data.SpimDataException;
import net.imglib2.RealLocalizable;
import org.mastodon.kdtree.IncrementalNearestNeighborSearch;
import org.mastodon.project.MamutProject;
import org.mastodon.project.MamutProjectIO;
import org.mastodon.properties.ObjPropertyMap;
import org.mastodon.revised.model.mamut.Model;
import org.mastodon.revised.model.mamut.Spot;
import org.mastodon.revised.model.mamut.SpotPool;
import org.mastodon.revised.util.DummySpimData;
import org.mastodon.spatial.SpatialIndex;
import org.mastodon.spatial.SpatioTemporalIndex;

public class MetteMerging
{
	public static final String basepath = "/Users/pietzsch/Desktop/Mastodon/merging/Mastodon-files_SimView2_20130315/";

	public static final String[] paths = {
			basepath + "1.SimView2_20130315_Mastodon_Automat-segm-t0-t300",
			basepath + "2.SimView2_20130315_Mastodon_MHT",
			basepath + "3.Pavel manual",
			basepath + "4.Vlado_TrackingPlatynereis",
			basepath + "5.SimView2_20130315_Mastodon_Automat-segm-t0-t300_JG"
	};

	public static class Dataset
	{
		final MamutProject project;

		final int numTimepoints;

		final Model model;

		final int maxNonEmptyTimepoint;

		Dataset( String path ) throws IOException
		{
			project = new MamutProjectIO().load( path );
			numTimepoints = getNumTimepoints( project );
			model = new Model();
			try (final MamutProject.ProjectReader reader = project.openForReading())
			{
				model.loadRaw( reader );
			}
			maxNonEmptyTimepoint = getMaxNonEmptyTimepoint( model, numTimepoints );
		}

		public void verify()
		{
			for ( Spot spot : model.getGraph().vertices() )
			{
				if ( spot.incomingEdges().size() > 1 )
					System.err.println( spot + " has more than one parent" );

				if ( spot.outgoingEdges().size() > 2 )
					System.err.println( spot + " has more than two children" );
			}
		}

		public void labels()
		{
			final SpotPool pool = ( SpotPool ) model.getGraph().vertices().getRefPool();
			ObjPropertyMap< Spot, String > labels = ( ObjPropertyMap< Spot, String > ) pool.labelProperty();
			System.out.println( "pool = " + pool );
			System.out.println( "labels = " + labels );

			for ( Spot spot : model.getGraph().vertices() )
			{
				if ( labels.isSet( spot ) )
				{
					System.out.println( "spot = " + spot + " label=" + labels.get( spot ) );
				}
			}
		}

		// for inns termination criterion
//		private void largestRadius( Ellipsoid ellipsoid )

		public void spotAt( int t, RealLocalizable pos )
		{
			final SpatioTemporalIndex< Spot > spatioTemporalIndex = model.getSpatioTemporalIndex();
			spatioTemporalIndex.readLock().lock();
			try
			{
				final SpatialIndex< Spot > index = spatioTemporalIndex.getSpatialIndex( t );
				final IncrementalNearestNeighborSearch< Spot > inns = index.getIncrementalNearestNeighborSearch();
				inns.search( pos );
				while ( inns.hasNext() )
				{
					inns.fwd();
					System.out.println( "inns.getDistance() = " + inns.getDistance() );
				}
			}
			finally
			{
				spatioTemporalIndex.readLock().unlock();
			}
		}
	}

	// Helper: read spimdata to figure out number of timepoints
	static int getNumTimepoints( MamutProject project )
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
	static int getMaxNonEmptyTimepoint( final Model model, final int numTimepoints )
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

	static void printSpotsPerTimepoint( final Dataset ds1, final Dataset ds2 )
	{
		final int numTimepoints = 1 + Math.max( ds1.maxNonEmptyTimepoint, ds2.maxNonEmptyTimepoint );

		final SpatioTemporalIndex< Spot > stIndex1 = ds1.model.getSpatioTemporalIndex();
		final SpatioTemporalIndex< Spot > stIndex2 = ds2.model.getSpatioTemporalIndex();
		stIndex1.readLock().lock();
		stIndex2.readLock().lock();
		try
		{
			for ( int t = 0; t < numTimepoints; ++t )
			{
				final SpatialIndex< Spot > index1 = stIndex1.getSpatialIndex( t );
				final SpatialIndex< Spot > index2 = stIndex2.getSpatialIndex( t );
				final int s1 = index1.size();
				final int s2 = index2.size();
				System.out.println( String.format( "tp %3d: %3d / %3d spots (diff = %d)", t, s1, s2, Math.abs( s1 - s2 ) ) );
			}
		}
		finally
		{
			stIndex1.readLock().unlock();
			stIndex2.readLock().unlock();
		}
	}

	static void match( final Dataset ds1, final Dataset ds2, final int timepoint )
	{
		final SpotMath spotMath = new SpotMath();

		final SpatioTemporalIndex< Spot > stIndex1 = ds1.model.getSpatioTemporalIndex();
		final SpatioTemporalIndex< Spot > stIndex2 = ds2.model.getSpatioTemporalIndex();
		stIndex1.readLock().lock();
		stIndex2.readLock().lock();
		try
		{
			final SpatialIndex< Spot > index1 = stIndex1.getSpatialIndex( timepoint );

			final SpatialIndex< Spot > index2 = stIndex2.getSpatialIndex( timepoint );
			final IncrementalNearestNeighborSearch< Spot > inns2 = index2.getIncrementalNearestNeighborSearch();

			for ( Spot spot1 : index1 )
			{
				final double radiusSqu = spot1.getBoundingSphereRadiusSquared();
				System.out.println( "spot1 = " + spot1 + " r^2 = " + radiusSqu );

				inns2.search( spot1 );
				while ( inns2.hasNext() )
				{
					inns2.fwd();
					final double dSqu = inns2.getSquareDistance();
					if ( dSqu > 2 * radiusSqu )
						break;
					final Spot spot2 = inns2.get();
					final boolean c12 = spotMath.containsCenter( spot1, spot2 );
					final boolean c21 = spotMath.containsCenter( spot2, spot1 );

					System.out.println( "     " + spot2 + " dSqu = " + dSqu + ", " + ( c12 ? "2 in 1 " : "------ " ) + ( c21 ? "1 in 2 " : "------ " ) );

				}

			}
		}
		finally
		{
			stIndex1.readLock().unlock();
			stIndex2.readLock().unlock();
		}

	}



	public static void main( String[] args ) throws IOException
	{
//		for ( String path : paths )
//		{
//			System.out.println("=================================================");
//			System.out.println( "path = " + path );
//			final Dataset dataset = new Dataset( path );
//			dataset.verify();
//			dataset.labels();
//		}

		final String path1 = paths[ 0 ];
		final String path2 = paths[ 4 ];
		System.out.println( "path1 = " + path1 );
		System.out.println( "path2 = " + path2 );

		final Dataset ds1 = new Dataset( path1 );
		final Dataset ds2 = new Dataset( path2 );

//		printSpotsPerTimepoint( ds1, ds2 );
		match( ds1, ds2,0 );
	}
}
