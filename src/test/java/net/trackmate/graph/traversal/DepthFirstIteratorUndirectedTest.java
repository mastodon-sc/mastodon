package net.trackmate.graph.traversal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Iterator;

import net.trackmate.graph.Graph;
import net.trackmate.graph.TestEdge;
import net.trackmate.graph.TestGraph;
import net.trackmate.graph.TestVertex;
import net.trackmate.graph.collection.CollectionUtils;
import net.trackmate.graph.object.ObjectGraph;
import net.trackmate.graph.object.ObjectVertex;

import org.junit.Before;
import org.junit.Test;

public class DepthFirstIteratorUndirectedTest
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
		/*
		 * Directed vs undirected.
		 */

		final GraphIteratorBuilder< TestVertex, TestEdge > builder = GraphIteratorBuilder.createOn( graph ).depthFirst( B ).unsorted();

		final Iterator< TestVertex > dfi = CollectionUtils.safeIterator( builder.directed().build() );
		assertEquals( dfi.next().getId(), 2 );
		assertEquals( dfi.next().getId(), 6 );
		assertEquals( dfi.next().getId(), 4 );
		assertFalse( dfi.hasNext() );

		final Iterator< TestVertex > dfiud = CollectionUtils.safeIterator( builder.undirected().build() );
		System.out.println( builder.undirected().build() );// DEBUG
		while ( dfiud.hasNext() )
		{
			System.out.println( dfiud.next() );
		}

		assertEquals( dfiud.next().getId(), 2 );
		assertEquals( dfiud.next().getId(), 6 );
		assertEquals( dfiud.next().getId(), 4 );
		assertFalse( dfiud.hasNext() );

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

		final Iterator< TestVertex > iter = GraphIteratorBuilder.createOn( graph ).depthFirst( v1 ).unsorted().directed().build();
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

		final Iterator< TestVertex > iter = GraphIteratorBuilder.createOn( graph ).depthFirst( v1 ).unsorted().directed().build();
		assertEquals( iter.next().getId(), 1 );
		assertEquals( iter.next().getId(), 11 );
		assertEquals( iter.next().getId(), 111 );
		assertEquals( iter.next().getId(), 12 );
		assertFalse( iter.hasNext() );
	}

	@Test
	public void testDepthFirstIteratorBigLoop()
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

		final Iterator< TestVertex > iter = GraphIteratorBuilder.createOn( graph ).depthFirst( v1 ).unsorted().directed().build();
		assertEquals( iter.next().getId(), 1 );
		assertEquals( iter.next().getId(), 2 );
		assertEquals( iter.next().getId(), 3 );
		assertEquals( iter.next().getId(), 4 );
		assertEquals( iter.next().getId(), 5 );
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

		final Iterator< ObjectVertex< Integer >> iter = GraphIteratorBuilder.createOn( graph ).depthFirst( v1 ).unsorted().directed().build();
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

		final Iterator< ObjectVertex< Integer >> iter = GraphIteratorBuilder.createOn( graph ).depthFirst( v1 ).unsorted().directed().build();
		assertEquals( iter.next().getContent().intValue(), 1 );
		assertEquals( iter.next().getContent().intValue(), 11 );
		assertEquals( iter.next().getContent().intValue(), 111 );
		assertEquals( iter.next().getContent().intValue(), 12 );
		assertFalse( iter.hasNext() );
	}
}
