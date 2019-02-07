package org.mastodon.tomancak;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.mastodon.project.MamutProject;
import org.mastodon.project.MamutProjectIO;
import org.mastodon.properties.ObjPropertyMap;
import org.mastodon.revised.model.mamut.Link;
import org.mastodon.revised.model.mamut.Model;
import org.mastodon.revised.model.mamut.ModelGraph;
import org.mastodon.revised.model.mamut.Spot;
import org.mastodon.revised.model.mamut.SpotPool;
import org.mastodon.revised.model.tag.ObjTags;
import org.mastodon.revised.model.tag.TagSetModel;
import org.mastodon.revised.model.tag.TagSetStructure;

import static org.mastodon.tomancak.MergingUtil.getMaxNonEmptyTimepoint;
import static org.mastodon.tomancak.MergingUtil.getNumTimepoints;
import static org.mastodon.tomancak.MergingUtil.spotToString;

/**
 * Reads model from a mastodon project file.
 * Access to the Model.
 * Convenience and debugging methods.
 */
public class Dataset
{
	private static Map< ModelGraph, Dataset > graphToDataset = new HashMap<>();

	public static Dataset getFor( ModelGraph graph )
	{
		return graphToDataset.get( graph );
	}

	private final MamutProject project;

	private final int numTimepoints;

	private final Model model;

	private final int maxNonEmptyTimepoint;

	public Dataset( String path ) throws IOException
	{
		project = new MamutProjectIO().load( path );
		numTimepoints = getNumTimepoints( project );
		model = new Model();
		try (final MamutProject.ProjectReader reader = project.openForReading())
		{
			model.loadRaw( reader );
		}
		maxNonEmptyTimepoint = getMaxNonEmptyTimepoint( model, numTimepoints );
		graphToDataset.put( model.getGraph(), this );

		verify();
	}

	public Model model()
	{
		return model;
	}

	public int maxNonEmptyTimepoint()
	{
		return maxNonEmptyTimepoint;
	}

	/**
	 * Checks that the model graph is a forest.
 	 */
	public void verify() throws IllegalStateException
	{
		for ( Spot spot : model.getGraph().vertices() )
		{
			if ( spot.incomingEdges().size() > 1 )
				throw new IllegalStateException( spot + " has more than one parent" );

			if ( spot.outgoingEdges().size() > 2 )
				throw new IllegalStateException( spot + " has more than two children" );
		}
	}

	/**
	 * Prints spots that have a set label.
	 * (exploratory)
	 */
	@Deprecated
	public void labels()
	{
		final SpotPool pool = ( SpotPool ) model.getGraph().vertices().getRefPool();
		ObjPropertyMap< Spot, String > labels = ( ObjPropertyMap< Spot, String > ) pool.labelProperty();
		for ( Spot spot : model.getGraph().vertices() )
			System.out.println( "spot = " + spot + " label=" + labels.get( spot ) );
	}

	/**
	 * (exploratory)
	 */
	@Deprecated
	public void tags()
	{
		final TagSetModel< Spot, Link > tsm = model.getTagSetModel();
		final TagSetStructure tss = tsm.getTagSetStructure();
		System.out.println( "tss = " + tss );

		System.out.println( "//////////////");
		for ( TagSetStructure.TagSet tagSet : tss.getTagSets() )
		{
			System.out.println( "  - " + tagSet.getName() + ", #" + tagSet.id() + ":" );
			for ( TagSetStructure.Tag tag : tagSet.getTags() )
			{
				final int numTaggedVertices = tsm.getVertexTags().getTaggedWith( tag ).size();
				final int numTaggedEdges = tsm.getEdgeTags().getTaggedWith( tag ).size();
				System.out.println( "      #" + tag.id() + ", " + tag.label() + ", color = " + String.format( "0x%08X", tag.color() )
				 + "  [" +numTaggedVertices + "/" + numTaggedEdges + "]" );
			}
		}

		final ObjTags< Spot > vt = tsm.getVertexTags();
		for ( Spot spot : model.getGraph().vertices() )
		{
			StringBuilder labels = new StringBuilder();
			for ( TagSetStructure.TagSet tagSet : tss.getTagSets() )
			{
				final TagSetStructure.Tag t = vt.tags( tagSet ).get( spot );
				if ( t != null )
					labels.append( " #" ).append( t.id() ).append( ":" ).append( t.label() );
			}
			if ( labels.length() > 0 )
			{
				System.out.println( spotToString( spot ) + labels );
			}
		}
	}
}
