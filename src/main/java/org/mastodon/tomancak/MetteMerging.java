package org.mastodon.tomancak;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import org.mastodon.collection.RefCollections;
import org.mastodon.collection.RefList;
import org.mastodon.graph.algorithm.traversal.UndirectedDepthFirstIterator;
import org.mastodon.kdtree.IncrementalNearestNeighborSearch;
import org.mastodon.project.MamutProject;
import org.mastodon.project.MamutProjectIO;
import org.mastodon.revised.mamut.Mastodon;
import org.mastodon.revised.mamut.WindowManager;
import org.mastodon.revised.model.mamut.Model;
import org.mastodon.revised.model.mamut.ModelGraph;
import org.mastodon.revised.model.mamut.Spot;
import org.mastodon.revised.model.tag.ObjTags;
import org.mastodon.revised.model.tag.TagSetStructure;
import org.mastodon.revised.model.tag.TagSetStructure.Tag;
import org.mastodon.revised.model.tag.TagSetStructure.TagSet;
import org.mastodon.spatial.SpatialIndex;
import org.scijava.Context;

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

	public static MatchingGraph buildMatchingGraph( final Dataset dsA, final Dataset dsB )
	{
		final int minTimepoint = 0;
		final int maxTimepoint = Math.max( dsA.maxNonEmptyTimepoint(), dsB.maxNonEmptyTimepoint() );
		return buildMatchingGraph( dsA, dsB, minTimepoint, maxTimepoint );
	}

	public static MatchingGraph buildMatchingGraph( final Dataset dsA, final Dataset dsB, final int minTimepoint, final int maxTimepoint )
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
					if ( dSqu > radiusSqu )
						break;
					final Spot spot2 = inns.get();
					if ( spotMath.containsCenter( spot1, spot2 ) )
						matching.addEdge( matching.getVertex( spot1 ), matching.getVertex( spot2 ) );
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
					if ( dSqu > radiusSqu )
						break;
					final Spot spot2 = inns.get();
					if ( spotMath.containsCenter( spot1, spot2 ) )
						matching.addEdge( matching.getVertex( spot1 ), matching.getVertex( spot2 ) );
				}
			}
		}

		return matching;
	}

	public static class OutputDataSet
	{
		private File datasetXmlFile;

		private final Model model;

		private final TagSetStructure tagSetStructure;

		private final TagSet sourceTagSet;

		private final TagSet conflictTagSet;

		public OutputDataSet()
		{
			model = new Model();
			tagSetStructure = new TagSetStructure();
			sourceTagSet = tagSetStructure.createTagSet( "Merge Source" );
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
			final Tag tag = sourceTagSet.createTag( name, color );
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

	static void match( final Dataset dsA, final Dataset dsB, final OutputDataSet output )
	{
		match( dsA, dsB, output,-1 );
	}

	static void match( final Dataset dsA, final Dataset dsB, final OutputDataSet output, final int tp )
	{
		final int minTimepoint = tp >= 0 ? tp : 0;
		final int maxTimepoint = tp >= 0 ? tp : Math.max( dsA.maxNonEmptyTimepoint(), dsB.maxNonEmptyTimepoint() );
		final MatchingGraph matching = buildMatchingGraph( dsA, dsB, minTimepoint, maxTimepoint );

		System.out.println( "conflicts:" );
		System.out.println( "----------" );

		final Tag tagA = output.addSourceTag( "A", 0xff00ff00 );
		final Tag tagB = output.addSourceTag( "B", 0xffff00ff );
		final ModelGraph graph = output.getModel().getGraph();
		final ObjTags< Spot > vertexTags = output.getModel().getTagSetModel().getVertexTags();

		final double[] pos = new double[ 3 ];
		final double[][] cov = new double[ 3 ][ 3 ];
		final Spot vref = graph.vertexRef();

		final RefList< MatchingVertex > addedMatchingVertices = RefCollections.createRefList( matching.vertices() );
		final UndirectedDepthFirstIterator< MatchingVertex, MatchingEdge > miter = new UndirectedDepthFirstIterator<>( matching );
		for ( MatchingVertex v : matching.vertices() )
		{
			if ( v.edges().size() == 0 )
				// not matched
				continue;

			if ( v.outgoingEdges().size() == 1 &&
					v.incomingEdges().size() == 1 &&
					v.outgoingEdges().get( 0 ).getTarget().equals( v.incomingEdges().get( 0 ).getSource() ) )
				// perfectly matched
				continue;

			miter.reset( v );
			while ( miter.hasNext() )
			{
				final MatchingVertex mv = miter.next();
				if ( !addedMatchingVertices.contains( mv ) )
				{
					addedMatchingVertices.add( mv );
					final Spot sourceSpot = mv.getSpot();
					final int stp = sourceSpot.getTimepoint();
					sourceSpot.localize( pos );
					sourceSpot.getCovariance( cov );
					final Spot destSpot = graph.addVertex( vref ).init( stp, pos, cov );
					vertexTags.set( destSpot, mv.graphId() == 0 ? tagA : tagB );
				}
			}
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
//			dataset.tags();
//		}

		final String path1 = paths[ 0 ];
		final String path2 = paths[ 4 ];
		System.out.println( "path1 = " + path1 );
		System.out.println( "path2 = " + path2 );

		final Dataset ds1 = new Dataset( path1 );
		final Dataset ds2 = new Dataset( path2 );

		final OutputDataSet output = new OutputDataSet();
		match( ds1, ds2, output );
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
