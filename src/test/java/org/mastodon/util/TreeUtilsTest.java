package org.mastodon.util;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

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
	/**
	 * Test {@link TreeUtils#findSelectedSubtreeRoots} on
	 * {@link ExampleGraph2}.
	 */
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

	/**
	 * Test {@link TreeUtils#findSelectedSubtreeRoots} on a simple graph
	 * with a loop.
	 */
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
		assertEquals( Collections.singletonList( b ), result );
	}

	@Test
	public void testFindRootsOfTheGivenNodes() {
		// Example graph:
		//   a   b
		//   |
		//   a1
		//  / \
		// a2 a3

		ModelGraph graph = new ModelGraph();
		Spot a = graph.addVertex().init( 0, new double[] { 0, 0, 0 }, 1 );
		Spot a1 = graph.addVertex().init( 0, new double[] { 0, 0, 0 }, 1 );
		Spot a2 = graph.addVertex().init( 0, new double[] { 0, 0, 0 }, 1 );
		Spot a3 = graph.addVertex().init( 0, new double[] { 0, 0, 0 }, 1 );
		Spot b = graph.addVertex().init( 0, new double[] { 0, 0, 0 }, 1 );
		graph.addEdge( a, a1 ).init();
		graph.addEdge( a1, a2 ).init();
		graph.addEdge( a2, a3 ).init();

		assertEquals( Collections.singleton( a ), TreeUtils.findRootsOfTheGivenNodes( graph, Arrays.asList( a, a1, a2, a3 ) ) );
		assertEquals( Collections.singleton( a ), TreeUtils.findRootsOfTheGivenNodes( graph, Collections.singleton( a3 ) ) );
		assertEquals( Collections.singleton( b ), TreeUtils.findRootsOfTheGivenNodes( graph, Collections.singleton( b ) ) );
		assertEquals( createSet( a, b ), TreeUtils.findRootsOfTheGivenNodes( graph, Arrays.asList( a2, b ) ) );
	}

	@Test
	public void testFindRootsOfTheGivenNodes_dontGetStuckInLoops() {
		// setup
		ModelGraph graph = new ModelGraph();
		Spot a = graph.addVertex().init( 0, new double[] { 0, 0, 0 }, 1 );
		Spot b = graph.addVertex().init( 1, new double[] { 0, 0, 0 }, 0 );
		Spot c = graph.addVertex().init( 1, new double[] { 0, 0, 0 }, 0 );
		graph.addEdge( a, b );
		graph.addEdge( b, c );
		graph.addEdge( c, b ); // loop between b and c
		// process
		RefSet< Spot > result = TreeUtils.findRootsOfTheGivenNodes( graph, Collections.singleton( c ) );
		// test
		assertEquals( Collections.singleton( a ), result );
	}

	@Test
	public void testFindRootsOfTheGivenNodes_noTree() {
		// Example graph:
		//   a   b
		//    \ /
		//     c
		//     |
		//     d
		ModelGraph graph = new ModelGraph();
		Spot a = graph.addVertex().init( 0, new double[] { 0, 0, 0 }, 1 );
		Spot b = graph.addVertex().init( 1, new double[] { 0, 0, 0 }, 1 );
		Spot c = graph.addVertex().init( 2, new double[] { 0, 0, 0 }, 1 );
		Spot d = graph.addVertex().init( 3, new double[] { 0, 0, 0 }, 1 );
		graph.addEdge( a, c );
		graph.addEdge( b, c );
		graph.addEdge( c, d );

		assertEquals( createSet( a, b ), TreeUtils.findRootsOfTheGivenNodes( graph, Arrays.asList( b, c, d ) ) );
		assertEquals( createSet( a, b ), TreeUtils.findRootsOfTheGivenNodes( graph, Arrays.asList( a, c, d ) ) );
	}

	@SafeVarargs
	private static < T > Set< T > createSet( T... elements )
	{
		return new HashSet<>( Arrays.asList( elements ) );
	}
}
