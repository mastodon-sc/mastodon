package org.mastodon.tomancak;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import org.mastodon.collection.RefMaps;
import org.mastodon.collection.RefRefMap;
import org.mastodon.project.MamutProject;
import org.mastodon.project.MamutProjectIO;
import org.mastodon.revised.model.mamut.Link;
import org.mastodon.revised.model.mamut.Model;
import org.mastodon.revised.model.mamut.ModelGraph;
import org.mastodon.revised.model.mamut.Spot;
import org.mastodon.revised.model.tag.TagSetModel;
import org.mastodon.revised.model.tag.TagSetStructure;
import org.mastodon.revised.model.tag.TagSetStructure.Tag;
import org.mastodon.revised.model.tag.TagSetStructure.TagSet;

import static org.mastodon.tomancak.MetteMerging.paths;

public class JoinDataSets
{
	public static void copy( Dataset dataset, String destination )
	{
		copy( dataset, new File( destination ) );
	}

	public static class JoinedDataSet
	{
		private File datasetXmlFile;

		private final Model model;

		private final TagSetStructure tagSetStructure;

		private final TagSet mergeSourceTagSet;

		public JoinedDataSet()
		{
			model = new Model();
			tagSetStructure = new TagSetStructure();
			mergeSourceTagSet = tagSetStructure.createTagSet( "MergeSource" );
		}

		public void setDatasetXmlFile( File file )
		{
			datasetXmlFile = file;
		}

		/**
		 * @param projectRoot where to store the new project
		 */
		public void saveProject( final File projectRoot ) throws IOException
		{
			if ( datasetXmlFile == null )
				throw new IllegalStateException();

			final MamutProject project = new MamutProject( projectRoot, datasetXmlFile );
			try (final MamutProject.ProjectWriter writer = project.openForWriting())
			{
				new MamutProjectIO().save( project, writer );
				model.saveRaw( writer );
			}
		}

		/**
		 * Add copy of {@code dataset}.
		 * Tags of dataset are copied with {@code tagSetPrefix}.
		 * All copied edges and vertices are tagged with new "MergeSource" tag {@code (label,argb)}.
		 */
		public void addCopy( final Dataset dataset, final String tagSetPrefix, final String label, final int argb )
		{

			final ModelGraph sourceGraph = dataset.model().getGraph();
			final TagSetModel< Spot, Link > sourceTagSetModel = dataset.model().getTagSetModel();
			final TagSetStructure sourceTagSetStructure = sourceTagSetModel.getTagSetStructure();

			final ModelGraph destGraph = model.getGraph();

			final double[] pos = new double[ 3 ];
			final double[][] cov = new double[ 3 ][ 3 ];
			final Spot vref1 = destGraph.vertexRef();
			final Spot vref2 = destGraph.vertexRef();
			final Link eref = destGraph.edgeRef();

			/*
			 * spots / links
			 */

			final RefRefMap< Spot, Spot > vmap = RefMaps.createRefRefMap( sourceGraph.vertices(), destGraph.vertices() );
			for ( Spot sourceSpot : sourceGraph.vertices() )
			{
				final int tp = sourceSpot.getTimepoint();
				sourceSpot.localize( pos );
				sourceSpot.getCovariance( cov );
				final Spot destSpot = destGraph.addVertex( vref1 ).init( tp, pos, cov );
				vmap.put( sourceSpot, destSpot );
			}

			final RefRefMap< Link, Link > emap = RefMaps.createRefRefMap( sourceGraph.edges(), destGraph.edges() );
			for ( Link sourceEdge : sourceGraph.edges() )
			{
				final Link destEdge = destGraph.addEdge(
						vmap.get( sourceEdge.getSource( vref1 ) ),
						vmap.get( sourceEdge.getTarget( vref2 ) ),
						eref ).init();
				emap.put( sourceEdge, destEdge );
			}

			/*
			 * tags
			 */

			final Tag mergeSourceTag = mergeSourceTagSet.createTag( label, argb );
			final Map< Tag, Tag > tagMap = MergeTags.addTagSetStructureCopy( tagSetStructure, sourceTagSetStructure, tagSetPrefix ).tagMap;

			final TagSetModel< Spot, Link > destTagSetModel = model.getTagSetModel();
			destTagSetModel.setTagSetStructure( tagSetStructure );

			for ( Spot sourceSpot : sourceGraph.vertices() )
			{
				final Spot destSpot = vmap.get( sourceSpot, vref1 );
				for ( TagSet tagSet : sourceTagSetStructure.getTagSets() )
				{
					Tag tag = sourceTagSetModel.getVertexTags().tags( tagSet ).get( sourceSpot );
					if ( tag != null )
						destTagSetModel.getVertexTags().set( destSpot, tagMap.get( tag ) );
				}
				destTagSetModel.getVertexTags().set( destSpot, mergeSourceTag );
			}

			for ( Link sourceEdge : sourceGraph.edges() )
			{
				final Link destEdge = emap.get( sourceEdge, eref );
				for ( TagSet tagSet : sourceTagSetStructure.getTagSets() )
				{
					Tag tag = sourceTagSetModel.getEdgeTags().tags( tagSet ).get( sourceEdge );
					if ( tag != null )
						destTagSetModel.getEdgeTags().set( destEdge, tagMap.get( tag ) );
				}
				destTagSetModel.getEdgeTags().set( destEdge, mergeSourceTag );
			}
		}
	}

	public static void copy( Dataset dataset, File destination )
	{
		final JoinedDataSet jds = new JoinedDataSet();
		jds.setDatasetXmlFile( dataset.project().getDatasetXmlFile() );
		jds.addCopy( dataset, "dsA_", "A", 0xffff00ff );
		try
		{
			jds.saveProject( destination );
		}
		catch ( IOException e )
		{
			e.printStackTrace();
		}
	}

	public static void main( String[] args ) throws IOException
	{
		final String path1 = paths[ 0 ];
		final Dataset dataset = new Dataset( path1 );
		copy( dataset, "/Users/pietzsch/Desktop/Mastodon/merging/testcopy.mastodon" );

//		final String path1 = paths[ 0 ];
//		final String path2 = paths[ 4 ];
//		System.out.println( "path1 = " + path1 );
//		System.out.println( "path2 = " + path2 );
//
//		final Dataset ds1 = new Dataset( path1 );
//		final Dataset ds2 = new Dataset( path2 );
//
//		final TagSetStructure tss = new TagSetStructure();
//		final TagSetStructureMaps maps1 = MergeTags.addTagSetStructureCopy( tss, ds1.model().getTagSetModel().getTagSetStructure(), "(A) " );
//		final TagSetStructureMaps maps2 = MergeTags.addTagSetStructureCopy( tss, ds2.model().getTagSetModel().getTagSetStructure(), "(B) " );
//
//		System.out.println( "tss = " + tss );
	}
}
