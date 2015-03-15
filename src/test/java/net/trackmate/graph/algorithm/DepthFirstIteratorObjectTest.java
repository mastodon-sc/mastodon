package net.trackmate.graph.algorithm;

import net.trackmate.graph.object.ObjectEdge;
import net.trackmate.graph.object.ObjectGraph;
import net.trackmate.graph.object.ObjectVertex;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class DepthFirstIteratorObjectTest
{
	@Test
	public void testDepthFirstIterator()
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
}
