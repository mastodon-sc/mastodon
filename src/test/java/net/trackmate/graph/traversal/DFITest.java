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

		final TraversalListener< TestVertex, TestEdge > tl = new TraversalListener< TestVertex, TestEdge >()
		{

			@Override
			public void processVertexLate( final TestVertex vertex, final int time )
			{
				System.out.println( "t = " + time + " - finished processing vertex " + vertex );// DEBUG
			}

			@Override
			public void processVertexEarly( final TestVertex vertex, final int time )
			{
				System.out.println( "t = " + time + " - discovered vertex " + vertex );// DEBUG

			}

			@Override
			public void processEdge( final TestEdge edge, final TestVertex from, final TestVertex to, final int time )
			{
				System.out.println( "t = " + time + " - crossing edge " + edge + " from vertex " + from + " to " + to );// DEBUG
			}

		};

		final DepthFirstSearch< TestVertex, TestEdge > search = new DepthFirstSearch< TestVertex, TestEdge >( graph, B, tl, false );
		search.restart( A );
		System.out.println();// DEBUG
		search.restart( B );
	}

}
