package org.mastodon.collection.ref;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import org.junit.Before;
import org.junit.Test;
import org.mastodon.collection.ref.IntRefHashMap;
import org.mastodon.collection.ref.RefArrayList;
import org.mastodon.collection.ref.RefSetImp;
import org.mastodon.graph.TestVertex;
import org.mastodon.graph.TestVertexPool;

public class IntRefHashMapValuesCollectionTest
{

	private TestVertexPool pool;

	private IntRefHashMap< TestVertex > map;

	private HashMap< Integer, Integer > truthMap;

	private int[] storedIds;

	private Collection< TestVertex > valueCollection;

	@Before
	public void setUp() throws Exception
	{
		pool = new TestVertexPool( 10 );
		truthMap = new HashMap<>( 10 );
		final TestVertex ref = pool.createRef();
		for ( int i = 0; i < 10; i++ )
		{
			final TestVertex a = pool.create( ref ).init( i );
			truthMap.put( Integer.valueOf( a.getId() ), Integer.valueOf( a.getInternalPoolIndex() ) );
		}

		map = new IntRefHashMap<>( pool, -1 );
		storedIds = new int[] { 2, 3, 6, 8 };
		for ( final int id : storedIds )
		{
			final Integer poolIndex = truthMap.get( id );
			pool.getObject( poolIndex, ref );
			map.put( id, ref );
		}
		pool.releaseRef( ref );
		valueCollection = map.valueCollection();
	}

	@Test( expected = UnsupportedOperationException.class )
	public void testAdd()
	{
		final TestVertex v = pool.create( pool.createRef() ).init( 100 );
		valueCollection.add( v );
	}

	@Test( expected = UnsupportedOperationException.class )
	public void testAddAll()
	{
		final RefSetImp< TestVertex > set = new RefSetImp<>( pool );
		final TestVertex ref = pool.createRef();
		for ( int i = 0; i < 5; i++ )
		{
			set.add( pool.create( ref ).init( 100 + i ) );
		}
		pool.releaseRef( ref );
		valueCollection.addAll( set );
	}

	@Test
	public void testClear()
	{
		valueCollection.clear();
		assertTrue( "Map should be empty after valueCollection.clear().", map.isEmpty() );
	}

	@Test
	public void testContains()
	{
		final TestVertex ref = pool.createRef();
		for ( final int id : storedIds )
		{
			final Integer poolIndex = truthMap.get( id );
			pool.getObject( poolIndex, ref );
			assertTrue( "ValueCollection should contain the object " + ref, valueCollection.contains( ref ) );
		}
		for ( final int id : new int[] { 0, 1, 4, 5, 7, 9 } )
		{
			final Integer poolIndex = truthMap.get( id );
			pool.getObject( poolIndex, ref );
			assertFalse( "ValueCollection should not contain the object " + ref, valueCollection.contains( ref ) );
		}
	}

	@Test
	public void testContainsAll()
	{
		final int[] allIn = storedIds;
		testContainsAllFromKeys( allIn, true );
		final int[] allOut = new int[] { 0, 1, 4, 5, 7, 9 };
		testContainsAllFromKeys( allOut, false );
		final int[] partIn = new int[] { 2, 3, 4, 5, 7, 9 };
		testContainsAllFromKeys( partIn, false );
	}

	private void testContainsAllFromKeys( final int[] keys, final boolean expected )
	{
		final boolean containsAll = valueCollection.containsAll( createListFromKeys( keys ) );
		if ( expected )
		{
			assertTrue( "Tested collection should be contained in the valueCondition.", containsAll );
		}
		else
		{
			assertFalse( "Tested collection should not be contained in the valueCondition.", containsAll );
		}
	}

	@Test
	public void testIsEmpty()
	{
		assertFalse( "ValueCollection should not be empty.", valueCollection.isEmpty() );
		valueCollection.clear();
		assertTrue( "ValueCollection should be empty after clear().", valueCollection.isEmpty() );
		assertTrue( "ValueCollection from new map should be empty.", new IntRefHashMap<>( pool, -1 ).valueCollection().isEmpty() );
	}

