package net.trackmate.graph;

import net.trackmate.graph.mempool.ByteMappedElement;

import org.junit.Before;

public abstract class PoolObjectPoolObjectMapVertexPoolAbstractTest
{
	protected TestGraph graph;

	protected TestVertex Ak;

	protected TestVertex Bk;

	protected TestVertex Ck;

	protected TestVertex Dk;

	protected TestVertex Ek;

	protected PoolObjectPoolObjectMap< TestVertex, TestVertex, ByteMappedElement, ByteMappedElement > map;

	/**
	 * Test a map that maps vertices from the same pool.
	 */
	@Before
	public void setUp()
	{
		graph = new TestGraph();

		Ak = graph.addVertex().init( 0 );
		Bk = graph.addVertex().init( 1 );
		Ck = graph.addVertex().init( 2 );
		Dk = graph.addVertex().init( 3 );
		Ek = graph.addVertex().init( 4 );

		graph.addEdge( Ak, Bk );
		graph.addEdge( Ak, Ck );
		graph.addEdge( Bk, Dk );
		graph.addEdge( Ck, Ek );
		graph.addEdge( Ek, Ak );

		map = new PoolObjectPoolObjectMap< TestVertex, TestVertex, ByteMappedElement, ByteMappedElement >( graph.vertexPool, graph.vertexPool );
		map.put( Bk, Ck );
		map.put( Ck, Dk );
		map.put( Dk, Ek );
		map.put( Ek, Ak );
		/*
		 * 4 mappings. No key for vertex Ak, no value for vertex Bk.
		 */
	}

}
