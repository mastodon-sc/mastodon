/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2024 Tobias Pietzsch, Jean-Yves Tinevez
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
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
import org.mastodon.mamut.feature.branch.exampleGraph.ExampleGraph1;
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
		final ExampleGraph2 exampleGraph = new ExampleGraph2();
		final ModelGraph graph = exampleGraph.getModel().getGraph();

		final RefList< Spot > roots = RefCollections.createRefList( graph.vertices() );
		roots.add( exampleGraph.spot0 );

		final RefSet< Spot > selectedSpots = RefCollections.createRefSet( graph.vertices() );
		selectedSpots.add( exampleGraph.spot4 );
		selectedSpots.add( exampleGraph.spot7 );
		selectedSpots.add( exampleGraph.spot10 );

		// process
		final RefList< Spot > result = TreeUtils.findSelectedSubtreeRoots( graph, roots, selectedSpots );

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
		final ModelGraph graph = new ModelGraph();
		final Spot a = graph.addVertex().init( 0, new double[] { 0, 0, 0 }, 0 );
		final Spot b = graph.addVertex().init( 1, new double[] { 0, 0, 0 }, 0 );
		graph.addEdge( a, b );
		graph.addEdge( b, a ); // loop

		final RefList< Spot > roots = RefCollections.createRefList( graph.vertices() );
		roots.add( a );

		final RefSet< Spot > selectedSpots = RefCollections.createRefSet( graph.vertices() );
		selectedSpots.add( b );

		// process
		final RefList< Spot > result = TreeUtils.findSelectedSubtreeRoots( graph, roots, selectedSpots );

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

		final ModelGraph graph = new ModelGraph();
		final Spot a = graph.addVertex().init( 0, new double[] { 0, 0, 0 }, 1 );
		final Spot a1 = graph.addVertex().init( 0, new double[] { 0, 0, 0 }, 1 );
		final Spot a2 = graph.addVertex().init( 0, new double[] { 0, 0, 0 }, 1 );
		final Spot a3 = graph.addVertex().init( 0, new double[] { 0, 0, 0 }, 1 );
		final Spot b = graph.addVertex().init( 0, new double[] { 0, 0, 0 }, 1 );
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
		final ModelGraph graph = new ModelGraph();
		final Spot a = graph.addVertex().init( 0, new double[] { 0, 0, 0 }, 1 );
		final Spot b = graph.addVertex().init( 1, new double[] { 0, 0, 0 }, 0 );
		final Spot c = graph.addVertex().init( 1, new double[] { 0, 0, 0 }, 0 );
		graph.addEdge( a, b );
		graph.addEdge( b, c );
		graph.addEdge( c, b ); // loop between b and c
		// process
		final RefSet< Spot > result = TreeUtils.findRootsOfTheGivenNodes( graph, Collections.singleton( c ) );
		// test
		assertEquals( Collections.singleton( a ), result );
	}

	@Test
	public void testFindRootsOfTheGivenNodes_noTree() {
		// Example graph:
		//   a   b     f
		//    \ / \
		//     c   e
		//     |
		//     d
		final ModelGraph graph = new ModelGraph();
		final Spot a = graph.addVertex().init( 0, new double[] { 0, 0, 0 }, 1 );
		final Spot b = graph.addVertex().init( 1, new double[] { 0, 0, 0 }, 1 );
		final Spot c = graph.addVertex().init( 2, new double[] { 0, 0, 0 }, 1 );
		final Spot d = graph.addVertex().init( 3, new double[] { 0, 0, 0 }, 1 );
		final Spot e = graph.addVertex().init( 4, new double[] { 0, 0, 0 }, 1 );
		graph.addVertex().init( 4, new double[] { 0, 0, 0 }, 1 );
		graph.addEdge( a, c );
		graph.addEdge( b, c );
		graph.addEdge( c, d );
		graph.addEdge( b, e );

		assertEquals( createSet( a, b ), TreeUtils.findRootsOfTheGivenNodes( graph, Arrays.asList( b, c, d ) ) );
		assertEquals( createSet( a, b ), TreeUtils.findRootsOfTheGivenNodes( graph, Arrays.asList( a, c, d ) ) );
		assertEquals( createSet( a, b ), TreeUtils.findRootsOfTheGivenNodes( graph, Collections.singletonList( e ) ) );
	}

	@SafeVarargs
	private static < T > Set< T > createSet( final T... elements )
	{
		return new HashSet<>( Arrays.asList( elements ) );
	}

	@Test
	public void testGetMinTimepoint()
	{
		assertEquals( 0, TreeUtils.getMinTimepoint( new ExampleGraph1().getModel() ) );
		assertEquals( 0, TreeUtils.getMinTimepoint( new ExampleGraph2().getModel() ) );

	}

	@Test
	public void testGetMaxTimepoint()
	{
		assertEquals( 3, TreeUtils.getMaxTimepoint( new ExampleGraph1().getModel() ) );
		assertEquals( 7, TreeUtils.getMaxTimepoint( new ExampleGraph2().getModel() ) );
	}

	@Test
	public void testGetFirstSpot()
	{
		ExampleGraph1 exampleGraph1 = new ExampleGraph1();
		assertEquals( exampleGraph1.spot0, TreeUtils.getFirstSpot( exampleGraph1.getModel(), exampleGraph1.branchSpotA ) );

		ExampleGraph2 exampleGraph2 = new ExampleGraph2();
		assertEquals( exampleGraph2.spot5, TreeUtils.getFirstSpot( exampleGraph2.getModel(), exampleGraph2.branchSpotD ) );
	}
}
