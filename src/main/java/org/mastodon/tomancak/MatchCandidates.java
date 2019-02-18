package org.mastodon.tomancak;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Locale;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import org.mastodon.collection.RefCollection;
import org.mastodon.collection.RefCollections;
import org.mastodon.collection.RefList;
import org.mastodon.collection.RefMaps;
import org.mastodon.collection.RefRefMap;
import org.mastodon.collection.ref.RefArrayList;
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
	private final double MAHALANOBIS_DISTSQU_CUTOFF;
	private final double RATIO_THRESHOLD_SQU;

	public MatchCandidates()
	{
		this( 1000, 1, 2.0 );
	}

	public MatchCandidates( final double distCutoff, final double mahalanobisDistCutoff, final double ratioThreshold )
	{
		ABSOLUTE_DISTSQU_CUTOFF = distCutoff * distCutoff;
		MAHALANOBIS_DISTSQU_CUTOFF = mahalanobisDistCutoff * mahalanobisDistCutoff;
		RATIO_THRESHOLD_SQU = ratioThreshold * ratioThreshold;
	}

	public MatchingGraph buildMatchingGraph( final Dataset dsA, final Dataset dsB )
	{
		final int minTimepoint = 0;
		final int maxTimepoint = Math.max( dsA.maxNonEmptyTimepoint(), dsB.maxNonEmptyTimepoint() );
		return buildMatchingGraph( dsA, dsB, minTimepoint, maxTimepoint );
	}

	public MatchingGraph buildMatchingGraph( final Dataset dsA, final Dataset dsB, final int minTimepoint, final int maxTimepoint )
	{
		final MatchingGraph matching = MatchingGraph.newWithAllSpots( dsA, dsB );
		for ( int timepoint = 0; timepoint <= maxTimepoint; timepoint++ )
		{
			final SpatialIndex< Spot > indexA = dsA.model().getSpatioTemporalIndex().getSpatialIndex( timepoint );
			final SpatialIndex< Spot > indexB = dsB.model().getSpatioTemporalIndex().getSpatialIndex( timepoint );
			addCandidates( matching, indexA, indexB );
			addCandidates( matching, indexB, indexA );
		}

		return matching;
	}

	private void addCandidates( final MatchingGraph matching, final SpatialIndex< Spot > indexA, final SpatialIndex< Spot > indexB )
	{
		final SpotMath spotMath = new SpotMath();
		final IncrementalNearestNeighborSearch< Spot > inns = indexB.getIncrementalNearestNeighborSearch();
		for ( final Spot spot1 : indexA )
		{
			inns.search( spot1 );
			while ( inns.hasNext() )
			{
				inns.fwd();
				final double dSqu = inns.getSquareDistance();
				if ( dSqu > ABSOLUTE_DISTSQU_CUTOFF )
					break;
				final Spot spot2 = inns.get();
				final double mdSqu = spotMath.mahalanobisDistSqu( spot1, spot2 );
				if ( mdSqu > MAHALANOBIS_DISTSQU_CUTOFF )
					break;
				matching.addEdge(
						matching.getVertex( spot1 ),
						matching.getVertex( spot2 )
				).init( dSqu, mdSqu );
			}
		}
	}

	public MatchingGraph pruneMatchingGraph( MatchingGraph graph )
	{
		final SpotMath spotMath = new SpotMath();
		final MatchingGraph matching = MatchingGraph.newWithAllSpots( graph );
		/*
		prune matching graph
			distance must be < th1 -- already done
			mahalanobis distance must be < th2 -- already done
			remaining edges qualify for creating conflict if no accepted match is found.
				accepted matches must be mutually nearest neighbors (wrt mahalanobis distance?)
				ratio of distance to 2nd neighbor must be > th3
				ratio of mahalanobis distance to 2nd neighbor must be > th4



		for each MatchingVertex v1:
			does it have any outgoing edges?
			no -->
				continue

			sort outgoing edges by mahalanobis distance

			do
				add edge[i]
			while |(i+1) <= |edge|) and (mdist(edge[i+1]) / mdist(edge[i]) > th3)
		 */
		final RefList< MatchingEdge > edges = RefCollections.createRefList( graph.edges() );
		for ( MatchingVertex v : graph.vertices() )
		{
			edges.clear();
			v.outgoingEdges().forEach( edges::add );
			if ( edges.isEmpty() )
				continue;
			edges.sort( Comparator.comparingDouble( MatchingEdge::getMahalDistSqu ) );
			for ( int i = 0; i < edges.size(); ++i )
			{
				final MatchingEdge ge = edges.get( i );
				final MatchingVertex source = matching.getVertex( ge.getSource().getSpot() );
				final MatchingVertex target = matching.getVertex( ge.getTarget().getSpot() );
				matching.addEdge( source, target ).init( ge.getDistSqu(), ge.getMahalDistSqu() );

				if ( i + 1 < edges.size() )
				{
					final MatchingEdge ne = edges.get( i + 1 );
					if ( ne.getMahalDistSqu() / ge.getMahalDistSqu() > RATIO_THRESHOLD_SQU )
						break;
				}
			}
		}

		// corresponding matching.v to graph.v
		MatchingVertex v = graph.vertices().iterator().next(); // from graph
		matching.getVertex( v.getSpot() );

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
		candidates.pruneMatchingGraph( graph );
	}
}
