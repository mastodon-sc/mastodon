package net.trackmate.graph.traversal;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import net.trackmate.graph.TestEdge;
import net.trackmate.graph.TestGraph;
import net.trackmate.graph.TestVertex;

import org.junit.Before;
import org.junit.Test;
public class DepthFirstSearchTest
{
	private TestGraph graph;

	private TestVertex A;

	private TestVertex B;

	private TestVertex C;

	private TestVertex E;

	private TestVertex D;

	private TestVertex F;

	private TestVertex G;

	@Before
	public void setUp()
	{
		graph = new TestGraph();

		A = graph.addVertex().init( 1 );
		B = graph.addVertex().init( 2 );
		C = graph.addVertex().init( 3 );
		E = graph.addVertex().init( 5 );
		D = graph.addVertex().init( 4 );
		F = graph.addVertex().init( 6 );
		G = graph.addVertex().init( 7 );

		graph.addEdge( B, D );
		graph.addEdge( B, F );
		graph.addEdge( A, B );
		graph.addEdge( A, C );
		graph.addEdge( A, E );
		graph.addEdge( C, G );
		graph.addEdge( E, F );
	}

	public void testNonTree()
	{
		/*
		 * Skiena DFS in a loop.
		 */
		final TestGraph loop = new TestGraph();
		final TestVertex F = loop.addVertex().init( 6 );
		final TestVertex A = loop.addVertex().init( 1 );
		final TestVertex B = loop.addVertex().init( 2 );
		final TestVertex C = loop.addVertex().init( 3 );
		final TestVertex E = loop.addVertex().init( 5 );
		final TestVertex D = loop.addVertex().init( 4 );

		loop.addEdge( A, B );
		loop.addEdge( E, A );
		loop.addEdge( B, C );
		loop.addEdge( C, D );
		loop.addEdge( D, E );
		loop.addEdge( E, B );
		loop.addEdge( A, F );

		final SearchListener< TestVertex, TestEdge > tt = new SearchListener< TestVertex, TestEdge >()
		{

			@Override
			public void processVertexLate( final TestVertex vertex, final int time, final GraphSearch< TestVertex, TestEdge > search )
			{
				System.out.println( " - finished processing vertex " + vertex );// DEBUG
			}

			@Override
			public void processVertexEarly( final TestVertex vertex, final int time, final GraphSearch< TestVertex, TestEdge > search )
			{
				System.out.println( " - discovered vertex " + vertex );// DEBUG
			}

			@Override
			public void processEdge( final TestEdge edge, final TestVertex from, final TestVertex to, final int time, final GraphSearch< TestVertex, TestEdge > search )
			{
				System.out.println( " - processing edge from " + from + " to " + to + ", class = " + search.edgeClass( from, to ) );// DEBUG
			}
		};
		final DepthFirstSearch< TestVertex, TestEdge > search = new DepthFirstSearch< TestVertex, TestEdge >( loop, true );
		search.start( A, tt );

	}

	@Test
	public void testDirected()
	{
		final List< TestVertex > fromB = Arrays.asList( new TestVertex[] { B, D, F } );
		final TraversalTester tt = new TraversalTester( fromB.iterator() );

		final DepthFirstSearch< TestVertex, TestEdge > search = new DepthFirstSearch< TestVertex, TestEdge >( graph, true );
		search.start( B, tt );
		System.out.println();// DEBUG

		final List< TestVertex > fromA = Arrays.asList( new TestVertex[] { A, B, D, F, C, G, E } );
		tt.setExpectedVertexIterator( fromA.iterator() );
		search.start( A, tt );
	}
	
	private class TraversalTester implements SearchListener< TestVertex, TestEdge >
	{

		private Iterator< TestVertex > expectedVertexIterator;

		public TraversalTester( final Iterator< TestVertex > expectedVertexIterator )
		{
			this.expectedVertexIterator = expectedVertexIterator;
		}

		public void setExpectedVertexIterator( final Iterator< TestVertex > expectedVertexIterator )
		{
			this.expectedVertexIterator = expectedVertexIterator;
		}

		@Override
		public void processVertexLate( final TestVertex vertex, final int time, final GraphSearch< TestVertex, TestEdge > search )
		{
			System.out.println( " - finished processing vertex " + vertex );// DEBUG
		}

		@Override
		public void processVertexEarly( final TestVertex vertex, final int time, final GraphSearch< TestVertex, TestEdge > search )
		{
			System.out.println( " - discovered vertex " + vertex );// DEBUG
			assertEquals( "Did not meet the expected vertex sequence during search.", expectedVertexIterator.next(), vertex );
		}

		@Override
		public void processEdge( final TestEdge edge, final TestVertex from, final TestVertex to, final int time, final GraphSearch< TestVertex, TestEdge > search )
		{
			System.out.println( " - processing edge from " + from + " to " + to + ", class = " + search.edgeClass( from, to ) );// DEBUG
		}

	}

}
