package org.mastodon.tomancak;

import java.io.IOException;
import org.mastodon.collection.RefCollections;
import org.mastodon.collection.RefList;
import org.mastodon.kdtree.IncrementalNearestNeighborSearch;
import org.mastodon.revised.model.mamut.Spot;
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


	private static void tags( final Dataset dsA, final Dataset dsB )
	{
		MergeTags.mergeTagSetStructures(
				dsA.model().getTagSetModel().getTagSetStructure(),
				dsB.model().getTagSetModel().getTagSetStructure() );

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
//			dataset.tags();
//		}

		final String path1 = paths[ 0 ];
		final String path2 = paths[ 4 ];
		System.out.println( "path1 = " + path1 );
		System.out.println( "path2 = " + path2 );

		final Dataset ds1 = new Dataset( path1 );
		final Dataset ds2 = new Dataset( path2 );

//		runLocked( ds1, ds2, () -> match( ds1, ds2,0 ) );
		tags( ds1, ds2 );
	}
}
