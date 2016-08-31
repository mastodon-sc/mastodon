package org.mastodon.graph.traversal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;

import org.junit.Test;
import org.mastodon.collection.RefList;
import org.mastodon.collection.util.CollectionUtils;
import org.mastodon.graph.TestEdge;
import org.mastodon.graph.TestVertex;
import org.mastodon.graph.algorithm.traversal.InverseBreadthFirstIterator;
import org.mastodon.graph.object.ObjectEdge;
import org.mastodon.graph.object.ObjectVertex;
import org.mastodon.graph.traversal.GraphsForTests.GraphTestBundle;

/**
 *
 * @author Jean=Yves Tinevez &ltjeanyves.tinevez@gmail.com&gt
 */
public class InverseBreadthFirstIteratorTest
{

	@Test
	public void testStraightLinePoolObjects()
	{
		final GraphTestBundle< TestVertex, TestEdge > bundle = GraphsForTests.straightLinePoolObjects();

		final TestVertex first = bundle.vertices.get( 5 );
		final InverseBreadthFirstIterator< TestVertex, TestEdge > it = new InverseBreadthFirstIterator<>( first, bundle.graph );

		final RefList< TestVertex > expected = CollectionUtils.createRefList( bundle.graph.vertices() );
		expected.add( bundle.vertices.get( 5 ) );
		expected.add( bundle.vertices.get( 4 ) );
		expected.add( bundle.vertices.get( 3 ) );
		expected.add( bundle.vertices.get( 2 ) );
		expected.add( bundle.vertices.get( 1 ) );
		expected.add( bundle.vertices.get( 0 ) );
		final Iterator< TestVertex > eit = expected.iterator();

		while ( eit.hasNext() )
		{
			assertTrue( "Iterator should not be finished, but is.", it.hasNext() );
			assertEquals( "Unexpected vertex met during iteration.", eit.next(), it.next() );
		}

		assertFalse( "Iteration should be finished, but is not.", it.hasNext() );
	}

	@Test
	public void testStraightLineStdObjects()
	{
		final GraphTestBundle< ObjectVertex< Integer >, ObjectEdge< Integer >> bundle = GraphsForTests.straightLineStdObjects();

		final ObjectVertex< Integer > first = bundle.vertices.get( 5 );
		final InverseBreadthFirstIterator< ObjectVertex< Integer >, ObjectEdge< Integer >> it = new InverseBreadthFirstIterator<>( first, bundle.graph );

		final RefList< ObjectVertex< Integer >> expected = CollectionUtils.createRefList( bundle.graph.vertices() );
		expected.add( bundle.vertices.get( 5 ) );
		expected.add( bundle.vertices.get( 4 ) );
		expected.add( bundle.vertices.get( 3 ) );
		expected.add( bundle.vertices.get( 2 ) );
		expected.add( bundle.vertices.get( 1 ) );
		expected.add( bundle.vertices.get( 0 ) );
		final Iterator< ObjectVertex< Integer >> eit = expected.iterator();

		while ( eit.hasNext() )
		{
			assertTrue( "Iterator should not be finished, but is.", it.hasNext() );
			assertEquals( "Unexpected vertex met during iteration.", eit.next(), it.next() );
		}

		assertFalse( "Iteration should be finished, but is not.", it.hasNext() );
	}

	@Test
	public void testForkStdObjects()
	{
		final GraphTestBundle< ObjectVertex< Integer >, ObjectEdge< Integer >> bundle = GraphsForTests.forkStdObjects();

		final ObjectVertex< Integer > first = bundle.vertices.get( 1 );
		final InverseBreadthFirstIterator< ObjectVertex< Integer >, ObjectEdge< Integer >> it = new InverseBreadthFirstIterator<>( first, bundle.graph );

		final RefList< ObjectVertex< Integer >> expected = CollectionUtils.createRefList( bundle.graph.vertices() );
		expected.add( bundle.vertices.get( 1 ) );
		expected.add( bundle.vertices.get( 0 ) );
		final Iterator< ObjectVertex< Integer >> eit = expected.iterator();

		while ( eit.hasNext() )
		{
			assertTrue( "Iterator should not be finished, but is.", it.hasNext() );
			assertEquals( "Unexpected vertex met during iteration.", eit.next(), it.next() );
		}

		assertFalse( "Iteration should be finished, but is not.", it.hasNext() );
	}

