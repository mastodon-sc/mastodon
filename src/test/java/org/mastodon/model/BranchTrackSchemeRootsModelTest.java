package org.mastodon.model;

import static org.junit.Assert.assertEquals;

import java.util.Collections;

import org.junit.Test;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.model.branch.BranchLink;
import org.mastodon.mamut.model.branch.BranchSpot;
import org.mastodon.mamut.model.branch.ModelBranchGraph;
import org.mastodon.views.trackscheme.TrackSchemeGraph;
import org.mastodon.views.trackscheme.TrackSchemeVertex;
import org.mastodon.views.trackscheme.wrap.DefaultModelGraphProperties;
import org.mastodon.views.trackscheme.wrap.ModelGraphProperties;

/**
 * Tests for {@link BranchTrackSchemeRootsModel}.
 */
public class BranchTrackSchemeRootsModelTest
{
	@Test
	public void testUpdateWhenRootIsRemoved()
	{
		// setup
		final Model model = new Model();
		final ModelGraph modelGraph = model.getGraph();
		final Spot spotA = modelGraph.addVertex().init( 0, new double[ 3 ], 1 );
		final ModelBranchGraph branchGraph = model.getBranchGraph();
		branchGraph.graphRebuilt();
		final TrackSchemeGraph< BranchSpot, BranchLink > viewGraph = createTrackSchemeGraph( branchGraph );
		final TrackSchemeVertex vertexA = viewGraph.getRoots().iterator().next();

		// run
		final RootsModel< TrackSchemeVertex > rootsModel = new BranchTrackSchemeRootsModel( modelGraph, branchGraph, viewGraph );
		rootsModel.setRoots( Collections.singletonList( vertexA ) );
		modelGraph.remove( spotA );

		// test
		assertEquals( 0, rootsModel.getRoots().size() );
	}

	private static TrackSchemeGraph< BranchSpot, BranchLink > createTrackSchemeGraph( ModelBranchGraph branchGraph )
	{
		final ModelGraphProperties< BranchSpot, BranchLink > properties =
				new DefaultModelGraphProperties< BranchSpot, BranchLink >()
				{

					@Override
					public String getFirstLabel( final BranchSpot branchSpot )
					{
						return branchSpot.getFirstLabel();
					}

					@Override
					public int getFirstTimePoint( final BranchSpot branchSpot )
					{
						return branchSpot.getFirstTimePoint();
					}
				};
		return new TrackSchemeGraph<>( branchGraph, branchGraph.getGraphIdBimap(), properties );
	}
}
