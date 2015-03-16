package net.trackmate.graph.algorithm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import net.trackmate.graph.TestEdge;
import net.trackmate.graph.TestGraph;
import net.trackmate.graph.TestVertex;

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
		final BreadthFirstIterator< TestVertex, TestEdge > iter = BreadthFirstIterator.create( A, graph );
		assertEquals( iter.next().getId(), 0 );
		assertEquals( iter.next().getId(), 1 );
		assertEquals( iter.next().getId(), 2 );
		assertEquals( iter.next().getId(), 3 );
		assertEquals( iter.next().getId(), 4 );
		assertEquals( iter.next().getId(), 5 );
		assertEquals( iter.next().getId(), 6 );
		assertEquals( iter.next().getId(), 7 );
		assertEquals( iter.next().getId(), 8 );
		assertEquals( iter.next().getId(), 9 );
		assertEquals( iter.next().getId(), 10 );
		assertEquals( iter.next().getId(), 11 );
		assertEquals( iter.next().getId(), 12 );
		// The breadth order is not affected by the two loops I put at then end
		// of the tree.
		assertFalse( iter.hasNext() );
	}

	public static void main( final String[] args ) throws Exception
	{
		final BreadthFirstIteratorTest test = new BreadthFirstIteratorTest();
		test.setUp();

		System.out.println( "Breadth first:" );
		final BreadthFirstIterator< TestVertex, TestEdge > i1 = new BreadthFirstIterator< TestVertex, TestEdge >( test.A, test.graph );
		while ( i1.hasNext() )
		{
			System.out.println( i1.next().toString() );
		}
		System.out.println();

		System.out.println( "Depth first:" );
		final DepthFirstIterator< TestVertex, TestEdge > i2 = new DepthFirstIterator< TestVertex, TestEdge >( test.A, test.graph );
		while ( i2.hasNext() )
		{
			System.out.println( i2.next().toString() );
		}
		System.out.println();
	}
}
