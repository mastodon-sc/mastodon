package net.trackmate.graph.traversal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;

import net.trackmate.graph.Graph;
import net.trackmate.graph.TestEdge;
import net.trackmate.graph.TestGraph;
import net.trackmate.graph.TestVertex;
import net.trackmate.graph.collection.CollectionUtils;
import net.trackmate.graph.object.ObjectEdge;
import net.trackmate.graph.object.ObjectGraph;
import net.trackmate.graph.object.ObjectVertex;
import net.trackmate.graph.traversal.DepthFirstIterator;

import org.junit.Before;
import org.junit.Test;

public class DepthFirstIteratorTest
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
		final Iterator< TestVertex > dfi = CollectionUtils.safeIterator( new DepthFirstIterator< TestVertex, TestEdge >( A, graph ) );

		assertTrue( "After initiation, DepthFirstIterator always has next. Got false.", dfi.hasNext() );

		final TestVertex first = dfi.next();
		assertTrue( "The first vertex iterated must be the root. got " + first + ".", A.equals( first ) );

		TestVertex previous = first;
		while ( dfi.hasNext() )
		{
			final TestVertex vertex = dfi.next();
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

	@Test
	public void testDepthFirstIterator()
	{
		final Graph< TestVertex, TestEdge > graph = new TestGraph();

		final TestVertex v1 = graph.addVertex().init( 1 );
		final TestVertex v11 = graph.addVertex().init( 11 );
		final TestVertex v12 = graph.addVertex().init( 12 );
		final TestVertex v111 = graph.addVertex().init( 111 );
		final TestVertex v112 = graph.addVertex().init( 112 );
		final TestVertex v121 = graph.addVertex().init( 121 );
		final TestVertex v122 = graph.addVertex().init( 122 );
		graph.addEdge( v12, v122 );
		graph.addEdge( v12, v121 );
		graph.addEdge( v11, v112 );
		graph.addEdge( v11, v111 );
		graph.addEdge( v1, v12 );
		graph.addEdge( v1, v11 );

		final DepthFirstIterator< TestVertex, TestEdge > iter = DepthFirstIterator.create( v1, graph );
		assertEquals( iter.next().getId(), 1 );
		assertEquals( iter.next().getId(), 11 );
		assertEquals( iter.next().getId(), 111 );
		assertEquals( iter.next().getId(), 112 );
		assertEquals( iter.next().getId(), 12 );
		assertEquals( iter.next().getId(), 121 );
		assertEquals( iter.next().getId(), 122 );
		assertFalse( iter.hasNext() );
	}

	@Test
	public void testDepthFirstIteratorCycle()
	{
		final Graph< TestVertex, TestEdge > graph = new TestGraph();

		final TestVertex v1 = graph.addVertex().init( 1 );
		final TestVertex v11 = graph.addVertex().init( 11 );
		final TestVertex v12 = graph.addVertex().init( 12 );
		final TestVertex v111 = graph.addVertex().init( 111 );
		graph.addEdge( v11, v111 );
		graph.addEdge( v12, v111 );
		graph.addEdge( v1, v12 );
		graph.addEdge( v1, v11 );

		final DepthFirstIterator< TestVertex, TestEdge > iter = DepthFirstIterator.create( v1, graph );
		assertEquals( iter.next().getId(), 1 );
		assertEquals( iter.next().getId(), 11 );
		assertEquals( iter.next().getId(), 111 );
		assertEquals( iter.next().getId(), 12 );
		assertFalse( iter.hasNext() );
	}

	@Test
	public void testDepthFirstIteratorWithObjectGraph()
	{
		final ObjectGraph< Integer > graph = new ObjectGraph< Integer >();
		final ObjectVertex< Integer > v1 = graph.addVertex().init( 1 );
		final ObjectVertex< Integer > v11 = graph.addVertex().init( 11 );
		final ObjectVertex< Integer > v12 = graph.addVertex().init( 12 );
		final ObjectVertex< Integer > v111 = graph.addVertex().init( 111 );
		final ObjectVertex< Integer > v112 = graph.addVertex().init( 112 );
		final ObjectVertex< Integer > v121 = graph.addVertex().init( 121 );
		final ObjectVertex< Integer > v122 = graph.addVertex().init( 122 );

		graph.addEdge( v12, v122 );
		graph.addEdge( v12, v121 );
		graph.addEdge( v11, v112 );
		graph.addEdge( v11, v111 );
		graph.addEdge( v1, v12 );
		graph.addEdge( v1, v11 );

		final DepthFirstIterator< ObjectVertex< Integer >, ObjectEdge< Integer > > iter = DepthFirstIterator.create( v1, graph );

		assertEquals( iter.next().getContent().intValue(), 1 );
		assertEquals( iter.next().getContent().intValue(), 11 );
		assertEquals( iter.next().getContent().intValue(), 111 );
		assertEquals( iter.next().getContent().intValue(), 112 );
		assertEquals( iter.next().getContent().intValue(), 12 );
		assertEquals( iter.next().getContent().intValue(), 121 );
		assertEquals( iter.next().getContent().intValue(), 122 );
		assertFalse( iter.hasNext() );
	}

	@Test
	public void testDepthFirstIteratorCycleWithObjectGraph()
	{
		final ObjectGraph< Integer > graph = new ObjectGraph< Integer >();
		final ObjectVertex< Integer > v1 = graph.addVertex().init( 1 );
		final ObjectVertex< Integer > v11 = graph.addVertex().init( 11 );
		final ObjectVertex< Integer > v12 = graph.addVertex().init( 12 );
		final ObjectVertex< Integer > v111 = graph.addVertex().init( 111 );

		graph.addEdge( v11, v111 );
		graph.addEdge( v12, v111 );
		graph.addEdge( v1, v12 );
		graph.addEdge( v1, v11 );

		final DepthFirstIterator< ObjectVertex< Integer >, ObjectEdge< Integer > > iter = DepthFirstIterator.create( v1, graph );

		assertEquals( iter.next().getContent().intValue(), 1 );
		assertEquals( iter.next().getContent().intValue(), 11 );
		assertEquals( iter.next().getContent().intValue(), 111 );
		assertEquals( iter.next().getContent().intValue(), 12 );
		assertFalse( iter.hasNext() );
	}
}
