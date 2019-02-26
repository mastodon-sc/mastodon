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
import org.mastodon.revised.model.tag.TagSetModel;
import org.mastodon.revised.model.tag.TagSetStructure;
import org.mastodon.revised.model.tag.TagSetStructure.Tag;
import org.mastodon.revised.model.tag.TagSetStructure.TagSet;
import org.mastodon.spatial.SpatialIndex;
import org.mastodon.tomancak.MergeTags.TagSetStructureMaps;
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

		private final TagSet tagConflictTagSet;

		public OutputDataSet()
		{
			model = new Model();
			tagSetStructure = new TagSetStructure();
			conflictTagSet = tagSetStructure.createTagSet( "Merge Conflict" );
			tagConflictTagSet = tagSetStructure.createTagSet( "Merge Conflict (Tags)" );
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

		public Tag addTagConflictTag( String name, int color )
		{
			final Tag tag = tagConflictTagSet.createTag( name, color );
			model.getTagSetModel().setTagSetStructure( tagSetStructure );
			return tag;
		}

		public TagSetStructure getTagSetStructure()
		{
			return tagSetStructure;
		}

		public void updateTagSetModel()
		{
			model.getTagSetModel().setTagSetStructure( tagSetStructure );
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
		final Tag tagTagConflict = output.addTagConflictTag( "Tag Conflict", 0xffff0000 );

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
		for every edge (a1,a2) in A
			get a1', a2' from mapping MA
			add edge (a1',a2') and translated (a1,a2) tags
*/
		final RefRefMap< Link, Link > mapAtoDestLinks = RefMaps.createRefRefMap( graphA.edges(), graph.edges() );
		for ( Link linkA : graphA.edges() )
		{
			final Spot source = mapAtoDest.get( linkA.getSource() );
			final Spot target = mapAtoDest.get( linkA.getTarget() );
			final Link destLink = graph.addEdge( source, target );
			mapAtoDestLinks.put( linkA, destLink );
		}

/*
		for every spot b in B, ordered by ascending timepoint!:
			if singleton (b):
				add b' with shape and translated b tags
				add mapping MB: b --> b'
			else if perfect match (a,b):
				get a'
				if b has incoming edge (c,b) AND a has incoming edge (d,a)
					get c', d'
					if c' != d':
						add b' with shape and translated b tags
						add mapping MB: b --> b'
						add "conflict" tag to a' and b'
						continue;
				(else:)
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
		for ( int timepoint = 0; timepoint <= maxTimepoint; timepoint++ )
		{
			final SpatialIndex< Spot > indexB = dsB.model().getSpatioTemporalIndex().getSpatialIndex( timepoint );
			for ( Spot spotB : indexB )
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
					final Spot destSpotA = mapAtoDest.get( spotA );
					if ( ! ( spotB.incomingEdges().isEmpty() || spotA.incomingEdges().isEmpty() ) )
					{
						final Spot spotC = spotB.incomingEdges().get( 0 ).getSource();
						final Spot spotD = spotA.incomingEdges().get( 0 ).getSource();
						final Spot destSpotC = mapBtoDest.get( spotC );
						final Spot destSpotD = mapAtoDest.get( spotD );
						if ( !destSpotC.equals( destSpotD ) )
						{
							final int tp = spotB.getTimepoint();
							spotB.localize( pos );
							spotB.getCovariance( cov );
							final Spot destSpotB = graph.addVertex( vref ).init( tp, pos, cov );
							vertexTags.set( destSpotB, tagB );
							mapBtoDest.put( spotB, destSpotB );
							vertexTags.set( destSpotB, tagConflict );
							vertexTags.set( destSpotA, tagConflict );
							continue;
						}
					}
					vertexTags.set( destSpotA, tagB );
					vertexTags.set( destSpotA, tagMatchAB );
					mapBtoDest.put( spotB, destSpotA );
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
		}

/*
		for every edge (b1,b2) in B
			get b1', b2' from mapping MB
			add edge (b1',b2') if not exists
			add translated (b1,b2) tags, checking for conflicts
*/
		final RefRefMap< Link, Link > mapBtoDestLinks = RefMaps.createRefRefMap( graphB.edges(), graph.edges() );
		for ( Link linkB : graphB.edges() )
		{
			final Spot source = mapBtoDest.get( linkB.getSource() );
			final Spot target = mapBtoDest.get( linkB.getTarget() );
			Link destLink = graph.getEdge( source, target );
			if ( destLink == null )
				destLink = graph.addEdge( source, target );
			mapBtoDestLinks.put( linkB, destLink );
		}




		/*
		 * ========================================
		 *           transfer tags
		 * ========================================
		 */

		final TagSetModel< Spot, Link > tsm = output.getModel().getTagSetModel();
		final TagSetStructure tss = output.getTagSetStructure();
		final TagSetModel< Spot, Link > tsmA = dsA.model().getTagSetModel();
		final TagSetModel< Spot, Link > tsmB = dsB.model().getTagSetModel();
		final TagSetStructure tssA = tsmA.getTagSetStructure();
		final TagSetStructure tssB = tsmB.getTagSetStructure();
		final TagSetStructureMaps tssAtoCopy = MergeTags.addTagSetStructureCopy( tss, tssA, "((A)) " );
		final TagSetStructureMaps tssBtoCopy = MergeTags.addTagSetStructureCopy( tss, tssB, "((B)) " );
		final TagSetStructureMaps tssAtoDest = MergeTags.mergeTagSetStructure( tss, tssA );
		final TagSetStructureMaps tssBtoDest = MergeTags.mergeTagSetStructure( tss, tssB );
		output.updateTagSetModel();

/*
		for every spot a in A:
			get a'
			for every tagset in A:
				get tag t of (a, tagset)
				if t exists:
					get t' as copy ((A)) of t
					set t' for a'
					get t" as merge of t
					set t" for a'
		analogous for links in A...
*/
		for ( Spot spotA : graphA.vertices() )
		{
			final Spot destSpot = mapAtoDest.get( spotA );
			for ( TagSet tagSet : tssA.getTagSets() )
			{
				final Tag tag = tsmA.getVertexTags().tags( tagSet ).get( spotA );
				if ( tag != null )
				{
					// copy ((A))
					tsm.getVertexTags().set( destSpot, tssAtoCopy.tagMap.get( tag ) );

					// merged
					tsm.getVertexTags().set( destSpot, tssAtoDest.tagMap.get( tag ) );
				}
			}
		}
		for ( Link linkA : graphA.edges() )
		{
			final Link destLink = mapAtoDestLinks.get( linkA );
			for ( TagSet tagSet : tssA.getTagSets() )
			{
				final Tag tag = tsmA.getEdgeTags().tags( tagSet ).get( linkA );
				if ( tag != null )
				{
					// copy ((A))
					tsm.getEdgeTags().set( destLink, tssAtoCopy.tagMap.get( tag ) );

					// merged
					tsm.getEdgeTags().set( destLink, tssAtoDest.tagMap.get( tag ) );
				}
			}
		}

/*
		for every spot b in B:
			get b'
			for every tagset in B:
				get tag t of (b, tagset)
				if t != null:
					get t' as copy ((B)) of t
					set t' for b'
					get t" as merge of t
					get tagset" as merge of tagset
					get tag x" og (b', tagset")
					if x" exists and x" != t":
						mark tag conflict for b'
					else:
						set t" for b'
		analogous for links in B...
*/
		for ( Spot spotB : graphB.vertices() )
		{
			final Spot destSpot = mapBtoDest.get( spotB );
			for ( TagSet tagSet : tssB.getTagSets() )
			{
				final Tag tag = tsmB.getVertexTags().tags( tagSet ).get( spotB );
				if ( tag != null )
				{
					// copy ((B))
					tsm.getVertexTags().set( destSpot, tssBtoCopy.tagMap.get( tag ) );

					// merged
					final TagSet destTagSet = tssBtoDest.tagSetMap.get( tagSet );
					final Tag destTag = tsm.getVertexTags().tags( destTagSet ).get( destSpot );
					final Tag expectedDestTag = tssBtoDest.tagMap.get( tag );
					if ( destTag == null )
						tsm.getVertexTags().set( destSpot, expectedDestTag );
					else if ( !destTag.equals( expectedDestTag ) )
						tsm.getVertexTags().set( destSpot, tagTagConflict );
				}
			}
		}
		for ( Link linkB : graphB.edges() )
		{
			final Link destLink = mapBtoDestLinks.get( linkB );
			for ( TagSet tagSet : tssB.getTagSets() )
			{
				final Tag tag = tsmB.getEdgeTags().tags( tagSet ).get( linkB );
				if ( tag != null )
				{
					// copy ((B))
					tsm.getEdgeTags().set( destLink, tssBtoCopy.tagMap.get( tag ) );

					// merged
					final TagSet destTagSet = tssBtoDest.tagSetMap.get( tagSet );
					final Tag destTag = tsm.getEdgeTags().tags( destTagSet ).get( destLink );
					final Tag expectedDestTag = tssBtoDest.tagMap.get( tag );
					if ( destTag == null )
						tsm.getEdgeTags().set( destLink, expectedDestTag );
					else if ( !destTag.equals( expectedDestTag ) )
						tsm.getEdgeTags().set( destLink, tagTagConflict );
				}
			}
		}




		/*
		 * ========================================
		 *           transfer labels
		 * ========================================
		 */

		//
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

//		MergeTags.mergeTagSetStructures(
//				ds1.model().getTagSetModel().getTagSetStructure(),
//				ds2.model().getTagSetModel().getTagSetStructure() );

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
