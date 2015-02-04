package net.trackmate.graph.algorithm.object;

import net.trackmate.graph.object.ObjectGraph;
import net.trackmate.graph.object.ObjectVertex;

import org.junit.After;
import org.junit.Before;

public class TopologicalOrderIteratorTest
{
	private ObjectGraph< String > graph;

	@Before
	public void setUp()
	{
		// From http://en.wikipedia.org/wiki/Topological_sorting
		graph = new ObjectGraph< String >();
		final ObjectVertex< String > v7 = graph.addVertex().init( "7" );
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
		final ObjectVertex< String > v10 = graph.addVertex().init( "10" );
		graph.addEdge( v3, v10 );
		graph.addEdge( v11, v10 );
	}

	@After
	public void tearDown()
	{
		graph = null;
	}
}
