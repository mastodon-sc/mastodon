package net.trackmate.graph;

import org.junit.Before;

import net.trackmate.graph.collection.pool.PoolObjectPoolObjectMap;

public class PoolObjectPoolObjectMapAbstractTest
{
	protected TestVertex Ak;

	protected TestVertex Bk;

	protected PoolObjectPoolObjectMap< TestVertex, TestEdge > map;

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
		map = new PoolObjectPoolObjectMap< TestVertex, TestEdge >( graph.vertexPool, graph.edgePool );
		map.put( Bk, eAB );
		map.put( Ck, eAC );
		map.put( Dk, eBD );
		map.put( Ek, eCE );
	}
}
