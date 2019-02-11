package org.mastodon.tomancak;

import java.io.File;
import java.io.IOException;
import org.mastodon.collection.RefCollection;
import org.mastodon.collection.RefMaps;
import org.mastodon.collection.RefObjectMap;
import org.mastodon.collection.RefRefMap;
import org.mastodon.pool.PoolCollectionWrapper;
import org.mastodon.project.MamutProject;
import org.mastodon.project.MamutProjectIO;
import org.mastodon.revised.model.mamut.Link;
import org.mastodon.revised.model.mamut.Model;
import org.mastodon.revised.model.mamut.ModelGraph;
import org.mastodon.revised.model.mamut.Spot;
import org.mastodon.revised.model.tag.TagSetStructure;
import org.mastodon.tomancak.MergeTags.TagSetStructureMaps;

import static org.mastodon.tomancak.MetteMerging.paths;

public class JoinDataSets
{
	public static void copy( Dataset dataset, String destination )
	{
		copy( dataset, new File( destination ) );
	}

	public static void copy( Dataset dataset, File destination )
	{
		MamutProject project = new MamutProject(
				destination,
				dataset.project().getDatasetXmlFile() );

		final ModelGraph sourceGraph = dataset.model().getGraph();

		final Model destModel = new Model(); // TODO copy units
		final ModelGraph destGraph = destModel.getGraph();

		final double[] pos = new double[ 3 ];
		final double[][] cov = new double[ 3 ][ 3 ];
		final Spot vref1 = destGraph.vertexRef();
		final Spot vref2 = destGraph.vertexRef();
		final Link eref = destGraph.edgeRef();

		final RefRefMap< Spot, Spot > vmap = RefMaps.createRefRefMap( sourceGraph.vertices(), destGraph.vertices() );
		for ( Spot sourceSpot : sourceGraph.vertices() )
		{
			final int tp = sourceSpot.getTimepoint();
			sourceSpot.localize( pos );
			sourceSpot.getCovariance( cov );
			final Spot destSpot = destGraph.addVertex( vref1 ).init( tp, pos, cov );
			vmap.put( sourceSpot, destSpot );
		}

		final RefRefMap< Link, Link > eMap = RefMaps.createRefRefMap( sourceGraph.edges(), destGraph.edges() );
		for ( Link sourceEdge : sourceGraph.edges() )
		{
			final Link destEdge = destGraph.addEdge(
					vmap.get( sourceEdge.getSource( vref1 ) ),
					vmap.get( sourceEdge.getTarget( vref2 ) ),
					eref ).init();
			eMap.put( sourceEdge, destEdge );
		}

		try (final MamutProject.ProjectWriter writer = project.openForWriting())
		{
			new MamutProjectIO().save( project, writer );
			destModel.saveRaw( writer );
		}
		catch ( IOException e )
		{
			throw new RuntimeException( e );
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