	@Test
	public void testForkPoolObjects()
	{
		final GraphTestBundle< TestVertex, TestEdge > bundle = GraphsForTests.forkPoolObjects();

		final TestVertex first = bundle.vertices.get( 1 );
		final InverseBreadthFirstIterator< TestVertex, TestEdge > it = new InverseBreadthFirstIterator<>( first, bundle.graph );

		final RefList< TestVertex > expected = CollectionUtils.createRefList( bundle.graph.vertices() );
		expected.add( bundle.vertices.get( 1 ) );
		expected.add( bundle.vertices.get( 0 ) );
		final Iterator< TestVertex > eit = expected.iterator();

		while ( eit.hasNext() )
		{
			assertTrue( "Iterator should not be finished, but is.", it.hasNext() );
			assertEquals( "Unexpected vertex met during iteration.", eit.next(), it.next() );
		}

		assertFalse( "Iteration should be finished, but is not.", it.hasNext() );
	}

	@Test
	public void testDiamondPoolObjects()
	{
		final GraphTestBundle< TestVertex, TestEdge > bundle = GraphsForTests.diamondPoolObjects();

		final TestVertex first = bundle.vertices.get( 3 );
		final InverseBreadthFirstIterator< TestVertex, TestEdge > it = new InverseBreadthFirstIterator<>( first, bundle.graph );

		final RefList< TestVertex > expected = CollectionUtils.createRefList( bundle.graph.vertices() );
		expected.add( bundle.vertices.get( 3 ) );
		expected.add( bundle.vertices.get( 1 ) );
		expected.add( bundle.vertices.get( 2 ) );
		expected.add( bundle.vertices.get( 0 ) );
		final Iterator< TestVertex > eit = expected.iterator();

		while ( eit.hasNext() )
		{
			assertTrue( "Iterator should not be finished, but is.", it.hasNext() );
			assertEquals( "Unexpected vertex met during iteration.", eit.next(), it.next() );
		}

		assertFalse( "Iteration should be finished, but is not.", it.hasNext() );
	}

	@Test
	public void testDiamondStdObjects()
	{
		final GraphTestBundle< ObjectVertex< Integer >, ObjectEdge< Integer >> bundle = GraphsForTests.diamondStdObjects();

		final ObjectVertex< Integer > first = bundle.vertices.get( 3 );
		final InverseBreadthFirstIterator< ObjectVertex< Integer >, ObjectEdge< Integer >> it = new InverseBreadthFirstIterator<>( first, bundle.graph );

		final RefList< ObjectVertex< Integer >> expected = CollectionUtils.createRefList( bundle.graph.vertices() );
		expected.add( bundle.vertices.get( 3 ) );
		expected.add( bundle.vertices.get( 1 ) );
		expected.add( bundle.vertices.get( 2 ) );
		expected.add( bundle.vertices.get( 0 ) );
		final Iterator< ObjectVertex< Integer >> eit = expected.iterator();

		while ( eit.hasNext() )
		{
			assertTrue( "Iterator should not be finished, but is.", it.hasNext() );
			assertEquals( "Unexpected vertex met during iteration.", eit.next(), it.next() );
		}

		assertFalse( "Iteration should be finished, but is not.", it.hasNext() );
	}

	@Test
	public void testLoopStdObjects()
	{
		final GraphTestBundle< ObjectVertex< Integer >, ObjectEdge< Integer >> bundle = GraphsForTests.loopStdObjects();

		final ObjectVertex< Integer > first = bundle.vertices.get( 0 );
		final InverseBreadthFirstIterator< ObjectVertex< Integer >, ObjectEdge< Integer >> it = new InverseBreadthFirstIterator<>( first, bundle.graph );

		final RefList< ObjectVertex< Integer >> expected = CollectionUtils.createRefList( bundle.graph.vertices() );
		expected.add( bundle.vertices.get( 0 ) );
		expected.add( bundle.vertices.get( 6 ) );
		expected.add( bundle.vertices.get( 5 ) );
		expected.add( bundle.vertices.get( 4 ) );
		expected.add( bundle.vertices.get( 3 ) );
		expected.add( bundle.vertices.get( 2 ) );
		expected.add( bundle.vertices.get( 1 ) );
		final Iterator< ObjectVertex< Integer >> eit = expected.iterator();

		while ( eit.hasNext() )
		{
			assertTrue( "Iterator should not be finished, but is.", it.hasNext() );
			assertEquals( "Unexpected vertex met during iteration.", eit.next(), it.next() );
		}

		assertFalse( "Iteration should be finished, but is not.", it.hasNext() );
	}

