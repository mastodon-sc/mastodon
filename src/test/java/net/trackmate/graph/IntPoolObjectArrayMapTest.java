package net.trackmate.graph;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;

public class IntPoolObjectArrayMapTest
{

	private TestVertexPool pool;

	private IntPoolObjectArrayMap< TestVertex > map;

	private HashMap< Integer, Integer > truthMap;

	private int[] storedIds;

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

		map = new IntPoolObjectArrayMap<>( pool );
		storedIds = new int[] { 2, 3, 6, 8 };
		for ( final int id : storedIds )
		{
			final Integer poolIndex = truthMap.get( id );
			pool.getByInternalPoolIndex( poolIndex, ref );
			map.put( id, ref );
		}
		pool.releaseRef( ref );
	}

	@Test
	public void testCreateRef()
	{
		map.createRef();
	}

	@Test
	public void testClear()
	{
		map.clear();
		assertTrue( "Map should be empty after clear().", map.isEmpty() );
		assertEquals( "Map should be of 0-size after clear().", 0, map.size() );
		final TestVertex ref = map.createRef();
		for ( int i = 0; i < 10; i++ )
		{
			final TestVertex vertex = map.get( i, ref );
			assertNull( "There should not be a mapping for key " + i + " after clear().", vertex );
		}
		map.releaseRef( ref );
	}

	@Test
	public void testGetInt()
	{
		final TestVertex ref = map.createRef();
		for ( final int id : storedIds )
		{

			final TestVertex vactual = map.get( id );
			final Integer poolIndex = truthMap.get( id );
			pool.getByInternalPoolIndex( poolIndex, ref );
			assertEquals( "Unexpected mapping for key " + id, ref, vactual );
		}
		map.releaseRef( ref );
	}

	@Test
	public void testGetIntV()
	{
		final TestVertex ref1 = map.createRef();
		final TestVertex ref2 = map.createRef();
		for ( final int id : storedIds )
		{

			final TestVertex vactual = map.get( id, ref1 );
			final Integer poolIndex = truthMap.get( id );
			pool.getByInternalPoolIndex( poolIndex, ref2 );
			assertEquals( "Unexpected mapping for key " + id, ref2, vactual );
		}
		map.releaseRef( ref1 );
		map.releaseRef( ref2 );
	}

	@Test
	public void testIsEmpty()
	{
		assertTrue( "Full map should not be empty.", !map.isEmpty() );
		for ( final int id : storedIds )
		{
			map.remove( id );
		}
		assertTrue( "Emptied map should be empty.", map.isEmpty() );
		assertTrue( "New map should be empty.", new IntPoolObjectArrayMap<>( pool ).isEmpty() );
	}

	@Test
	public void testPutIntV()
	{
		final int key = 5;
		assertTrue( "Map should not yet contain a mapping for key " + key, !map.containsKey( key ) );

		final Integer poolIndex = truthMap.get( key );
		final TestVertex ref = map.createRef();
		pool.getByInternalPoolIndex( poolIndex, ref );
		final TestVertex put = map.put( key, ref );
		map.releaseRef( ref );
		assertNull( "There should not be a previous mapping for key " + key, put );
		assertTrue( "Map should now contain a mapping for key " + key, map.containsKey( key ) );
	}

	@Test
	public void testPutIntVV()
	{
		final int key = 5;
		assertTrue( "Map should not yet contain a mapping for key " + key, !map.containsKey( key ) );

		final Integer poolIndex = truthMap.get( key );
		final TestVertex ref1 = map.createRef();
		final TestVertex ref2 = map.createRef();
		pool.getByInternalPoolIndex( poolIndex, ref1 );
		final TestVertex put = map.put( key, ref1, ref2 );
		map.releaseRef( ref1 );
		map.releaseRef( ref2 );
		assertNull( "There should not be a previous mapping for key " + key, put );
		assertTrue( "Map should now contain a mapping for key " + key, map.containsKey( key ) );
	}

	@Test
	public void testRemoveInt()
	{
		final int key = 6;
		final int size = map.size();
		assertTrue( "Map should contain a mapping for key " + key, map.containsKey( key ) );
		final TestVertex removed = map.remove( key );
		assertNotNull( "Object removed by existing mapping should not be null.", removed );
		assertTrue( "Map should not contain a mapping for removed key " + key, !map.containsKey( key ) );
		assertEquals( "Map size should have shrunk by 1 after removal.", size - 1, map.size() );

		final TestVertex removed2 = map.remove( key );
		assertNull( "Object removed by non-existing mapping should be null.", removed2 );
		assertEquals( "Map size should not have shrunk by 1 after removal of non-existing mapping.", size - 1, map.size() );
	}

	@Test
	public void testRemoveIntV()
	{
		final int key = 6;
		final int size = map.size();
		assertTrue( "Map should contain a mapping for key " + key, map.containsKey( key ) );
		final TestVertex ref = map.createRef();
		final TestVertex removed = map.remove( key, ref );
		assertNotNull( "Object removed by existing mapping should not be null.", removed );
		assertTrue( "Map should not contain a mapping for removed key " + key, !map.containsKey( key ) );
		assertEquals( "Map size should have shrunk by 1 after removal.", size - 1, map.size() );

		final TestVertex removed2 = map.remove( key, ref );
		assertNull( "Object removed by non-existing mapping should be null.", removed2 );
		assertEquals( "Map size should not have shrunk by 1 after removal of non-existing mapping.", size - 1, map.size() );
		map.releaseRef( ref );
	}

	@Test
	public void testSize()
	{
		assertEquals( "Unexpected map size.", storedIds.length, map.size() );
		final int[] toAdd = new int[] { 4, 5 };
		final TestVertex ref1 = map.createRef();
		final TestVertex ref2 = map.createRef();
		for ( final int add : toAdd )
		{
			final int poolIndex = truthMap.get( add );
			pool.getByInternalPoolIndex( poolIndex, ref1 );
			map.put( add, ref1, ref2 );
		}
		map.releaseRef( ref1 );
		map.releaseRef( ref2 );
		assertEquals( "Unexpected map size after addition.", storedIds.length + toAdd.length, map.size() );
		assertEquals( "Unexpected new map size.", 0, new IntPoolObjectArrayMap<>( pool ).size() );
	}

	@Test
	public void testGetNoEntryKey()
	{
		final int noEntryKey = map.getNoEntryKey();
		assertTrue( "The no entry key should be negative.", noEntryKey < 0 );
	}

	@Test
	public void testContainsKey()
	{
		for ( final int key : storedIds )
		{
			assertTrue( "The map should contain a mapping for key " + key, map.containsKey( key ) );
		}
		for ( final Integer key : truthMap.keySet() )
		{
			if ( Arrays.binarySearch( storedIds, key ) < 0 )
			{
				assertFalse( "The map should not contain a mapping for key " + key, map.containsKey( key ) );
			}
			
		}
	}

	@Test
	public void testContainsValue()
	{
		fail( "Not yet implemented" );
	}

	@Test
	public void testPutIfAbsent()
	{
		fail( "Not yet implemented" );
	}

	@Test
	public void testPutAllMapOfQextendsIntegerQextendsV()
	{
		fail( "Not yet implemented" );
	}

	@Test
	public void testPutAllTIntObjectMapOfQextendsV()
	{
		fail( "Not yet implemented" );
	}

	@Test
	public void testKeySet()
	{
		fail( "Not yet implemented" );
	}

	@Test
	public void testKeys()
	{
		fail( "Not yet implemented" );
	}

	@Test
	public void testKeysIntArray()
	{
		fail( "Not yet implemented" );
	}

	@Test
	public void testValueCollection()
	{
		fail( "Not yet implemented" );
	}

	@Test
	public void testValues()
	{
		fail( "Not yet implemented" );
	}

	@Test
	public void testValuesVArray()
	{
		fail( "Not yet implemented" );
	}

	@Test
	public void testIterator()
	{
		fail( "Not yet implemented" );
	}

	@Test
	public void testForEachKey()
	{
		fail( "Not yet implemented" );
	}

	@Test
	public void testForEachValue()
	{
		fail( "Not yet implemented" );
	}

	@Test
	public void testForEachEntry()
	{
		fail( "Not yet implemented" );
	}

	@Test
	public void testTransformValues()
	{
		fail( "Not yet implemented" );
	}

	@Test
	public void testRetainEntries()
	{
		fail( "Not yet implemented" );
	}

}
