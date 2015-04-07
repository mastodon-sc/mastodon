package net.trackmate.graph.traversal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import net.trackmate.graph.Graph;
import net.trackmate.graph.TestEdge;
import net.trackmate.graph.TestGraph;
import net.trackmate.graph.TestVertex;

import org.junit.Before;
import org.junit.Test;

public class BreadthFirstIteratorSortedTest
{
	private static final int N_CHILDREN = 10;

	private static final int N_SUB_CHILDREN = 4;

	private TestGraph graph;

	private TestVertex root;

	@Before
	public void setUp() throws Exception
	{
		graph = new TestGraph( N_CHILDREN * N_SUB_CHILDREN + 1 );
		root = graph.addVertex().init( 0 );

		final List< Integer > ids = new ArrayList< Integer >( N_CHILDREN * N_SUB_CHILDREN );
		for ( int i = 0; i < N_CHILDREN + N_CHILDREN * N_SUB_CHILDREN; i++ )
		{
			ids.add( Integer.valueOf( i + 1 ) );
		}
		Collections.shuffle( ids, new Random( 1337l ) );

		final Iterator< Integer > it = ids.iterator();
		for ( int i = 0; i < N_CHILDREN; i++ )
		{
			final TestVertex child = graph.addVertex().init( it.next() );
			graph.addEdge( root, child );

			for ( int j = 0; j < N_SUB_CHILDREN; j++ )
			{
				final TestVertex grandChild = graph.addVertex().init( it.next() );
				graph.addEdge( child, grandChild );
			}
		}
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

		final BreadthFirstIteratorSorted< TestVertex, TestEdge > iter = BreadthFirstIteratorSorted.create( v1, graph, null );
		assertEquals( iter.next().getId(), 1 );
		assertEquals( iter.next().getId(), 2 );
		assertEquals( iter.next().getId(), 3 );
		assertEquals( iter.next().getId(), 4 );
		assertEquals( iter.next().getId(), 5 );
		assertFalse( iter.hasNext() );
	}

	@Test
	public void testBehavior()
	{
		final Comparator< TestVertex > comparator = new Comparator< TestVertex >()
		{
			@Override
			public int compare( final TestVertex o1, final TestVertex o2 )
			{
				return o1.getId() - o2.getId();
			}
		};
		final Iterator< TestVertex > it = BreadthFirstIteratorSorted.create( root, graph, comparator );
		assertTrue( "Iterator should more than 0 element, has not.", it.hasNext() );
		assertTrue( "First element should be the root, is not.", it.next().equals( root ) );

		final TestVertex previousParent = graph.vertexRef();
		previousParent.refTo( root );

		final TestVertex previous = graph.vertexRef();
		previous.refTo( it.next() );
		final TestVertex current = graph.vertexRef();
		while ( it.hasNext() )
		{
			current.refTo( it.next() );
			final TestVertex currentParent = getParent( current );
			if ( currentParent.equals( previousParent ) )
			{
				assertTrue( "Iteration over children of the same descendance should be ordered, but is not.", comparator.compare( current, previous ) > 0 );
			}
			previousParent.refTo( currentParent );
			previous.refTo( current );
		}
	}

	private TestVertex getParent( final TestVertex current )
	{
		final TestVertex parent = graph.vertexRef();
		parent.refTo( current.incomingEdges().get( 0 ).getSource() );
		return parent;
	}

}
