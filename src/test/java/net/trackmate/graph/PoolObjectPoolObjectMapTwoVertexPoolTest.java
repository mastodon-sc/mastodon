package net.trackmate.graph;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import net.trackmate.graph.collection.RefSet;

import org.junit.Test;

public class PoolObjectPoolObjectMapTwoVertexPoolTest extends PoolObjectPoolObjectMapVertexPoolAbstractTest
{

	/**
	 * We want to test that the instances we are using are what is expected in
	 * this test unit. For this instance, we expect to test two vertex pools
	 * coming from the same graph.
	 */
	@Test
	public void testTest()
	{
		final TestVertex key = map.keySet().iterator().next();
		final TestVertex val = map.get( key );
		assertEquals( "The map key vertex pool and the map value vertex pool are different.", key.creatingPool, val.creatingPool );
	}

	@Test
	public void testClear()
	{
		assertFalse( "Map should not be empty now,", map.isEmpty() );
		map.clear();
		assertTrue( "Map should be empty now,", map.isEmpty() );
	}

	@Test
	public void testContainsKey()
	{
		assertFalse( "Map should not contain a key for vertex A.", map.containsKey( Ak ) );
		assertTrue( "Map should contain a key for vertex B.", map.containsKey( Bk ) );
	}

	@Test
	public void testContainsValue()
	{
		assertFalse( "Map should not contain a value for vertex Bk.", map.containsValue( Bk ) );
		assertTrue( "Map should contain a value for vertex Ak.", map.containsValue( Ak ) );
	}

	@Test
	public void testGetObject()
	{
		assertEquals( "Unexpected mapping for key B.", Ck, map.get( Bk ) );
		assertEquals( "Unexpected mapping for key D.", Ek, map.get( Dk ) );
		assertEquals( "Unexpected mapping for key C.", Dk, map.get( Ck ) );
		assertEquals( "Unexpected mapping for key E.", Ak, map.get( Ek ) );
		assertNull( "There should not be a mapping for key A.", map.get( Ak ) );
	}

	@Test
	public void testGetObjectL()
	{
		final TestVertex ref = map.createValueRef();
		map.get( Bk, ref );
		assertEquals( "Unexpected mapping for key B.", Ck, ref );
		map.get( Dk, ref );
		assertEquals( "Unexpected mapping for key D.", Ek, ref );
		map.get( Ck, ref );
		assertEquals( "Unexpected mapping for key C.", Dk, ref );
		map.get( Ek, ref );
		assertEquals( "Unexpected mapping for key E.", Ak, ref );
		assertNull( "There should not be a mapping for key A.", map.get( Ak, ref ) );

		assertEquals( "Unexpected mapping for key B.", Ck, map.get( Bk, ref ) );
		assertEquals( "Unexpected mapping for key D.", Ek, map.get( Dk, ref ) );
		assertEquals( "Unexpected mapping for key C.", Dk, map.get( Ck, ref ) );
		assertEquals( "Unexpected mapping for key E.", Ak, map.get( Ek, ref ) );
	}

	@Test
	public void testKeySet()
	{
		final Set< TestVertex > keySet = map.keySet();
		assertTrue( "Set returned should be a PoolObjectSet.", keySet instanceof PoolObjectSet );
		final RefSet< TestVertex > set = graph.createVertexSet();
		set.add( Bk );
		set.add( Ck );
		set.add( Dk );
		set.add( Ek );
		// All but Ak

		for ( final TestVertex key : keySet )
		{
			assertTrue( "Unexpected key found in the key set.", set.remove( key ) );
		}
		assertTrue( "All the expected keys have not been fount in the key set.", set.isEmpty() );
	}

	@Test
	public void testPutKLL()
	{
		final TestVertex ref = graph.vertexRef();

		// Add a new key
		final TestVertex put = map.put( Ak, Bk, ref );
		assertNull( "There should not be any mapping prior to adding this key.", put );
		assertEquals( "Could not find the expected value for the new key.", Bk, map.get( Ak, ref ) );

		// Replace an existing key
		final TestVertex put2 = map.put( Bk, Ak, ref );
		assertEquals( "Could not retrieve the expected value for the old key.", Ck, put2 );
		assertEquals( "Could not find the expected value for the new key.", Ak, map.get( Bk, ref ) );
	}

	@Test
	public void testPutKL()
	{
		// Add a new key
		final TestVertex put = map.put( Ak, Bk );
		assertNull( "There should not be any mapping prior to adding this key.", put );
		assertEquals( "Could not find the expected value for the new key.", Bk, map.get( Ak ) );

		// Replace an existing key
		final TestVertex put2 = map.put( Bk, Ak );
		assertEquals( "Could not retrieve the expected value for the old key.", Ck, put2 );
		assertEquals( "Could not find the expected value for the new key.", Ak, map.get( Bk ) );
	}

	@Test
	public void testPutAll()
	{
		final PoolObjectPoolObjectMap< TestVertex, TestVertex > extraMap = new PoolObjectPoolObjectMap< TestVertex, TestVertex >( graph.vertexPool, graph.vertexPool );
		extraMap.put( Ak, Bk );
		// Careful to add 1 mapping not already present in the map.
		extraMap.put( Bk, Ak );
		// Change one mapping.

		final int initSize = map.size();
		map.putAll( extraMap );
		assertEquals( "Map after putAll does not have the expected size.", initSize + 1, map.size() );
		assertEquals( "New mapping is not right.", Bk, map.get( Ak ) );
		assertEquals( "New mapping is not right.", Ak, map.get( Bk ) );
	}

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
		assertEquals( "Did not retrieve the expected value upong key removal.", Ck, remove2 );
		assertEquals( "Map size should have decreased by 1.", size - 1, map.size() );
	}

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
		assertEquals( "Did not retrieve the expected value upong key removal.", Ck, remove2 );
		assertEquals( "Map size should have decreased by 1.", size - 1, map.size() );
	}

	@Test
	public void testSize()
	{
		final int initSize = 4;
		assertEquals( "Map does not report the expected size.", initSize, map.size() );
		map.remove( Ak ); // absent
		map.remove( Bk ); // present
		map.remove( Ck ); // present
		assertEquals( "Map does not report the expected size after changes.", initSize - 2, map.size() );
	}

	@Test
	public void testCreateRef()
	{
		final TestVertex ref = map.createKeyRef();
		assertNotNull( "Created reference object is null.", ref );
	}

	@Test
	public void testCreateValueRef()
	{
		final TestVertex ref = map.createValueRef();
		assertNotNull( "Created reference object is null.", ref );
	}

}
