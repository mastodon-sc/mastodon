package net.trackmate.graph.traversal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Iterator;

import net.trackmate.graph.Graph;
import net.trackmate.graph.TestEdge;
import net.trackmate.graph.TestGraph;
import net.trackmate.graph.TestVertex;
import net.trackmate.graph.object.ObjectGraph;
import net.trackmate.graph.object.ObjectVertex;

import org.junit.Before;
import org.junit.Test;

public class BreadthFirstIteratorTest
{
	private TestGraph graph;

	private TestVertex A;

	@Before
	public void setUp() throws Exception
	{
		graph = new TestGraph();

		A = graph.addVertex().init( 0 );
		final TestVertex B = graph.addVertex().init( 1 );
		graph.addEdge( A, B );

		final TestVertex C = graph.addVertex().init( 2 );
		graph.addEdge( A, C );

		final TestVertex D = graph.addVertex().init( 3 );
		graph.addEdge( A, D );

		final TestVertex E = graph.addVertex().init( 4 );
		graph.addEdge( B, E );

		final TestVertex F = graph.addVertex().init( 5 );
		graph.addEdge( B, F );

		final TestVertex G = graph.addVertex().init( 6 );
		graph.addEdge( D, G );

		final TestVertex H = graph.addVertex().init( 7 );
		graph.addEdge( D, H );

		final TestVertex I = graph.addVertex().init( 8 );
		graph.addEdge( E, I );

		final TestVertex J = graph.addVertex().init( 9 );
		graph.addEdge( E, J );

		final TestVertex K = graph.addVertex().init( 10 );
		graph.addEdge( E, K );

		final TestVertex L = graph.addVertex().init( 11 );
		graph.addEdge( H, L );

		final TestVertex M = graph.addVertex().init( 12 );
		graph.addEdge( H, M );

		// Loops
		graph.addEdge( L, F );
		graph.addEdge( K, G );
	}

	@Test
	public void testBehavior()
	{
		final Iterator< TestVertex > iter = GraphIteratorBuilder.createOn( graph ).breadthFirst( A ).unsorted().directed().build();
		assertEquals( 0, iter.next().getId() );
		assertEquals( 1, iter.next().getId() );
		assertEquals( 2, iter.next().getId() );
		assertEquals( 3, iter.next().getId() );
		assertEquals( 4, iter.next().getId() );
		assertEquals( 5, iter.next().getId() );
		assertEquals( 6, iter.next().getId() );
		assertEquals( 7, iter.next().getId() );
		assertEquals( 8, iter.next().getId() );
		assertEquals( 9, iter.next().getId() );
		assertEquals( 10, iter.next().getId() );
		assertEquals( 11, iter.next().getId() );
		assertEquals( 12, iter.next().getId() );
		// The breadth order is not affected by the two loops I put at then end
		// of the tree.
		assertFalse( iter.hasNext() );
	}

	@Test
	public void testBreadthFirstIteratorCycle()
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

		final Iterator< TestVertex > iter = GraphIteratorBuilder.createOn( graph ).breadthFirst( v1 ).unsorted().directed().build();
		assertEquals( iter.next().getId(), 1 );
		assertEquals( iter.next().getId(), 12 );
		assertEquals( iter.next().getId(), 11 );
		assertEquals( iter.next().getId(), 111 );
		assertFalse( iter.hasNext() );
	}

	@Test
	public void testBreadthFirstIteratorBigLoop()
	{
		final Graph< TestVertex, TestEdge > graph = new TestGraph();

		final TestVertex v1 = graph.addVertex().init( 1 );
		final TestVertex v2 = graph.addVertex().init( 2 );
		final TestVertex v3 = graph.addVertex().init( 3 );
		final TestVertex v4 = graph.addVertex().init( 4 );
		final TestVertex v5 = graph.addVertex().init( 5 );
		graph.addEdge( v1, v2 );
		graph.addEdge( v2, v3 );
		graph.addEdge( v3, v4 );
		graph.addEdge( v4, v5 );
		graph.addEdge( v5, v1 );

		final Iterator< TestVertex > iter = GraphIteratorBuilder.createOn( graph ).breadthFirst( v1 ).unsorted().directed().build();
		assertEquals( iter.next().getId(), 1 );
		assertEquals( iter.next().getId(), 2 );
		assertEquals( iter.next().getId(), 3 );
		assertEquals( iter.next().getId(), 4 );
		assertEquals( iter.next().getId(), 5 );
		assertFalse( iter.hasNext() );
	}

	@Test
	public void testBreadthFirstIteratorWithObjectGraph()
	{
		final ObjectGraph< Integer > graph = new ObjectGraph< Integer >();
		final ObjectVertex< Integer > v1 = graph.addVertex().init( 1 );
		final ObjectVertex< Integer > v11 = graph.addVertex().init( 11 );
		final ObjectVertex< Integer > v12 = graph.addVertex().init( 12 );
		final ObjectVertex< Integer > v111 = graph.addVertex().init( 111 );
		final ObjectVertex< Integer > v112 = graph.addVertex().init( 112 );
		final ObjectVertex< Integer > v121 = graph.addVertex().init( 121 );
		final ObjectVertex< Integer > v122 = graph.addVertex().init( 122 );

		graph.addEdge( v12, v122 );
		graph.addEdge( v12, v121 );
		graph.addEdge( v11, v112 );
		graph.addEdge( v11, v111 );
		graph.addEdge( v1, v12 );
		graph.addEdge( v1, v11 );

		final Iterator< ObjectVertex< Integer >> iter = GraphIteratorBuilder.createOn( graph ).breadthFirst( v1 ).unsorted().directed().build();
		assertEquals( iter.next().getContent().intValue(), 1 );
		assertEquals( iter.next().getContent().intValue(), 12 );
		assertEquals( iter.next().getContent().intValue(), 11 );
		assertEquals( iter.next().getContent().intValue(), 122 );
		assertEquals( iter.next().getContent().intValue(), 121 );
		assertEquals( iter.next().getContent().intValue(), 112 );
		assertEquals( iter.next().getContent().intValue(), 111 );
		// BreadthFirstIterator return the linking order by default.
		assertFalse( iter.hasNext() );
	}

	@Test
	public void testBreadthFirstIteratorCycleWithObjectGraph()
	{
		final ObjectGraph< Integer > graph = new ObjectGraph< Integer >();
		final ObjectVertex< Integer > v1 = graph.addVertex().init( 1 );
		final ObjectVertex< Integer > v11 = graph.addVertex().init( 11 );
		final ObjectVertex< Integer > v12 = graph.addVertex().init( 12 );
		final ObjectVertex< Integer > v111 = graph.addVertex().init( 111 );

		graph.addEdge( v11, v111 );
		graph.addEdge( v12, v111 );
		graph.addEdge( v1, v12 );
		graph.addEdge( v1, v11 );

		final Iterator< ObjectVertex< Integer >> iter = GraphIteratorBuilder.createOn( graph ).breadthFirst( v1 ).unsorted().directed().build();
		assertEquals( iter.next().getContent().intValue(), 1 );
		assertEquals( iter.next().getContent().intValue(), 12 );
		assertEquals( iter.next().getContent().intValue(), 11 );
		assertEquals( iter.next().getContent().intValue(), 111 );
		assertFalse( iter.hasNext() );
	}
}
