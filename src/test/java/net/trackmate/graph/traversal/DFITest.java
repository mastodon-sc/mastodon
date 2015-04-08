package net.trackmate.graph.traversal;

import net.trackmate.graph.TestEdge;
import net.trackmate.graph.TestGraph;
import net.trackmate.graph.TestVertex;

import org.junit.Before;
import org.junit.Test;

public class DFITest
{
	private TestGraph graph;

	private TestVertex A;

	private TestVertex B;

	@Before
	public void setUp()
	{
		graph = new TestGraph();

		A = graph.addVertex().init( 1 );

		B = graph.addVertex().init( 2 );
		graph.addEdge( A, B );

		final TestVertex C = graph.addVertex().init( 3 );
		graph.addEdge( A, C );

		final TestVertex E = graph.addVertex().init( 5 );
		graph.addEdge( A, E );

		final TestVertex D = graph.addVertex().init( 4 );
		graph.addEdge( B, D );

		final TestVertex F = graph.addVertex().init( 6 );
		graph.addEdge( B, F );

		final TestVertex G = graph.addVertex().init( 7 );
		graph.addEdge( C, G );

		graph.addEdge( E, F );
	}

	@Test
	public void test()
	{
		final DFI< TestVertex, TestEdge > dfi = new DFI< TestVertex, TestEdge >( B, graph, false );
		while ( dfi.hasNext() )
		{
			System.out.println( "Iterating over " + dfi.next() );// DEBUG
		}
	}

}
