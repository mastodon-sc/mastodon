package net.trackmate.graph.algorithm;

import static org.junit.Assert.assertTrue;
import net.trackmate.trackscheme.TrackSchemeEdge;
import net.trackmate.trackscheme.TrackSchemeGraph;
import net.trackmate.trackscheme.TrackSchemeVertex;

import org.junit.Before;
import org.junit.Test;

public class DFITest
{
	private TrackSchemeGraph graph;

	private TrackSchemeVertex A;

	private TrackSchemeVertex B;

	@Before
	public void setUp()
	{
		graph = new TrackSchemeGraph();

		A = graph.addVertex().init( "A", 0, true );

		B = graph.addVertex().init( "B", 1, true );
		graph.addEdge( A, B );

		final TrackSchemeVertex C = graph.addVertex().init( "C", 1, true );
		graph.addEdge( A, C );

		final TrackSchemeVertex E = graph.addVertex().init( "E", 1, true );
		graph.addEdge( A, E );

		final TrackSchemeVertex D = graph.addVertex().init( "D", 2, true );
		graph.addEdge( B, D );

		final TrackSchemeVertex F = graph.addVertex().init( "F", 2, true );
		graph.addEdge( B, F );

		final TrackSchemeVertex G = graph.addVertex().init( "G", 2, true );
		graph.addEdge( C, G );

		graph.addEdge( E, F );
	}

	@Test
	public void testBehavior()
	{
		final DepthFirstIterator< TrackSchemeVertex, TrackSchemeEdge > dfi = new DepthFirstIterator< TrackSchemeVertex, TrackSchemeEdge >( A, graph );

		assertTrue( "After initiation, DepthFirstIterator always has next. Got false.", dfi.hasNext() );

		final TrackSchemeVertex first = dfi.next();
		assertTrue( "The first vertex iterated must be the root. got " + first + ".", A.equals( first ) );

		TrackSchemeVertex previous = first;
		while ( dfi.hasNext() )
		{
			System.out.println( previous + "->" );// DEBUG INTERESTING TEST
													// FAILURE
			/*
			 * The ref pointed to by previous is the root, and is modified by
			 * the dfi. Aarrrrgh. Have to think about this.
			 */
			final TrackSchemeVertex vertex = dfi.next();
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

	private boolean isSuccessor( final TrackSchemeVertex parent, final TrackSchemeVertex candidate )
	{
		final TrackSchemeVertex v = graph.vertexRef();
		for ( final TrackSchemeEdge edge : parent.outgoingEdges() )
		{
			if ( edge.getTarget( v ).equals( candidate ) ) { return true; }
		}

		return false;
	}

	public static void main( final String[] args )
	{

		final DFITest test = new DFITest();
		test.setUp();

		final DepthFirstIterator< TrackSchemeVertex, TrackSchemeEdge > iterator = new DepthFirstIterator< TrackSchemeVertex, TrackSchemeEdge >( test.A, test.graph );
		while ( iterator.hasNext() )
		{
			System.out.println( iterator.next().toString() );
		}
	}

}
