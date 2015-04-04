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

public class DepthFirstIteratorSortedTest
{

	private static final int N_CHILDREN = 30;

	private TestGraph graph;

	private TestVertex root;

	@Before
	public void setUp() throws Exception
	{
		graph = new TestGraph( N_CHILDREN + 1 );
		root = graph.addVertex().init( 0 );

		final List<Integer> ids = new ArrayList< Integer >(N_CHILDREN);
		for ( int i = 0; i < N_CHILDREN; i++ )
		{
			ids.add( Integer.valueOf( i + 1 ) );
		}
		Collections.shuffle( ids, new Random( 1337l ) );

		for ( final Integer id : ids )
		{
			final TestVertex child = graph.addVertex().init( id );
			graph.addEdge( root, child );
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

		final DepthFirstIteratorSorted< TestVertex, TestEdge > iter = DepthFirstIteratorSorted.create( v1, graph, null );
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
		// Will sort the tree in ASCENDING order
		final Comparator< TestVertex > comparator = new Comparator< TestVertex >()
		{
			@Override
			public int compare( final TestVertex o1, final TestVertex o2 )
			{
				return -o1.getId() + o2.getId();
			}
		};
		final Iterator< TestVertex > it = DepthFirstIteratorSorted.create( root, graph, comparator );
		assertTrue( "Iterator should more than 0 element, has not.", it.hasNext() );
		assertTrue( "First element should be the root, is not.", it.next().equals( root ) );

		final TestVertex previous = graph.vertexRef();
		previous.refTo( it.next() );
		final TestVertex current = graph.vertexRef();
		while ( it.hasNext() )
		{
			current.refTo( it.next() );
			// Careful: DFIsorted returns opposite order.
			assertTrue( "Iteration over children of the same descendance should be ordered, but is not.", comparator.compare( current, previous ) < 0 );
			previous.refTo( current );
		}
	}
}