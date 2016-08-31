package org.mastodon.graph.algorithm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mastodon.graph.TestEdge;
import org.mastodon.graph.TestVertex;
import org.mastodon.graph.algorithm.ShortestPath;
import org.mastodon.graph.algorithm.traversal.GraphSearch.SearchDirection;
import org.mastodon.graph.traversal.GraphsForTests;
import org.mastodon.graph.traversal.GraphsForTests.GraphTestBundle;

public class ShortestPathTest
{

	private GraphTestBundle< TestVertex, TestEdge > bundle;

	@Before
	public void setUp() throws Exception
	{
		bundle = GraphsForTests.wpExamplePoolObjects();
	}

	@Test
	public void testUndirected()
	{
		final ShortestPath< TestVertex, TestEdge > sp = new ShortestPath<>( bundle.graph, SearchDirection.UNDIRECTED );
		final TestVertex D = bundle.vertices.get( 3 );
		final TestVertex G = bundle.vertices.get( 6 );

		final Iterator< TestVertex > path = sp.findPath( D, G ).iterator();
		assertNotNull( "Path not found, though it exists.", path );

		final List< TestVertex > expectedOrder = new ArrayList<>( 5 );
		// Reverse order
		expectedOrder.add( bundle.vertices.get( 6 ) );
		expectedOrder.add( bundle.vertices.get( 2 ) );
		expectedOrder.add( bundle.vertices.get( 0 ) );
		expectedOrder.add( bundle.vertices.get( 1 ) );
		expectedOrder.add( bundle.vertices.get( 3 ) );
		final Iterator< TestVertex > eit = expectedOrder.iterator();
		while ( eit.hasNext() )
		{
			assertEquals( "Path found does not follow expected order.", eit.next(), path.next() );
		}
		assertFalse( "Path is longer than expected.", path.hasNext() );
	}

	@Test
	public void testDirected()
	{
		final TestVertex A = bundle.vertices.get( 0 );
		final TestVertex E = bundle.vertices.get( 4 );
		// Change the direction of eAE
		bundle.graph.remove( bundle.edges.get( 2 ) );
		bundle.graph.addEdge( E, A );

		final ShortestPath< TestVertex, TestEdge > spUndirected = new ShortestPath<>( bundle.graph, SearchDirection.UNDIRECTED );
		final Iterator< TestVertex > pathUndirected = spUndirected.findPath( A, E ).iterator();
		assertNotNull( "Path not found, though it exists.", pathUndirected );

		// Reverse order
		final List< TestVertex > expectedOrderUD = new ArrayList<>( 2 );
		expectedOrderUD.add( E );
		expectedOrderUD.add( A );
		final Iterator< TestVertex > eitUD = expectedOrderUD.iterator();
		while ( eitUD.hasNext() )
		{
			assertEquals( "Path found does not follow expected order.", eitUD.next(), pathUndirected.next() );
		}
		assertFalse( "Path is longer than expected.", pathUndirected.hasNext() );

		// Redo it, as directed search.
		final ShortestPath< TestVertex, TestEdge > spDirected = new ShortestPath<>( bundle.graph, SearchDirection.DIRECTED );
		final Iterator< TestVertex > pathDirected = spDirected.findPath( A, E ).iterator();
		assertNotNull( "Path not found, though it exists.", pathDirected );

		final List< TestVertex > expectedOrderD = new ArrayList<>( 5 );
		// Reverse order
		expectedOrderD.add( E );
		expectedOrderD.add( bundle.vertices.get( 5 ) );
		expectedOrderD.add( bundle.vertices.get( 1 ) );
		expectedOrderD.add( A );
		final Iterator< TestVertex > eitD = expectedOrderD.iterator();
		while ( eitD.hasNext() )
		{
			final TestVertex v = pathDirected.next();
			assertEquals( "Path found does not follow expected order.", eitD.next(), v );
		}
		assertFalse( "Path is longer than expected.", pathDirected.hasNext() );
	}

	@Test
	public void testNonExistingPath()
	{
		// TODO
	}

}
