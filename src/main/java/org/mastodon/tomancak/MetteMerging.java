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

import static org.mastodon.tomancak.MatchCandidates.avgOutDegree;
import static org.mastodon.tomancak.MatchCandidates.avgOutDegreeOfMatchedVertices;
import static org.mastodon.tomancak.MatchCandidates.maxOutDegree;

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

	public static MatchingGraph buildMatchingGraph( final Dataset dsA, final Dataset dsB, final int minTimepoint, final int maxTimepoint )
	{
		final MatchCandidates candidates = new MatchCandidates();

		MatchingGraph graph = candidates.buildMatchingGraph( dsA, dsB, minTimepoint, maxTimepoint );

		System.out.println( "avgOutDegree = " + avgOutDegree( graph ) );
		System.out.println( "avgOutDegreeOfMatchedVertices = " + avgOutDegreeOfMatchedVertices( graph ) );
		System.out.println( "maxOutDegree = " + maxOutDegree( graph ) );

		graph = candidates.pruneMatchingGraph( graph );

		System.out.println( "avgOutDegree = " + avgOutDegree( graph ) );
		System.out.println( "avgOutDegreeOfMatchedVertices = " + avgOutDegreeOfMatchedVertices( graph ) );
		System.out.println( "maxOutDegree = " + maxOutDegree( graph ) );

		return graph;
	}

	public static class OutputDataSet
	{
		private File datasetXmlFile;

		private final Model model;

		private final TagSetStructure tagSetStructure;

		private final TagSet conflictTagSet;

		public OutputDataSet()
		{
			model = new Model();
			tagSetStructure = new TagSetStructure();
			conflictTagSet = tagSetStructure.createTagSet( "Merge Conflict" );
		}

		public void setDatasetXmlFile( File file )
		{
			datasetXmlFile = file;
		}

		/**
		 * @param projectRoot
		 * 		where to store the new project
		 */
		public void saveProject( final File projectRoot ) throws IOException
		{
			if ( datasetXmlFile == null )
				throw new IllegalStateException();

			final MamutProject project = new MamutProject( projectRoot, datasetXmlFile );
			try ( final MamutProject.ProjectWriter writer = project.openForWriting() )
			{
				new MamutProjectIO().save( project, writer );
				model.saveRaw( writer );
			}
		}

		public Model getModel()
		{
			return model;
		}

		public Tag addSourceTag( String name, int color )
		{
			final TagSet ts = tagSetStructure.createTagSet( "Merge Source " + name );
			final Tag tag = ts.createTag( name, color );
			model.getTagSetModel().setTagSetStructure( tagSetStructure );
			return tag;
		}

		public Tag addConflictTag( String name, int color )
		{
			final Tag tag = conflictTagSet.createTag( name, color );
			model.getTagSetModel().setTagSetStructure( tagSetStructure );
			return tag;
		}
	}

	static void merge( final Dataset dsA, final Dataset dsB, final OutputDataSet output )
	{
		final int minTimepoint = 0;
		final int maxTimepoint = Math.max( dsA.maxNonEmptyTimepoint(), dsB.maxNonEmptyTimepoint() );
		final MatchingGraph matching = buildMatchingGraph( dsA, dsB, minTimepoint, maxTimepoint );
		final MatchingGraphUtils utils = new MatchingGraphUtils( matching );

		final Tag tagA = output.addSourceTag( "A", 0xffffff00 );
		final Tag tagB = output.addSourceTag( "B", 0xffff00ff );
		final Tag tagSingletonA = output.addConflictTag( "Singleton A", 0xffffffcc );
		final Tag tagSingletonB = output.addConflictTag( "Singleton B", 0xffffccff );
		final Tag tagMatchAB = output.addConflictTag( "MatchAB", 0xffccffcc );
		final Tag tagConflict = output.addConflictTag( "Conflict", 0xffff0000 );

		final ModelGraph graph = output.getModel().getGraph();
		final ObjTags< Spot > vertexTags = output.getModel().getTagSetModel().getVertexTags();

		final double[] pos = new double[ 3 ];
		final double[][] cov = new double[ 3 ][ 3 ];
		final Spot vref = graph.vertexRef();

/*
		for every spot a in A:
			add a' with shape and translated a tags
			add mapping MA: a --> a'
*/
		final ModelGraph graphA = dsA.model().getGraph();
		final RefRefMap< Spot, Spot > mapAtoDest = RefMaps.createRefRefMap( graphA.vertices(), graph.vertices() );
		for ( Spot spotA : graphA.vertices() )
		{
			final int tp = spotA.getTimepoint();
			spotA.localize( pos );
			spotA.getCovariance( cov );
			final Spot destSpot = graph.addVertex( vref ).init( tp, pos, cov );
			vertexTags.set( destSpot, tagA );
			vertexTags.set( destSpot, tagSingletonA );
			mapAtoDest.put( spotA, destSpot );
		}

/*
		for every spot b in B:
			if singleton (b):
				add b' with shape and translated b tags
				add mapping MB: b --> b'
			else if perfect match (a,b):
				get a'
				add translated b tags, checking for conflicts
				add mapping MB: b --> a'
			else
				add b' with shape and translated b tags
				add "conflict" tag to b' and any connected (and already present) c'
				add mapping MB: b --> b'
*/
		final ModelGraph graphB = dsB.model().getGraph();
		final RefRefMap< Spot, Spot > mapBtoDest = RefMaps.createRefRefMap( graphA.vertices(), graph.vertices() );
		final UndirectedDepthFirstIterator< MatchingVertex, MatchingEdge > miter = new UndirectedDepthFirstIterator<>( matching );
		for ( Spot spotB : graphB.vertices() )
		{
			final MatchingVertex mvB = matching.getVertex( spotB );

			if ( utils.isUnmatched( mvB ) )
			{
				final int tp = spotB.getTimepoint();
				spotB.localize( pos );
				spotB.getCovariance( cov );
				final Spot destSpot = graph.addVertex( vref ).init( tp, pos, cov );
				vertexTags.set( destSpot, tagB );
				vertexTags.set( destSpot, tagSingletonB );
				mapBtoDest.put( spotB, destSpot );
			}
			else if ( utils.isPerfectlyMatched( mvB ) )
			{
				final MatchingVertex mvA = mvB.outgoingEdges().get( 0 ).getTarget();
				final Spot spotA = mvA.getSpot( vref );
				final Spot destSpot = mapAtoDest.get( spotA );
				vertexTags.set( destSpot, tagB );
				vertexTags.set( destSpot, tagMatchAB );
				mapBtoDest.put( spotB, destSpot );
			}
			else
			{
				final int tp = spotB.getTimepoint();
				spotB.localize( pos );
				spotB.getCovariance( cov );
				final Spot destSpot = graph.addVertex( vref ).init( tp, pos, cov );
				vertexTags.set( destSpot, tagB );
				mapBtoDest.put( spotB, destSpot );

				miter.reset( mvB );
				while ( miter.hasNext() )
				{
					final MatchingVertex mv = miter.next();
					final Spot sourceSpot = mv.getSpot();
					final Spot spot;
					if ( sourceSpot.getModelGraph() == graphA )
						spot = mapAtoDest.get( sourceSpot );
					else
						spot = mapBtoDest.get( sourceSpot );
					if ( spot != null )
						vertexTags.set( spot, tagConflict );
				}
			}
		}

/*
		for every edge (a1,a2) in A
			get a1', a2' from mapping MA
			add edge (a1',a2') and translated (a1,a2) tags
*/
		for ( Link linkA : graphA.edges() )
		{
			final Spot source = mapAtoDest.get( linkA.getSource() );
			final Spot target = mapAtoDest.get( linkA.getTarget() );
			graph.addEdge( source, target );
		}

/*
		for every edge (b1,b2) in B
			get b1', b2' from mapping MB
			add edge (b1',b2') if not exists
			add translated (b1,b2) tags, checking for conflicts
*/
		for ( Link linkB : graphB.edges() )
		{
			final Spot source = mapBtoDest.get( linkB.getSource() );
			final Spot target = mapBtoDest.get( linkB.getTarget() );
			if ( graph.getEdge( source, target ) == null )
				graph.addEdge( source, target );
		}
	}


	static class MatchingGraphUtils
	{
		private final MatchingGraph matchingGraph;

		private final MatchingEdge eref1;
		private final MatchingEdge eref2;
		private final MatchingVertex vref1;
		private final MatchingVertex vref2;

		public MatchingGraphUtils( final MatchingGraph matchingGraph )
		{
			this.matchingGraph = matchingGraph;
			eref1 = matchingGraph.edgeRef();
			eref2 = matchingGraph.edgeRef();
			vref1 = matchingGraph.vertexRef();
			vref2 = matchingGraph.vertexRef();
		}

		public boolean isUnmatched( MatchingVertex mv )
		{
			return mv.edges().isEmpty();
		}

		/**
		 * {@code true} if the best target of {@code mv}, has {@code mv} as its best target in return.
		 * Assumes that outgoing edges are sorted by increasing mahalanobis distance.
		 */
		public boolean isPerfectlyMatched( MatchingVertex mv )
		{
			if ( mv.outgoingEdges().isEmpty() )
				return false;
			final MatchingVertex target = mv.outgoingEdges().get( 0, eref1 ).getTarget( vref1 );
			if ( target.outgoingEdges().isEmpty() )
				return false;
			final MatchingVertex targetsTarget = target.outgoingEdges().get( 0, eref2 ).getTarget( vref2 );
			return targetsTarget.equals( mv );
		}

//		public boolean isPerfectlyMatched( MatchingVertex mv )
//		{
//			if ( mv.outgoingEdges().size() != 1 )
//				return false;
//			if ( mv.incomingEdges().size() != 1 )
//				return false;
//			final MatchingVertex vTo = mv.outgoingEdges().get( 0, eref1 ).getTarget( vref1 );
//			final MatchingVertex vFrom = mv.incomingEdges().get( 0, eref2 ).getSource( vref2 );
//			return vTo.equals( vFrom );
//		}
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

		final OutputDataSet output = new OutputDataSet();
		merge( ds1, ds2, output );
		output.setDatasetXmlFile( ds1.project().getDatasetXmlFile() );

		final String resultPath = "/Users/pietzsch/Desktop/Mastodon/merging/conflicts.mastodon";
		try
		{
			output.saveProject( new File( resultPath ) );
		}
		catch ( IOException e )
		{
			e.printStackTrace();
		}

		System.out.println( "done" );

		try
		{
			openInMastodon( resultPath );
		}
		catch ( Exception e )
		{
			e.printStackTrace();
		}
	}

	public static void openInMastodon( final String projectPath ) throws Exception
	{
		Locale.setDefault( Locale.US );
		UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );

		final Mastodon mastodon = new Mastodon();
		new Context().inject( mastodon );
		mastodon.run();
		mastodon.mainWindow.setDefaultCloseOperation( WindowConstants.EXIT_ON_CLOSE );

		final WindowManager windowManager = mastodon.windowManager;

		final MamutProject project = new MamutProjectIO().load( projectPath );

		windowManager.getProjectManager().open( project );
		SwingUtilities.invokeAndWait( () -> {
			windowManager.createBigDataViewer();
			windowManager.createTrackScheme();
		} );
	}

}
