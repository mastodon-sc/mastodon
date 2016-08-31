package org.mastodon.graph.object;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.mastodon.graph.object.ObjectEdge;
import org.mastodon.graph.object.ObjectGraph;
import org.mastodon.graph.object.ObjectVertex;

public class ObjectGraphTest
{

	@Test
	public void testEdgePresence()
	{
		final ObjectGraph< Integer > graph = new ObjectGraph<>();

		final ObjectVertex< Integer > v0 = graph.addVertex().init( 0 );
		final ObjectVertex< Integer > v1 = graph.addVertex().init( 1 );
		final ObjectEdge< Integer > e = graph.addEdge( v0, v1 );

		assertTrue( "Incoming edges for vertex " + v0 + " should be empty.", v0.incomingEdges().isEmpty() );

		assertEquals( "Outgoing edges for vertex " + v0 + " does not have the expected size.", 1, v0.outgoingEdges().size() );
		assertEquals( "Unexpected outgoing edge.", e, v0.outgoingEdges().iterator().next() );

		assertEquals( "All edges for vertex " + v0 + " does not have the expected size.", 1, v0.edges().size() );
		assertEquals( "Unexpected edge.", e, v0.edges().iterator().next() );

		assertEquals( "Incoming edges for vertex " + v1 + " does not have the expected size.", 1, v1.incomingEdges().size() );
		assertEquals( "Unexpected incoming edge.", e, v1.incomingEdges().iterator().next() );

		assertTrue( "Outgoing edges for vertex " + v1 + " should be empty.", v1.outgoingEdges().isEmpty() );

		assertEquals( "All edges for vertex " + v1 + " does not have the expected size.", 1, v1.edges().size() );
		assertEquals( "Unexpected edge.", e, v1.edges().iterator().next() );

	}

}
