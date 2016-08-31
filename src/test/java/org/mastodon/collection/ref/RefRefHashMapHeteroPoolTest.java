package org.mastodon.collection.ref;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.Before;
import org.junit.Test;
import org.mastodon.collection.ref.RefRefHashMap;
import org.mastodon.graph.TestEdge;
import org.mastodon.graph.TestGraph;
import org.mastodon.graph.TestVertex;

/**
 * This test reproduces the tests from its parent class, but uses a map between
 * two pools that do not belong to the same graph.
 *
 * @author Jean-Yves Tinevez - 2015
 */
public class RefRefHashMapHeteroPoolTest extends RefRefHashMapTest
{

	@Before
	@Override
	public void setUp() throws Exception
	{
		graph = new TestGraph();
		final TestGraph graph2 = new TestGraph();

		Ak = graph.addVertex().init( 0 );
		Bk = graph.addVertex().init( 1 );
		Ck = graph.addVertex().init( 2 );
		Dk = graph.addVertex().init( 3 );
		Ek = graph.addVertex().init( 4 );

		final TestVertex Ak2 = graph2.addVertex().init( 0 );
		final TestVertex Bk2 = graph2.addVertex().init( 1 );
		final TestVertex Ck2 = graph2.addVertex().init( 2 );
		final TestVertex Dk2 = graph2.addVertex().init( 3 );
		final TestVertex Ek2 = graph2.addVertex().init( 4 );

		eAB = graph2.addEdge( Ak2, Bk2 );
		eAC = graph2.addEdge( Ak2, Ck2 );
		eBD = graph2.addEdge( Bk2, Dk2 );
		eCE = graph2.addEdge( Ck2, Ek2 );
		eEA = graph2.addEdge( Ek2, Ak2 );

		// Map each vertex to edge going in.
		map = new RefRefHashMap< TestVertex, TestEdge >( graph.getVertexPool(), graph2.getEdgePool() );
		map.put( Bk, eAB );
		map.put( Ck, eAC );
		map.put( Dk, eBD );
		map.put( Ek, eCE );
	}

	/**
	 * We want to test that the instances we are using are what is expected in
	 * this test unit. For this instance, we expect to test a vertex pool and an
	 * edge pool that DO NOT come from the same graph.
	 */
	@Override
	@Test
	public void testTest()
	{
		assertEquals( "The graph vertex pool and the map key vertex pool are different.", graph.getVertexPool(), Ak.creatingPool );
		assertNotEquals( "The graph edge pool and the map value edge pool are the same.", graph.getEdgePool(), eAB.creatingPool );
	}

}
