package org.mastodon.tomancak;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import org.mastodon.collection.RefCollections;
import org.mastodon.collection.RefList;
import org.mastodon.collection.RefMaps;
import org.mastodon.collection.RefRefMap;
import org.mastodon.graph.algorithm.traversal.UndirectedDepthFirstIterator;
import org.mastodon.kdtree.IncrementalNearestNeighborSearch;
import org.mastodon.project.MamutProject;
import org.mastodon.project.MamutProjectIO;
import org.mastodon.revised.mamut.Mastodon;
import org.mastodon.revised.mamut.WindowManager;
import org.mastodon.revised.model.mamut.Link;
import org.mastodon.revised.model.mamut.Model;
import org.mastodon.revised.model.mamut.ModelGraph;
import org.mastodon.revised.model.mamut.Spot;
import org.mastodon.revised.model.tag.ObjTags;
import org.mastodon.revised.model.tag.TagSetStructure;
import org.mastodon.revised.model.tag.TagSetStructure.Tag;
import org.mastodon.revised.model.tag.TagSetStructure.TagSet;
import org.mastodon.spatial.SpatialIndex;
import org.scijava.Context;

public class MatchCandidates
{
	public static final String basepath = "/Users/pietzsch/Desktop/Mastodon/merging/Mastodon-files_SimView2_20130315/";

	public static final String[] paths = {
			basepath + "1.SimView2_20130315_Mastodon_Automat-segm-t0-t300",
			basepath + "2.SimView2_20130315_Mastodon_MHT",
			basepath + "3.Pavel manual",
			basepath + "4.Vlado_TrackingPlatynereis",
			basepath + "5.SimView2_20130315_Mastodon_Automat-segm-t0-t300_JG"
	};

	private final double ABSOLUTE_DISTSQU_CUTOFF;
	private final double RADIUSSQU_FACTOR_CUTOFF;

	public MatchCandidates()
	{
		this( 1000, 4 );
	}

	public MatchCandidates( final double distCutoff, final double radiusFactorCutoff )
	{
		ABSOLUTE_DISTSQU_CUTOFF = distCutoff * distCutoff;
		RADIUSSQU_FACTOR_CUTOFF = radiusFactorCutoff * radiusFactorCutoff;
	}

	public MatchingGraph buildMatchingGraph( final Dataset dsA, final Dataset dsB )
	{
		final int minTimepoint = 0;
		final int maxTimepoint = Math.max( dsA.maxNonEmptyTimepoint(), dsB.maxNonEmptyTimepoint() );
		return buildMatchingGraph( dsA, dsB, minTimepoint, maxTimepoint );
	}

	public MatchingGraph buildMatchingGraph( final Dataset dsA, final Dataset dsB, final int minTimepoint, final int maxTimepoint )
	{
		final SpotMath spotMath = new SpotMath();

		final MatchingGraph matching = MatchingGraph.newWithAllSpots( dsA, dsB );
		for ( int timepoint = 0; timepoint <= maxTimepoint; timepoint++ )
		{
			final SpatialIndex< Spot > indexA = dsA.model().getSpatioTemporalIndex().getSpatialIndex( timepoint );
			final SpatialIndex< Spot > indexB = dsB.model().getSpatioTemporalIndex().getSpatialIndex( timepoint );

			IncrementalNearestNeighborSearch< Spot > inns = indexB.getIncrementalNearestNeighborSearch();
			for ( final Spot spot1 : indexA )
			{
				final double radiusSqu = spot1.getBoundingSphereRadiusSquared();
				inns.search( spot1 );
				while ( inns.hasNext() )
				{
					inns.fwd();
					final double dSqu = inns.getSquareDistance();
					if ( dSqu > radiusSqu * RADIUSSQU_FACTOR_CUTOFF )
						break;
					final Spot spot2 = inns.get();
					final double mdSqu = spotMath.mahalanobisDistSqu( spot1, spot2 );
					matching.addEdge(
							matching.getVertex( spot1 ),
							matching.getVertex( spot2 )
					).init( dSqu, mdSqu );
				}
			}

			inns = indexA.getIncrementalNearestNeighborSearch();
			for ( final Spot spot1 : indexB )
			{
				final double radiusSqu = spot1.getBoundingSphereRadiusSquared();
				inns.search( spot1 );
				while ( inns.hasNext() )
				{
					inns.fwd();
					final double dSqu = inns.getSquareDistance();
					if ( dSqu > radiusSqu * RADIUSSQU_FACTOR_CUTOFF )
						break;
					if ( dSqu > ABSOLUTE_DISTSQU_CUTOFF )
						break;
					final Spot spot2 = inns.get();
					final double mdSqu = spotMath.mahalanobisDistSqu( spot1, spot2 );
					matching.addEdge(
							matching.getVertex( spot1 ),
							matching.getVertex( spot2 )
					).init( dSqu, mdSqu );
				}
			}
		}

		return matching;
	}

	public static double avgOutDegree( final MatchingGraph graph )
	{
		long sumOutEdges = 0;
		for ( MatchingVertex v : graph.vertices() )
			sumOutEdges += v.outgoingEdges().size();
		return ( double ) sumOutEdges / graph.vertices().size();
	}

	public static double avgOutDegreeOfMatchedVertices( final MatchingGraph graph )
	{
		long sumOutEdges = 0;
		int numMatchedVertices = 0;
		for ( MatchingVertex v : graph.vertices() )
			if ( !v.outgoingEdges().isEmpty() )
			{
				sumOutEdges += v.outgoingEdges().size();
				numMatchedVertices++;
			}
		return ( double ) sumOutEdges / numMatchedVertices;
	}

	public static int maxOutDegree( final MatchingGraph graph )
	{
		int maxOutEdges = 0;
		for ( MatchingVertex v : graph.vertices() )
			maxOutEdges = Math.max( maxOutEdges, v.outgoingEdges().size() );
		return maxOutEdges;
	}

	public static void main( String[] args ) throws IOException
	{
		final String path1 = paths[ 0 ];
		final String path2 = paths[ 4 ];
		System.out.println( "path1 = " + path1 );
		System.out.println( "path2 = " + path2 );

		final Dataset ds1 = new Dataset( path1 );
		final Dataset ds2 = new Dataset( path2 );

		final MatchCandidates candidates = new MatchCandidates();
		final MatchingGraph graph = candidates.buildMatchingGraph( ds1, ds2 );
		System.out.println( "avgOutDegree = " + avgOutDegree( graph ) );
		System.out.println( "avgOutDegreeOfMatchedVertices = " + avgOutDegreeOfMatchedVertices( graph ) );
		System.out.println( "maxOutDegree = " + maxOutDegree( graph ) );
	}
}
