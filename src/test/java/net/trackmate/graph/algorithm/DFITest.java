package net.trackmate.graph.algorithm;

import static org.junit.Assert.assertTrue;
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
	public void testBehavior()
	{
		final DepthFirstIterator< TestVertex, TestEdge > dfi = new DepthFirstIterator< TestVertex, TestEdge >( A, graph );

		assertTrue( "After initiation, DepthFirstIterator always has next. Got false.", dfi.hasNext() );

		final TestVertex first = dfi.next();
		assertTrue( "The first vertex iterated must be the root. got " + first + ".", A.equals( first ) );

		TestVertex previous = first;
		while ( dfi.hasNext() )
		{
			System.out.println( previous + "->" );// DEBUG INTERESTING TEST
													// FAILURE
			/*
			 * The ref pointed to by previous is the root, and is modified by
			 * the dfi. Aarrrrgh. Have to think about this.
			 */
			final TestVertex vertex = dfi.next();
			System.out.println( previous + "->" + vertex );// DEBUG

			if ( !previous.outgoingEdges().isEmpty() )
			{
				assertTrue( "When iterating from a previous vertex that has successors, "
						+ "the current vertex must be a successor of the previous vertex. "
						+ "This was not the case for " + previous + " -> " + vertex + ".",
						isSuccessor( previous, vertex ) );
			}

			previous = vertex;
		}
	}

	private boolean isSuccessor( final TestVertex parent, final TestVertex candidate )
	{
		final TestVertex v = graph.vertexRef();
		for ( final TestEdge edge : parent.outgoingEdges() )
		{
			if ( edge.getTarget( v ).equals( candidate ) ) { return true; }
		}

		return false;
	}

	public static void main( final String[] args )
	{

		final DFITest test = new DFITest();
		test.setUp();

		final DepthFirstIterator< TestVertex, TestEdge > iterator = new DepthFirstIterator< TestVertex, TestEdge >( test.A, test.graph );
		while ( iterator.hasNext() )
		{
			System.out.println( iterator.next().toString() );
		}
	}

}
