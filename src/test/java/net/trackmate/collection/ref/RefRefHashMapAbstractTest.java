package net.trackmate.collection.ref;

import org.junit.Before;

import net.trackmate.graph.TestEdge;
import net.trackmate.graph.TestGraph;
import net.trackmate.graph.TestVertex;

public class RefRefHashMapAbstractTest
{
	protected TestVertex Ak;

	protected TestVertex Bk;

	protected RefRefHashMap< TestVertex, TestEdge > map;

	protected TestEdge eEA;

	protected TestEdge eAB;

	protected TestEdge eAC;

	protected TestEdge eBD;

	protected TestEdge eCE;

	protected TestVertex Ck;

	protected TestVertex Dk;

	protected TestVertex Ek;

	protected TestGraph graph;

	/**
	 * We test for a key pool and a value pool that are linked in a graph.
	 *
	 * @throws Exception
	 */
	@Before
	public void setUp() throws Exception
	{
		graph = new TestGraph();

		Ak = graph.addVertex().init( 0 );
		Bk = graph.addVertex().init( 1 );
		Ck = graph.addVertex().init( 2 );
		Dk = graph.addVertex().init( 3 );
		Ek = graph.addVertex().init( 4 );

		eAB = graph.addEdge( Ak, Bk );
		eAC = graph.addEdge( Ak, Ck );
		eBD = graph.addEdge( Bk, Dk );
		eCE = graph.addEdge( Ck, Ek );
		eEA = graph.addEdge( Ek, Ak );

		// Map each vertex to edge going in.
		map = new RefRefHashMap< TestVertex, TestEdge >( graph.getVertexPool(), graph.getEdgePool() );
		map.put( Bk, eAB );
		map.put( Ck, eAC );
		map.put( Dk, eBD );
		map.put( Ek, eCE );
	}
}
