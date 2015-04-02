package net.trackmate.graph.algorithm;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

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