	@Test
	public void testLoopPoolObjects()
	{
		final GraphTestBundle< TestVertex, TestEdge > bundle = GraphsForTests.loopPoolObjects();

		final TestVertex first = bundle.vertices.get( 0 );
		final InverseBreadthFirstIterator< TestVertex, TestEdge > it = new InverseBreadthFirstIterator<>( first, bundle.graph );

		final RefList< TestVertex > expected = CollectionUtils.createRefList( bundle.graph.vertices() );
		expected.add( bundle.vertices.get( 0 ) );
		expected.add( bundle.vertices.get( 6 ) );
		expected.add( bundle.vertices.get( 5 ) );
		expected.add( bundle.vertices.get( 4 ) );
		expected.add( bundle.vertices.get( 3 ) );
		expected.add( bundle.vertices.get( 2 ) );
		expected.add( bundle.vertices.get( 1 ) );
		final Iterator< TestVertex > eit = expected.iterator();

		while ( eit.hasNext() )
		{
			assertTrue( "Iterator should not be finished, but is.", it.hasNext() );
			assertEquals( "Unexpected vertex met during iteration.", eit.next(), it.next() );
		}

		assertFalse( "Iteration should be finished, but is not.", it.hasNext() );
	}

	@Test
	public void testSingleEdgePoolObjects()
	{
		final GraphTestBundle< TestVertex, TestEdge > bundle = GraphsForTests.singleEdgePoolObjects();

		final TestVertex first = bundle.vertices.get( 1 );
		final InverseBreadthFirstIterator< TestVertex, TestEdge > it = new InverseBreadthFirstIterator<>( first, bundle.graph );

		final RefList< TestVertex > expected = CollectionUtils.createRefList( bundle.graph.vertices() );
		expected.add( bundle.vertices.get( 1 ) );
		expected.add( bundle.vertices.get( 0 ) );
		final Iterator< TestVertex > eit = expected.iterator();

		while ( eit.hasNext() )
		{
			assertTrue( "Iterator should not be finished, but is.", it.hasNext() );
			assertEquals( "Unexpected vertex met during iteration.", eit.next(), it.next() );
		}

		assertFalse( "Iteration should be finished, but is not.", it.hasNext() );
	}

	@Test
	public void testSingleEdgeStdObjects()
	{
		final GraphTestBundle< ObjectVertex< Integer >, ObjectEdge< Integer >> bundle = GraphsForTests.singleEdgeStdObjects();

		final ObjectVertex< Integer > first = bundle.vertices.get( 1 );
		final InverseBreadthFirstIterator< ObjectVertex< Integer >, ObjectEdge< Integer >> it = new InverseBreadthFirstIterator<>( first, bundle.graph );

		final RefList< ObjectVertex< Integer >> expected = CollectionUtils.createRefList( bundle.graph.vertices() );
		expected.add( bundle.vertices.get( 1 ) );
		expected.add( bundle.vertices.get( 0 ) );
		final Iterator< ObjectVertex< Integer >> eit = expected.iterator();

		while ( eit.hasNext() )
		{
			assertTrue( "Iterator should not be finished, but is.", it.hasNext() );
			assertEquals( "Unexpected vertex met during iteration.", eit.next(), it.next() );
		}

		assertFalse( "Iteration should be finished, but is not.", it.hasNext() );
	}

	@Test
	public void testSingleVertexStdObjects()
	{
		final GraphTestBundle< ObjectVertex< Integer >, ObjectEdge< Integer >> bundle = GraphsForTests.singleVertexStdObjects();

		final ObjectVertex< Integer > first = bundle.vertices.get( 0 );
		final InverseBreadthFirstIterator< ObjectVertex< Integer >, ObjectEdge< Integer >> it = new InverseBreadthFirstIterator<>( first, bundle.graph );

		final RefList< ObjectVertex< Integer >> expected = CollectionUtils.createRefList( bundle.graph.vertices() );
		expected.add( bundle.vertices.get( 0 ) );
		final Iterator< ObjectVertex< Integer >> eit = expected.iterator();

		while ( eit.hasNext() )
		{
			assertTrue( "Iterator should not be finished, but is.", it.hasNext() );
			assertEquals( "Unexpected vertex met during iteration.", eit.next(), it.next() );
		}

		assertFalse( "Iteration should be finished, but is not.", it.hasNext() );
	}

	@Test
	public void testSingleVertexPoolObjects()
	{
		final GraphTestBundle< TestVertex, TestEdge > bundle = GraphsForTests.singleVertexPoolObjects();

		final TestVertex first = bundle.vertices.get( 0 );
		final InverseBreadthFirstIterator< TestVertex, TestEdge > it = new InverseBreadthFirstIterator<>( first, bundle.graph );

		final RefList< TestVertex > expected = CollectionUtils.createRefList( bundle.graph.vertices() );
		expected.add( bundle.vertices.get( 0 ) );
		final Iterator< TestVertex > eit = expected.iterator();

		while ( eit.hasNext() )
		{
			assertTrue( "Iterator should not be finished, but is.", it.hasNext() );
			assertEquals( "Unexpected vertex met during iteration.", eit.next(), it.next() );
		}

		assertFalse( "Iteration should be finished, but is not.", it.hasNext() );
	}

