package net.trackmate.collection.ref;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Test;

import net.trackmate.collection.RefSet;
import net.trackmate.graph.TestEdge;
import net.trackmate.graph.TestVertex;

/**
 * Test map from vertices to edges, both belong to the same graph.
 *
 * @author Jean-Yves Tinevez - 2015
 */
public class RefRefHashMapTest extends RefRefHashMapAbstractTest
{

	/**
	 * We want to test that the instances we are using are what is expected in
	 * this test unit. For this instance, we expect to test a vertex pool and an
	 * edge pool that come from the same graph.
	 */
	@Test
	public void testTest()
	{
		assertEquals( "The graph vertex pool and the map key vertex pool are different.", graph.getVertexPool(), Ak.creatingPool );
		assertEquals( "The graph edge pool and the map value edge pool are different.", graph.getEdgePool(), eAB.creatingPool );
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
		assertFalse( "Map should not contain a key for edge E->A.", map.containsValue( eEA ) );
		assertTrue( "Map should contain a key for edge A->B.", map.containsValue( eAB ) );
	}

	@Test
	public void testGetObject()
	{
		assertEquals( "Unexpected mapping for key B.", eAB, map.get( Bk ) );
		assertEquals( "Unexpected mapping for key D.", eBD, map.get( Dk ) );
		assertEquals( "Unexpected mapping for key C.", eAC, map.get( Ck ) );
		assertEquals( "Unexpected mapping for key E.", eCE, map.get( Ek ) );
		assertNull( "There should not be a mapping for key A.", map.get( Ak ) );
	}

	@Test
	public void testGetObjectL()
	{
		final TestEdge ref = map.createValueRef();
		map.get( Bk, ref );
		assertEquals( "Unexpected mapping for key B.", eAB, ref );
		map.get( Dk, ref );
		assertEquals( "Unexpected mapping for key D.", eBD, ref );
		map.get( Ck, ref );
		assertEquals( "Unexpected mapping for key C.", eAC, ref );
		map.get( Ek, ref );
		assertEquals( "Unexpected mapping for key E.", eCE, ref );
		assertNull( "There should not be a mapping for key A.", map.get( Ak, ref ) );

		assertEquals( "Unexpected mapping for key B.", eAB, map.get( Bk, ref ) );
		assertEquals( "Unexpected mapping for key D.", eBD, map.get( Dk, ref ) );
		assertEquals( "Unexpected mapping for key C.", eAC, map.get( Ck, ref ) );
		assertEquals( "Unexpected mapping for key E.", eCE, map.get( Ek, ref ) );
	}

	@Test
	public void testKeySet()
	{
		final Set< TestVertex > keySet = map.keySet();
		assertTrue( "Set returned should be a PoolObjectSet.", keySet instanceof RefSetImp );
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
		final TestEdge ref = graph.edgeRef();

		// Add a new key
		final TestEdge put = map.put( Ak, eAB, ref );
		assertNull( "There should not be any mapping prior to adding this key.", put );
		assertEquals( "Could not find the expected value for the new key.", eAB, map.get( Ak, ref ) );

		// Replace an existing key
		final TestEdge put2 = map.put( Bk, eEA, ref );
		assertEquals( "Could not retrieve the expected value for the old key.", eAB, put2 );
		assertEquals( "Could not find the expected value for the new key.", eEA, map.get( Bk, ref ) );
	}

	@Test
	public void testPutKL()
	{
		// Add a new key
		final TestEdge put = map.put( Ak, eAB );
		assertNull( "There should not be any mapping prior to adding this key.", put );
		assertEquals( "Could not find the expected value for the new key.", eAB, map.get( Ak ) );

		// Replace an existing key
		final TestEdge put2 = map.put( Bk, eEA );
		assertEquals( "Could not retrieve the expected value for the old key.", eAB, put2 );
		assertEquals( "Could not find the expected value for the new key.", eEA, map.get( Bk ) );
	}

	@Test
	public void testPutAll()
	{
		final RefRefHashMap< TestVertex, TestEdge > extraMap = new RefRefHashMap< TestVertex, TestEdge >( graph.getVertexPool(), graph.getEdgePool() );
		extraMap.put( Ak, eAB );
		// Careful to add 1 mapping not already present in the map.
		extraMap.put( Bk, eAC );
		// Change one mapping.

		final int initSize = map.size();
		map.putAll( extraMap );
		assertEquals( "Map after putAll does not have the expected size.", initSize + 1, map.size() );
		assertEquals( "New mapping is not right.", eAB, map.get( Ak ) );
		assertEquals( "New mapping is not right.", eAC, map.get( Bk ) );
	}

	@Test
	public void testRemoveObjectL()
	{
		final int size = map.size();
		final TestEdge ref = graph.edgeRef();

		// Remove a non existing mapping
		final TestEdge remove = map.removeWithRef( Ak, ref );
		assertNull( "Removing a non-exiting mapping should return null.", remove );
		assertEquals( "Map size should not have changed.", size, map.size() );

		// Remove an existing mapping
		final TestEdge remove2 = map.removeWithRef( Bk, ref );
		assertEquals( "Did not retrieve the expected value upong key removal.", eAB, remove2 );
		assertEquals( "Map size should have decreased by 1.", size - 1, map.size() );
	}

	@Test
	public void testRemoveObject()
	{
		final int size = map.size();

		// Remove a non existing mapping
		final TestEdge remove = map.remove( Ak );
		assertNull( "Removing a non-exiting mapping should return null.", remove );
		assertEquals( "Map size should not have changed.", size, map.size() );

		// Remove an existing mapping
		final TestEdge remove2 = map.remove( Bk );
		assertEquals( "Did not retrieve the expected value upong key removal.", eAB, remove2 );
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
		final TestEdge ref = map.createValueRef();
		assertNotNull( "Created reference object is null.", ref );
	}
}
