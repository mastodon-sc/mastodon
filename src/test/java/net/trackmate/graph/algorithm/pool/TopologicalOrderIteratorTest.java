package net.trackmate.graph.algorithm.pool;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import net.trackmate.trackscheme.TrackSchemeGraph;
import net.trackmate.trackscheme.TrackSchemeVertex;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TopologicalOrderIteratorTest
{

	private TrackSchemeGraph graph;

	private TrackSchemeVertex v10;

	private TrackSchemeVertex v7;

	@Before
	public void setUp()
	{
		// From http://en.wikipedia.org/wiki/Topological_sorting
		// X encodes "level"
		graph = new TrackSchemeGraph();
		v7 = graph.addVertex().init( "7", 0, true );
		final TrackSchemeVertex v11 = graph.addVertex().init( "11", 1, true );
		graph.addEdge( v7, v11 );
		final TrackSchemeVertex v5 = graph.addVertex().init( "5", 0, true );
		graph.addEdge( v5, v11 );
		final TrackSchemeVertex v8 = graph.addVertex().init( "8", 1, true );
		graph.addEdge( v7, v8 );
		final TrackSchemeVertex v3 = graph.addVertex().init( "3", 0, true );
		graph.addEdge( v3, v8 );
		final TrackSchemeVertex v2 = graph.addVertex().init( "2", 2, true );
		graph.addEdge( v11, v2 );
		final TrackSchemeVertex v9 = graph.addVertex().init( "9", 2, true );
		graph.addEdge( v11, v9 );
		graph.addEdge( v8, v9 );
		v10 = graph.addVertex().init( "10", 2, true );
		graph.addEdge( v3, v10 );
		graph.addEdge( v11, v10 );
	}

	@After
	public void tearDown()
	{
		graph = null;
	}

	@Test
	public void testBehavior()
	{
		final TopologicalOrderIterator< TrackSchemeVertex > it = new TopologicalOrderIterator< TrackSchemeVertex >( graph.getVertexPool() );
		assertFalse( it.hasFailed() );

		final TrackSchemeVertex previous = graph.vertexRef();
		final TrackSchemeVertex current = graph.vertexRef();
		previous.refTo( it.next() );
		int count = 1;
		while ( it.hasNext() )
		{
			current.refTo( it.next() );
			count++;

			assertTrue( "Vertex " + previous + " should have happend after " + current, current.getLayoutX() >= previous.getLayoutX() );

			previous.refTo( current );
		}
		graph.releaseRef( current );
		graph.releaseRef( previous );

		assertEquals( "Did not iterate through all the vertices of the graph.", graph.numVertices(), count );
	}

	@Test
	public void testNotDAG()
	{
		final TopologicalOrderIterator< TrackSchemeVertex > it = new TopologicalOrderIterator< TrackSchemeVertex >( graph.getVertexPool() );
		assertFalse( it.hasFailed() );

		graph.addEdge( v10, v7 );
		final TopologicalOrderIterator< TrackSchemeVertex > it2 = new TopologicalOrderIterator< TrackSchemeVertex >( graph.getVertexPool() );
		assertTrue( it2.hasFailed() );
	}

}
