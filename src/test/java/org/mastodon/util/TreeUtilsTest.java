package org.mastodon.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.mastodon.collection.RefCollections;
import org.mastodon.collection.RefList;
import org.mastodon.collection.RefSet;
import org.mastodon.mamut.feature.branch.exampleGraph.ExampleGraph2;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;

/**
 * Tests {@link TreeUtils}.
 *
 * @author Matthias Arzt
 */
public class TreeUtilsTest
{
	/** Test {@link TreeUtils#findSelectedSubtreeRoots} on {@link ExampleGraph2}. */
	@Test
	public void testFindSelectedSubtreeRoots()
	{
		// setup
		ExampleGraph2 exampleGraph = new ExampleGraph2();
		ModelGraph graph = exampleGraph.getModel().getGraph();

		RefList< Spot > roots = RefCollections.createRefList( graph.vertices() );
		roots.add( exampleGraph.spot0 );

		RefSet< Spot > selectedSpots = RefCollections.createRefSet( graph.vertices() );
		selectedSpots.add( exampleGraph.spot4 );
		selectedSpots.add( exampleGraph.spot7 );
		selectedSpots.add( exampleGraph.spot10 );

		// process
		RefList< Spot > result = TreeUtils.findSelectedSubtreeRoots( graph, roots, selectedSpots );

		// test
		assertEquals( 1, result.size() );
		assertEquals( exampleGraph.spot4, result.get( 0 ) );
	}

	/** Test {@link TreeUtils#findSelectedSubtreeRoots} on a simple graph with a loop. */
	@Test
	public void testFindSelectedSubTreeRoots_dontGetStuckInLoops()
	{
		// setup
		ModelGraph graph = new ModelGraph();
		Spot a = graph.addVertex().init( 0, new double[] { 0, 0, 0 }, 0 );
		Spot b = graph.addVertex().init( 1, new double[] { 0, 0, 0 }, 0 );
		graph.addEdge( a, b );
		graph.addEdge( b, a ); // loop

		RefList< Spot > roots = RefCollections.createRefList( graph.vertices() );
		roots.add( a );

		RefSet< Spot > selectedSpots = RefCollections.createRefSet( graph.vertices() );
		selectedSpots.add( b );

		// process
		RefList< Spot > result = TreeUtils.findSelectedSubtreeRoots( graph, roots, selectedSpots );

		// test
		assertEquals( 1, result.size() );
		assertEquals( b, result.get( 0 ) );
	}
}
