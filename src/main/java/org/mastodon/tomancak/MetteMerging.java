package org.mastodon.tomancak;

import bdv.spimdata.SpimDataMinimal;
import bdv.spimdata.XmlIoSpimDataMinimal;
import java.io.IOException;
import mpicbg.spim.data.SpimDataException;
import org.mastodon.collection.RefCollections;
import org.mastodon.collection.RefList;
import org.mastodon.kdtree.IncrementalNearestNeighborSearch;
import org.mastodon.project.MamutProject;
import org.mastodon.revised.model.mamut.Model;
import org.mastodon.revised.model.mamut.Spot;
import org.mastodon.revised.util.DummySpimData;
import org.mastodon.spatial.SpatialIndex;
import org.mastodon.spatial.SpatioTemporalIndex;

import static org.mastodon.tomancak.MergingUtil.spotToString;

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

		final SpatioTemporalIndex< Spot > stIndex1 = ds1.model().getSpatioTemporalIndex();
		final SpatioTemporalIndex< Spot > stIndex2 = ds2.model().getSpatioTemporalIndex();
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

	static void runLocked( final Dataset dsA, final Dataset dsB, final Runnable runnable )
	{
		final SpatioTemporalIndex< Spot > stIndexA = dsA.model().getSpatioTemporalIndex();
		final SpatioTemporalIndex< Spot > stIndexB = dsB.model().getSpatioTemporalIndex();
		stIndexA.readLock().lock();
		stIndexB.readLock().lock();
		try
		{
			runnable.run();
		}
		finally
		{
			stIndexA.readLock().unlock();
			stIndexB.readLock().unlock();
		}
	}

	static void match( final Dataset dsA, final Dataset dsB, final int timepoint )
	{
		final SpotMath spotMath = new SpotMath();

		final SpatioTemporalIndex< Spot > stIndexA = dsA.model().getSpatioTemporalIndex();
		final SpatialIndex< Spot > indexA = stIndexA.getSpatialIndex( timepoint );
		final SpatioTemporalIndex< Spot > stIndexB = dsB.model().getSpatioTemporalIndex();
		final SpatialIndex< Spot > indexB = stIndexB.getSpatialIndex( timepoint );

		final MatchingGraph matching = new MatchingGraph( dsA.model().getGraph(), dsB.model().getGraph() );
		for ( Spot spot : indexA )
			matching.getVertex( spot );
		for ( Spot spot : indexB )
			matching.getVertex( spot );

		IncrementalNearestNeighborSearch< Spot > inns = indexB.getIncrementalNearestNeighborSearch();
		for ( Spot spot1 : indexA )
		{
			final double radiusSqu = spot1.getBoundingSphereRadiusSquared();
			inns.search( spot1 );
			while ( inns.hasNext() )
			{
				inns.fwd();
				final double dSqu = inns.getSquareDistance();
				if ( dSqu > radiusSqu )
					break;
				final Spot spot2 = inns.get();
				if ( spotMath.containsCenter( spot1, spot2 ) )
					matching.addEdge( matching.getVertex( spot1 ), matching.getVertex( spot2 ) );
			}
		}

		inns = indexA.getIncrementalNearestNeighborSearch();
		for ( Spot spot1 : indexB )
		{
			final double radiusSqu = spot1.getBoundingSphereRadiusSquared();
			inns.search( spot1 );
			while ( inns.hasNext() )
			{
				inns.fwd();
				final double dSqu = inns.getSquareDistance();
				if ( dSqu > radiusSqu )
					break;
				final Spot spot2 = inns.get();
				if ( spotMath.containsCenter( spot1, spot2 ) )
					matching.addEdge( matching.getVertex( spot1 ), matching.getVertex( spot2 ) );
			}
		}

		checkMatchingGraph( matching );

		RefList< MatchingVertex > seeds = RefCollections.createRefList( matching.vertices() );
		for ( final MatchingVertex v : matching.vertices() )
		{
			if ( v.edges().isEmpty() )
			{
				seeds.add( v );
			}
			else if ( v.outgoingEdges().size() == 1 && v.incomingEdges().size() == 1 )
			{
				final MatchingVertex w = v.outgoingEdges().get( 0 ).getTarget();
				if ( w.equals( v.incomingEdges().get( 0 ).getSource() ) )
				{
					if ( !seeds.contains( w ) )
						seeds.add( v );
				}
			}
		}

		for ( final MatchingVertex v : seeds )
		{
			final Spot spot1 = v.getSpot();
			if ( v.edges().isEmpty() )
			{
				System.out.println( "[add]   " + spotToString( spot1 ) + " (ds " + v.graphId() + ")" );
			}
			else
			{
				final MatchingVertex v2 = v.outgoingEdges().get( 0 ).getTarget();
				final Spot spot2 = v2.getSpot();
				System.out.println( "[merge] " + spotToString( spot1 ) + " (ds " + v.graphId() + ")" );
				System.out.println( "        " + spotToString( spot2 ) + " (ds " + v2.graphId() + ")" );
			}
		}
	}

	private static void checkMatchingGraph( final MatchingGraph matching )
	{
		for ( final MatchingVertex v : matching.vertices() )
		{
			if ( v.edges().size() == 0 )
				// not matched
				continue;

			if ( v.outgoingEdges().size() == 1 &&
					v.incomingEdges().size() == 1 &&
					v.outgoingEdges().get( 0 ).getTarget().equals( v.incomingEdges().get( 0 ).getSource() ) )
				// perfectly matched
				continue;

			// otherwise matching is ambiguous
			System.out.println( "ambiguous: " + v.getSpot() );
		}
	}

	public static void main( String[] args ) throws IOException
	{
		for ( String path : paths )
		{
			System.out.println("=================================================");
			System.out.println( "path = " + path );
			final Dataset dataset = new Dataset( path );
			dataset.verify();
//			dataset.labels();
			dataset.tags();
		}

//		final String path1 = paths[ 0 ];
//		final String path2 = paths[ 4 ];
//		System.out.println( "path1 = " + path1 );
//		System.out.println( "path2 = " + path2 );
//
//		final Dataset ds1 = new Dataset( path1 );
//		final Dataset ds2 = new Dataset( path2 );
//
//		ds1.tags();
//		ds2.tags();

//		printSpotsPerTimepoint( ds1, ds2 );
//		runLocked( ds1, ds2, () -> match( ds1, ds2,0 ) );
	}
}
