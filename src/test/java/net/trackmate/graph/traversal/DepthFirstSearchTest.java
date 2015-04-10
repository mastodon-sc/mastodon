package net.trackmate.graph.traversal;

import static net.trackmate.graph.traversal.GraphSearch.EdgeClass.BACK;
import static net.trackmate.graph.traversal.GraphSearch.EdgeClass.CROSS;
import static net.trackmate.graph.traversal.GraphSearch.EdgeClass.TREE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import net.trackmate.graph.TestEdge;
import net.trackmate.graph.TestGraph;
import net.trackmate.graph.TestUtils;
import net.trackmate.graph.TestVertex;
import net.trackmate.graph.traversal.GraphSearch.EdgeClass;

import org.junit.Test;

public class DepthFirstSearchTest
{

	@Test
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

		final List< TestVertex > fromB = Arrays.asList( new TestVertex[] { A, B, C, D, E, F } );
		final List< EdgeClass > fromBEC = Arrays.asList( new EdgeClass[] { TREE, TREE, TREE, TREE, BACK, BACK, TREE } );
		final TraversalTester tt = new TraversalTester( fromB.iterator(), fromBEC.iterator() );
		final DepthFirstSearch< TestVertex, TestEdge > search = new DepthFirstSearch< TestVertex, TestEdge >( loop, true );
		search.start( A, tt );
	}

	@Test
	public void testDirected()
	{
		final TestGraph graph = new TestGraph();
		final TestVertex A = graph.addVertex().init( 1 );
		final TestVertex B = graph.addVertex().init( 2 );
		final TestVertex C = graph.addVertex().init( 3 );
		final TestVertex E = graph.addVertex().init( 5 );
		final TestVertex D = graph.addVertex().init( 4 );
		final TestVertex F = graph.addVertex().init( 6 );
		final TestVertex G = graph.addVertex().init( 7 );

		graph.addEdge( B, D );
		graph.addEdge( B, F );
		graph.addEdge( A, B );
		graph.addEdge( A, C );
		graph.addEdge( A, E );
		graph.addEdge( C, G );
		graph.addEdge( E, F );
		final List< TestVertex > fromB = Arrays.asList( new TestVertex[] { B, D, F } );
		final List< EdgeClass > fromBEC = Arrays.asList( new EdgeClass[] { TREE, TREE } );
		final TraversalTester tt = new TraversalTester( fromB.iterator(), fromBEC.iterator() );

		final DepthFirstSearch< TestVertex, TestEdge > search = new DepthFirstSearch< TestVertex, TestEdge >( graph, true );
		search.start( B, tt );

		final List< TestVertex > fromA = Arrays.asList( new TestVertex[] { A, B, D, F, C, G, E } );
		final List< EdgeClass > fromAEC = Arrays.asList( new EdgeClass[] { TREE, TREE, TREE, TREE, TREE, TREE, CROSS } );
		tt.setExpectedVertexIterator( fromA.iterator() );
		tt.setExpectedEdgeClassIterator( fromAEC.iterator() );
		search.start( A, tt );
	}

	@Test
	public void testSorted()
	{
		final Comparator< TestVertex > comparator = TestUtils.idComparator;
		final int N_CHILDREN = 30;
		final TestGraph graph = new TestGraph( N_CHILDREN + 1 );
		final TestVertex root = graph.addVertex().init( 0 );

		final List< Integer > ids = new ArrayList< Integer >( N_CHILDREN );
		for ( int i = 0; i < N_CHILDREN; i++ )
		{
			ids.add( Integer.valueOf( i + 1 ) );
		}
		Collections.shuffle( ids, new Random( 13371l ) );

		for ( final Integer id : ids )
		{
			final TestVertex child = graph.addVertex().init( id );
			graph.addEdge( root, child );
		}

		final TestVertex previous = graph.vertexRef();
		final TestVertex edgeCheck = graph.vertexRef();
		previous.refTo( root );
		final SearchListener< TestVertex, TestEdge > l = new SearchListener< TestVertex, TestEdge >()
		{

			@Override
			public void processVertexLate( final TestVertex vertex, final int time, final GraphSearch< TestVertex, TestEdge > search )
			{}

			@Override
			public void processEdge( final TestEdge edge, final TestVertex from, final TestVertex to, final int time, final GraphSearch< TestVertex, TestEdge > search )
			{
				// Check that the edge are processed also in order
				edgeCheck.refTo( edge.getTarget( edgeCheck ) );
			}

			@Override
			public void processVertexEarly( final TestVertex vertex, final int time, final GraphSearch< TestVertex, TestEdge > search )
			{
				assertEquals( "Given the edge that was previously processed, I received the wrong vertex.", edgeCheck, vertex );

				if ( !vertex.equals( root ) )
				{
					assertTrue( "Iteration over children of the same descendance should be ordered, but is not. Got " + vertex + " after " + previous, comparator.compare( vertex, previous ) > 0 );
				}
				previous.refTo( vertex );
			}
		};

		final DepthFirstSearch< TestVertex, TestEdge > dfs = new DepthFirstSearch< TestVertex, TestEdge >( graph, true );
		dfs.setComparator( comparator );
		dfs.start( root, l );

		graph.releaseRef( previous );
		graph.releaseRef( edgeCheck );
	}

	private class TraversalTester implements SearchListener< TestVertex, TestEdge >
	{

		private Iterator< TestVertex > expectedVertexIterator;

		private Iterator< EdgeClass > expectedEdgeClassIterator;

		public TraversalTester( final Iterator< TestVertex > expectedVertexIterator, final Iterator< EdgeClass > expectedEdgeClassIterator )
		{
			this.expectedVertexIterator = expectedVertexIterator;
			this.expectedEdgeClassIterator = expectedEdgeClassIterator;
		}

		public void setExpectedVertexIterator( final Iterator< TestVertex > expectedVertexIterator )
		{
			this.expectedVertexIterator = expectedVertexIterator;
		}

		public void setExpectedEdgeClassIterator( final Iterator< EdgeClass > expectedEdgeClassIterator )
		{
			this.expectedEdgeClassIterator = expectedEdgeClassIterator;
		}

		@Override
		public void processVertexLate( final TestVertex vertex, final int time, final GraphSearch< TestVertex, TestEdge > search )
		{}

		@Override
		public void processVertexEarly( final TestVertex vertex, final int time, final GraphSearch< TestVertex, TestEdge > search )
		{
			assertEquals( "Did not meet the expected vertex sequence during search.", expectedVertexIterator.next(), vertex );
		}

		@Override
		public void processEdge( final TestEdge edge, final TestVertex from, final TestVertex to, final int time, final GraphSearch< TestVertex, TestEdge > search )
		{
			final EdgeClass eclass = search.edgeClass( from, to );
			assertEquals( "The edge " + edge + " traversed  from " + from + " to " + to + " has an unexpcted class in the search.", expectedEdgeClassIterator.next(), eclass );
		}
	}

}
