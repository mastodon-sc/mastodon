package org.mastodon.graph;

import java.io.IOException;

import org.mastodon.graph.io.RawGraphIO.FileIdToGraphMap;
import org.mastodon.mamut.feature.MamutRawFeatureModelIO;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.ModelUtils;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.model.branch.ModelBranchGraph;
import org.mastodon.mamut.project.MamutProject;
import org.mastodon.mamut.project.MamutProjectIO;
import org.scijava.Context;

public class BranchGraphExample
{

	public static void main( final String[] args ) throws IOException
	{
		try (Context context = new Context())
		{
			final MamutProject project = new MamutProjectIO().load( "samples/test_branchgraph.mastodon" );
			final Model model = new Model( project.getSpaceUnits(), project.getTimeUnits() );
			try (final MamutProject.ProjectReader reader = project.openForReading())
			{
				final FileIdToGraphMap< Spot, Link > idmap = model.loadRaw( reader );
				// Load features.
				MamutRawFeatureModelIO.deserialize(
						context,
						model,
						idmap,
						reader );
			}
			catch ( final ClassNotFoundException e )
			{
				e.printStackTrace();
			}

			final String str = ModelUtils.dump( model );
			System.out.println( str ); // DEBUG

			final ModelBranchGraph bg = new ModelBranchGraph( model.getGraph() );
			System.out.println( bg ); // DEBUG

		}
		catch ( final Exception e1 )
		{
			e1.printStackTrace();
		}

	}

}
