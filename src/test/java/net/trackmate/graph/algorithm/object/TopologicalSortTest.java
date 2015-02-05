package net.trackmate.graph.algorithm.object;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import net.trackmate.graph.object.ObjectEdge;
import net.trackmate.graph.object.ObjectGraph;
import net.trackmate.graph.object.ObjectVertex;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TopologicalSortTest
{
	private ObjectGraph< String > graph;

	private ObjectVertex< String > v10;

	private ObjectVertex< String > v7;

	@Before
	public void setUp()
	{
		// From http://en.wikipedia.org/wiki/Topological_sorting
		graph = new ObjectGraph< String >();
		v7 = graph.addVertex().init( "7" );
		final ObjectVertex< String > v11 = graph.addVertex().init( "11" );
		graph.addEdge( v7, v11 );
		final ObjectVertex< String > v5 = graph.addVertex().init( "5" );
		graph.addEdge( v5, v11 );
		final ObjectVertex< String > v8 = graph.addVertex().init( "8" );
		graph.addEdge( v7, v8 );
		final ObjectVertex< String > v3 = graph.addVertex().init( "3" );
		graph.addEdge( v3, v8 );
		final ObjectVertex< String > v2 = graph.addVertex().init( "2" );
		graph.addEdge( v11, v2 );
		final ObjectVertex< String > v9 = graph.addVertex().init( "9" );
		graph.addEdge( v11, v9 );
		graph.addEdge( v8, v9 );
		v10 = graph.addVertex().init( "10" );
		graph.addEdge( v3, v10 );
		graph.addEdge( v11, v10 );
	}

	@After
	public void tearDown()
	{
		graph = null;
	}

	@Test
	public void testBehavior()
	{
		final TopologicalSort< ObjectVertex< String > > sort = new TopologicalSort< ObjectVertex< String > >( graph.getVertices().iterator() );
		assertFalse( sort.hasFailed() );
		final List< ObjectVertex< String >> list = sort.get();

		for ( int i = 0; i < list.size(); i++ )
		{
			final ObjectVertex< String > current = list.get( i );
			for ( final ObjectEdge< String > e : current.outgoingEdges() )
			{
				final ObjectVertex< String > target = e.getTarget();
				final boolean dependenceInList = list.subList( 0, i ).contains( target );
				assertTrue( "The dependency of " + current + ", " + target + ", could not be found in the sorted list before him.", dependenceInList );
			}
		}
		assertEquals( "Did not iterate through all the vertices of the graph.", graph.getVertices().size(), sort.get().size() );
	}

	@Test
	public void testNotDAG()
	{
		final TopologicalSort< ObjectVertex< String > > it = new TopologicalSort< ObjectVertex< String > >( graph.getVertices().iterator() );
		assertFalse( it.hasFailed() );

		graph.addEdge( v10, v7 );
		final TopologicalSort< ObjectVertex< String > > it2 = new TopologicalSort< ObjectVertex< String > >( graph.getVertices().iterator() );
		assertTrue( it2.hasFailed() );
	}

}
