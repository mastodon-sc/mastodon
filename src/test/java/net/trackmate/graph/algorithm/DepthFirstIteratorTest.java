package net.trackmate.graph.algorithm;

import net.trackmate.graph.Graph;
import net.trackmate.graph.TestEdge;
import net.trackmate.graph.TestGraph;
import net.trackmate.graph.TestVertex;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class DepthFirstIteratorTest
{
	@Test
	public void testDepthFirstIterator()
	{
		final Graph< TestVertex, TestEdge > graph = new TestGraph();

		final TestVertex v1 = graph.addVertex().init( 1 );
		final TestVertex v11 = graph.addVertex().init( 11 );
		final TestVertex v12 = graph.addVertex().init( 12 );
		final TestVertex v111 = graph.addVertex().init( 111 );
		final TestVertex v112 = graph.addVertex().init( 112 );
		final TestVertex v121 = graph.addVertex().init( 121 );
		final TestVertex v122 = graph.addVertex().init( 122 );
		graph.addEdge( v12, v122 );
		graph.addEdge( v12, v121 );
		graph.addEdge( v11, v112 );
		graph.addEdge( v11, v111 );
		graph.addEdge( v1, v12 );
		graph.addEdge( v1, v11 );

		final DepthFirstIterator< TestVertex, TestEdge > iter = DepthFirstIterator.create( v1, graph );
		assertEquals( iter.next().getId(), 1 );
		assertEquals( iter.next().getId(), 11 );
		assertEquals( iter.next().getId(), 111 );
		assertEquals( iter.next().getId(), 112 );
		assertEquals( iter.next().getId(), 12 );
		assertEquals( iter.next().getId(), 121 );
		assertEquals( iter.next().getId(), 122 );
		assertFalse( iter.hasNext() );
	}

	@Test
	public void testDepthFirstIteratorCycle()
	{
		final Graph< TestVertex, TestEdge > graph = new TestGraph();

		final TestVertex v1 = graph.addVertex().init( 1 );
		final TestVertex v11 = graph.addVertex().init( 11 );
		final TestVertex v12 = graph.addVertex().init( 12 );
		final TestVertex v111 = graph.addVertex().init( 111 );
		graph.addEdge( v11, v111 );
		graph.addEdge( v12, v111 );
		graph.addEdge( v1, v12 );
		graph.addEdge( v1, v11 );

		final DepthFirstIterator< TestVertex, TestEdge > iter = DepthFirstIterator.create( v1, graph );
		assertEquals( iter.next().getId(), 1 );
		assertEquals( iter.next().getId(), 11 );
		assertEquals( iter.next().getId(), 111 );
		assertEquals( iter.next().getId(), 12 );
		assertFalse( iter.hasNext() );
	}
}
