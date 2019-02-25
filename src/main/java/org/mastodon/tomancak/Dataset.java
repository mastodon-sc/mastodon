package org.mastodon.tomancak;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.mastodon.project.MamutProject;
import org.mastodon.project.MamutProjectIO;
import org.mastodon.revised.model.mamut.Model;
import org.mastodon.revised.model.mamut.ModelGraph;
import org.mastodon.revised.model.mamut.Spot;

import static org.mastodon.tomancak.MergingUtil.getMaxNonEmptyTimepoint;
import static org.mastodon.tomancak.MergingUtil.getNumTimepoints;

/**
 * Reads model from a mastodon project file.
 * Access to the Model.
 * Convenience and debugging methods.
 */
public class Dataset
{
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

		verify();
	}

	public Model model()
	{
		return model;
	}

	public MamutProject project()
	{
		return project;
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
}