	@Test
	public void testIterator()
	{
		// Test iterate in the right order.
		final Iterator< TestVertex > it = valueCollection.iterator();
		int count = 0;
		while ( it.hasNext() )
		{
			final TestVertex actual = it.next();
			assertTrue( "Iterator returns unexpected value: " + actual, map.containsValue( actual ) );
			count++;
		}
		assertEquals( "Iterator did not iterate through all the values.", map.size(), count );

		// Test iterator removal.
		// Remove the 3rd whatsoever value.
		final int size = map.size();
		final Iterator< TestVertex > it2 = valueCollection.iterator();
		it2.next();
		it2.next();
		final TestVertex val = it2.next();
		it2.remove();
		assertEquals( "Map does not have the expected size after removal by keyset iterator.", size - 1, map.size() );
		assertFalse( "Map should not contain a mapping for key " + val + " after removal by keyset iterator.", map.containsValue( val ) );

		// Remove all.
		final Iterator< TestVertex > it3 = valueCollection.iterator();
		while ( it3.hasNext() )
		{
			it3.next();
			it3.remove();
		}
		assertTrue( "Map should be empty after removing all content with keyset iterator.", map.isEmpty() );
	}

	@Test
	public void testRemoveAll()
	{
		final int[] allOut = new int[] { 0, 1, 4, 5, 7, 9 };
		boolean changed = removeAllFromKeys( allOut );
		assertFalse( "Removing non-present values should not have changed the valueCollection.", changed );

		final int[] partIn = new int[] { 2, 3, 4, 5, 7, 9 };
		changed = removeAllFromKeys( partIn );
		assertTrue( "Removing some present values should have changed the valueCollection.", changed );
		assertEquals( "Map does not have the expected size after valueCollection.removeAll.", 2, map.size() );

		final int[] allIn = storedIds;
		changed = removeAllFromKeys( allIn );
		assertTrue( "Removing all present values should have changed the valueCollection.", changed );
		assertTrue( "Map should be empty after removing all values.", map.isEmpty() );
	}

	private boolean removeAllFromKeys( final int[] keys )
	{
		return valueCollection.removeAll( createListFromKeys( keys ) );
	}

	@Test
	public void testRetainAll()
	{
		final int[] allIn = storedIds;
		boolean changed = retainAllFromKeys( allIn );
		assertFalse( "Retaining all present values should have not changed the valueCollection.", changed );

		final int[] partIn = new int[] { 2, 3, 4, 5, 7, 9 };
		changed = retainAllFromKeys( partIn );
		assertTrue( "Retaining only some present values should have changed the valueCollection.", changed );
		assertEquals( "Map does not have the expected size after valueCollection.retainAll.", 2, map.size() );

		final int[] allOut = new int[] { 0, 1, 4, 5, 7, 9 };
		changed = retainAllFromKeys( allOut );
		assertTrue( "Retaining only non-present values should have changed the valueCollection.", changed );
		assertTrue( "The map should be empty after retraining none of its values.", map.isEmpty() );
	}

	private boolean retainAllFromKeys( final int[] keys )
	{
		return valueCollection.retainAll( createListFromKeys( keys ) );
	}

	@Test
	public void testSize()
	{
		assertEquals( "ValueCollection and the map do not have the same size.", map.size(), valueCollection.size() );
		final TestVertex ref = pool.createRef();
		for ( int i = 0; i < 5; i++ )
		{
			map.put( 100 + i, ref );
		}
		assertEquals( "ValueCollection and the map do not have the same size.", map.size(), valueCollection.size() );
		map.clear();
		assertEquals( "ValueCollection and the map do not have the same size.", map.size(), valueCollection.size() );
	}

	@Test
	public void testToArray()
	{
		final Object[] array = valueCollection.toArray();
		assertEquals( "Created array does not have the expected length.", valueCollection.size(), array.length );
		for ( final Object obj : array )
		{
			assertTrue( "Unexpected object in the array returned by toArray(): ", map.containsValue( obj ) );
		}
	}
	
	@Test
	public void testToArrayArray()
	{
		final TestVertex[] arr = new TestVertex[ 100 ];
		// Initialize it with non-null values.
		final TestVertex v = pool.create(pool.createRef()).init( 100 );
		for ( int i = 0; i < arr.length; i++ )
		{
			arr[ i ] = v;
		}

		final TestVertex[] array = valueCollection.toArray( arr );
		for ( int i = 0; i < map.size(); i++ )
		{
			assertTrue( "Unexpected object in the array returned by toArray(): " + array[ i ], map.containsValue( array[ i ] ) );
		}
		for ( int j = map.size(); j < array.length; j++ )
		{
			assertNull( "Remaining array slots should be null.", array[ j ] );
		}
	}

	private RefArrayList< TestVertex > createListFromKeys( final int[] keys )
	{
		final RefArrayList< TestVertex > set = new RefArrayList<>( pool );
		final TestVertex ref = pool.createRef();
		for ( final int key : keys )
		{
			final int poolIndex = truthMap.get( key );
			pool.getObject( poolIndex, ref );
			set.add( ref );
		}
		return set;
	}
}