	@Test
	public void testWpExampleVertexPoolObjects()
	{
		final GraphTestBundle< TestVertex, TestEdge > bundle = GraphsForTests.wpExamplePoolObjects();

		final TestVertex first = bundle.vertices.get( 4 ); // E
		final InverseBreadthFirstIterator< TestVertex, TestEdge > it = new InverseBreadthFirstIterator<>( first, bundle.graph );

		final RefList< TestVertex > expected = CollectionUtils.createRefList( bundle.graph.vertices() );
		expected.add( bundle.vertices.get( 4 ) );
		expected.add( bundle.vertices.get( 0 ) );
		expected.add( bundle.vertices.get( 5 ) );
		expected.add( bundle.vertices.get( 1 ) );
		final Iterator< TestVertex > eit = expected.iterator();

		while ( eit.hasNext() )
		{
			assertTrue( "Iterator should not be finished, but is.", it.hasNext() );
			assertEquals( "Unexpected vertex met during iteration.", eit.next(), it.next() );
		}

		assertFalse( "Iteration should be finished, but is not.", it.hasNext() );
	}

	@Test
	public void testWpExampleVertexStdObjects()
	{
		final GraphTestBundle< ObjectVertex< Integer >, ObjectEdge< Integer >> bundle = GraphsForTests.wpExampleStdObjects();

		final ObjectVertex< Integer > first = bundle.vertices.get( 4 ); // E
		final InverseBreadthFirstIterator< ObjectVertex< Integer >, ObjectEdge< Integer >> it = new InverseBreadthFirstIterator<>( first, bundle.graph );

		final RefList< ObjectVertex< Integer >> expected = CollectionUtils.createRefList( bundle.graph.vertices() );
		expected.add( bundle.vertices.get( 4 ) );
		expected.add( bundle.vertices.get( 0 ) );
		expected.add( bundle.vertices.get( 5 ) );
		expected.add( bundle.vertices.get( 1 ) );
		final Iterator< ObjectVertex< Integer >> eit = expected.iterator();

		while ( eit.hasNext() )
		{
			assertTrue( "Iterator should not be finished, but is.", it.hasNext() );
			assertEquals( "Unexpected vertex met during iteration.", eit.next(), it.next() );
		}

		assertFalse( "Iteration should be finished, but is not.", it.hasNext() );
	}

	@Test
	public void testTwoComponentsVertexStdObjects()
	{
		final GraphTestBundle< ObjectVertex< Integer >, ObjectEdge< Integer >> bundle = GraphsForTests.twoComponentsStdObjects();

		final ObjectVertex< Integer > first = bundle.vertices.get( 4 ); // E
		final InverseBreadthFirstIterator< ObjectVertex< Integer >, ObjectEdge< Integer >> it = new InverseBreadthFirstIterator<>( first, bundle.graph );

		final RefList< ObjectVertex< Integer >> expected = CollectionUtils.createRefList( bundle.graph.vertices() );
		expected.add( bundle.vertices.get( 4 ) );
		expected.add( bundle.vertices.get( 0 ) );
		expected.add( bundle.vertices.get( 5 ) );
		expected.add( bundle.vertices.get( 1 ) );
		final Iterator< ObjectVertex< Integer >> eit = expected.iterator();

		while ( eit.hasNext() )
		{
			assertTrue( "Iterator should not be finished, but is.", it.hasNext() );
			assertEquals( "Unexpected vertex met during iteration.", eit.next(), it.next() );
		}

		assertFalse( "Iteration should be finished, but is not.", it.hasNext() );
	}

	@Test
	public void testTwoComponentsVertexPoolObjects()
	{
		final GraphTestBundle< TestVertex, TestEdge > bundle = GraphsForTests.twoComponentsPoolObjects();

		final TestVertex first = bundle.vertices.get( 4 ); // E
		final InverseBreadthFirstIterator< TestVertex, TestEdge > it = new InverseBreadthFirstIterator<>( first, bundle.graph );

		final RefList< TestVertex > expected = CollectionUtils.createRefList( bundle.graph.vertices() );
		expected.add( bundle.vertices.get( 4 ) );
		expected.add( bundle.vertices.get( 0 ) );
		expected.add( bundle.vertices.get( 5 ) );
		expected.add( bundle.vertices.get( 1 ) );
		final Iterator< TestVertex > eit = expected.iterator();

		while ( eit.hasNext() )
		{
			assertTrue( "Iterator should not be finished, but is.", it.hasNext() );
			assertEquals( "Unexpected vertex met during iteration.", eit.next(), it.next() );
		}

		assertFalse( "Iteration should be finished, but is not.", it.hasNext() );
	}
}
