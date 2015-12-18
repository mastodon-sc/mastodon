package net.trackmate.graph;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;

public class PoolObjectPoolObjectMapTwoHeteroVertexPoolTest extends PoolObjectPoolObjectMapTwoVertexPoolTest
{

	private TestGraph graph2;

	private TestVertex Ak2;

	private TestVertex Bk2;

	private TestVertex Ck2;

	private TestVertex Dk2;

	private TestVertex Ek2;

	/**
	 * Test a map that maps vertices from two different pools.
	 */
	@Override
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

		graph2 = new TestGraph();
		Ak2 = graph2.addVertex().init( 0 );
		Bk2 = graph2.addVertex().init( 1 );
		Ck2 = graph2.addVertex().init( 2 );
		Dk2 = graph2.addVertex().init( 3 );
		Ek2 = graph2.addVertex().init( 4 );

		map = new PoolObjectPoolObjectMap< TestVertex, TestVertex >( graph.vertexPool, graph2.vertexPool );
		map.put( Bk, Ck2 );
		map.put( Ck, Dk2 );
		map.put( Dk, Ek2 );
		map.put( Ek, Ak2 );
		/*
		 * 4 mappings. No key for vertex Ak, no value for vertex Bk2.
		 */
	}

	/**
	 * We want to test that the instances we are using are what is expected in
	 * this test unit. For this instance, we expect to test a key vertex pool
	 * different from the value vertex pool.
	 */
	@Override
	@Test
	public void testTest()
	{
		final TestVertex key = map.keySet().iterator().next();
		final TestVertex val = map.get( key );
		assertNotEquals( "The map key vertex pool and the map value vertex pool are different.", key.creatingPool, val.creatingPool );
	}

	@Override
	@Test
	public void testGetObject()
	{
		assertEquals( "Unexpected mapping for key B.", Ck2, map.get( Bk ) );
		assertEquals( "Unexpected mapping for key D.", Ek2, map.get( Dk ) );
		assertEquals( "Unexpected mapping for key C.", Dk2, map.get( Ck ) );
		assertEquals( "Unexpected mapping for key E.", Ak2, map.get( Ek ) );
		assertNull( "There should not be a mapping for key A.", map.get( Ak ) );
	}

	@Override
	@Test
	public void testGetObjectL()
	{
		final TestVertex ref = map.createValueRef();
		map.get( Bk, ref );
		assertEquals( "Unexpected mapping for key B.", Ck2, ref );
		map.get( Dk, ref );
		assertEquals( "Unexpected mapping for key D.", Ek2, ref );
		map.get( Ck, ref );
		assertEquals( "Unexpected mapping for key C.", Dk2, ref );
		map.get( Ek, ref );
		assertEquals( "Unexpected mapping for key E.", Ak2, ref );
		assertNull( "There should not be a mapping for key A.", map.get( Ak, ref ) );

		assertEquals( "Unexpected mapping for key B.", Ck2, map.get( Bk, ref ) );
		assertEquals( "Unexpected mapping for key D.", Ek2, map.get( Dk, ref ) );
		assertEquals( "Unexpected mapping for key C.", Dk2, map.get( Ck, ref ) );
		assertEquals( "Unexpected mapping for key E.", Ak2, map.get( Ek, ref ) );
	}

	@Override
	@Test
	public void testPutKLL()
	{
		final TestVertex ref = graph.vertexRef();

		// Add a new key
		final TestVertex put = map.put( Ak, Bk2, ref );
		assertNull( "There should not be any mapping prior to adding this key.", put );
		assertEquals( "Could not find the expected value for the new key.", Bk2, map.get( Ak, ref ) );

		// Replace an existing key
		final TestVertex put2 = map.put( Bk, Ak2, ref );
		assertEquals( "Could not retrieve the expected value for the old key.", Ck2, put2 );
		assertEquals( "Could not find the expected value for the new key.", Ak2, map.get( Bk, ref ) );
	}

	@Override
	@Test
	public void testPutKL()
	{
		// Add a new key
		final TestVertex put = map.put( Ak, Bk );
		assertNull( "There should not be any mapping prior to adding this key.", put );
		assertEquals( "Could not find the expected value for the new key.", Bk2, map.get( Ak ) );

		// Replace an existing key
		final TestVertex put2 = map.put( Bk, Ak2 );
		assertEquals( "Could not retrieve the expected value for the old key.", Ck2, put2 );
		assertEquals( "Could not find the expected value for the new key.", Ak2, map.get( Bk ) );
	}


	@Override
	@Test
	public void testRemoveObject()
	{
		final int size = map.size();

		// Remove a non existing mapping
		final TestVertex remove = map.remove( Ak );
		assertNull( "Removing a non-exiting mapping should return null.", remove );
		assertEquals( "Map size should not have changed.", size, map.size() );

		// Remove an existing mapping
		final TestVertex remove2 = map.remove( Bk );
		assertEquals( "Did not retrieve the expected value upong key removal.", Ck2, remove2 );
		assertEquals( "Map size should have decreased by 1.", size - 1, map.size() );
	}

	@Override
	@Test
	public void testRemoveObjectL()
	{
		final int size = map.size();
		final TestVertex ref = graph.vertexRef();

		// Remove a non existing mapping
		final TestVertex remove = map.removeWithRef( Ak, ref );
		assertNull( "Removing a non-exiting mapping should return null.", remove );
		assertEquals( "Map size should not have changed.", size, map.size() );

		// Remove an existing mapping
		final TestVertex remove2 = map.removeWithRef( Bk, ref );
		assertEquals( "Did not retrieve the expected value upong key removal.", Ck2, remove2 );
		assertEquals( "Map size should have decreased by 1.", size - 1, map.size() );
	}

	@Override
	@Test
	public void testPutAll()
	{
		final PoolObjectPoolObjectMap< TestVertex, TestVertex > extraMap = new PoolObjectPoolObjectMap< TestVertex, TestVertex >( graph.vertexPool, graph2.vertexPool );
		extraMap.put( Ak, Bk2 );
		// Careful to add 1 mapping not already present in the map.
		extraMap.put( Bk, Ak2 );
		// Change one mapping.

		final int initSize = map.size();
		map.putAll( extraMap );
		assertEquals( "Map after putAll does not have the expected size.", initSize + 1, map.size() );
		assertEquals( "New mapping is not right.", Bk2, map.get( Ak ) );
		assertEquals( "New mapping is not right.", Ak2, map.get( Bk ) );
	}

}
