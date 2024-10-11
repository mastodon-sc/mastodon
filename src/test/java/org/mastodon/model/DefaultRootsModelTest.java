package org.mastodon.model;

import static org.junit.Assert.assertEquals;

import java.util.Collections;

import org.junit.Test;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.ModelGraphTrackSchemeProperties;
import org.mastodon.mamut.model.Spot;
import org.mastodon.views.trackscheme.TrackSchemeGraph;
import org.mastodon.views.trackscheme.TrackSchemeVertex;

/**
 * Tests for {@link DefaultRootsModel}.
 */
public class DefaultRootsModelTest
{
	@Test
	public void testUpdateWhenRootIsRemoved()
	{
		// setup
		final ModelGraph graph = new ModelGraph();
		final Spot spotA = graph.addVertex().init( 0, new double[ 3 ], 1 );
		final TrackSchemeGraph< Spot, Link > viewGraph = new TrackSchemeGraph<>( graph, graph.getGraphIdBimap(), new ModelGraphTrackSchemeProperties( graph ) );
		final TrackSchemeVertex vertexA = viewGraph.getRoots().iterator().next();

		// run
		final RootsModel< TrackSchemeVertex > rootsModel = new DefaultRootsModel<>( graph, viewGraph );
		rootsModel.setRoots( Collections.singletonList( vertexA ) );
		graph.remove( spotA );

		// test
		assertEquals( 0, rootsModel.getRoots().size() );
	}
}
