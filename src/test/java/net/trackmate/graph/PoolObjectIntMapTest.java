package net.trackmate.graph;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Random;
import java.util.Set;

import net.trackmate.graph.mempool.ByteMappedElement;

import org.junit.Before;
import org.junit.Test;

public class PoolObjectIntMapTest
{

	private int noEntryValue;

	private PoolObjectIntMap< TestObject, ByteMappedElement > map;

	private ArrayList< TestObject > notIns;

	private ArrayList< TestObject > ins;

	private int[] values;

	@Before
	public void setUp() throws Exception
	{
		final TestObjectPool pool = new TestObjectPool( 10 );
		noEntryValue = -1;
		map = new PoolObjectIntMap< TestObject, ByteMappedElement >( pool, noEntryValue );
		final TestObject A = pool.create().init( 1 );
		final TestObject B = pool.create().init( 2 );
		final TestObject C = pool.create().init( 3 );
		final TestObject E = pool.create().init( 4 );
		final TestObject D = pool.create().init( 5 );
		final TestObject F = pool.create().init( 6 );
		final TestObject G = pool.create().init( 7 );

		notIns = new ArrayList< TestObject >( 7 );
		notIns.add( E );
		notIns.add( F );
		notIns.add( G );

		ins = new ArrayList< TestObject >( 4 );
		ins.add( A );
		ins.add( B );
		ins.add( C );
		ins.add( D );

		/*
		 * We map objects to 100 + their id. Only add 4 objects.
		 */
		values = new int[] { 101, 102, 103, 104 };
		int index = 0;
		for ( final TestObject o : ins )
		{
			map.put( o, values[ index++ ] );
		}
	}

	@Test
	public void testClear()
	{
		map.clear();
		assertTrue( "Map should be empty after clear().", map.isEmpty() );
	}

	@Test
	public void testContainsKey()
	{
		for ( final TestObject in : ins )
		{
			assertTrue( "Map should contain the key " + in, map.containsKey( in ) );
		}
		for ( final TestObject out : notIns )
		{
			assertFalse( "Map sould not contain the key " + out, map.containsKey( out ) );
		}
	}

	@Test
	public void testContainsValue()
	{
		for ( final int val : values )
		{
			assertTrue( "Map should contain the value " + val, map.containsValue( val ) );
		}
		final Random rand = new Random();
		for ( int i = 0; i < 30; i++ )
		{
			final int val = 1000 + rand.nextInt( 1000 );
			assertFalse( "Map should not contain the value " + val, map.containsValue( val ) );
		}
	}

	@Test
	public void testGet()
	{
		int index = 0;
		for ( final TestObject in : ins )
		{
			assertEquals( "Did not retrieve the expected value for key " + in, values[ index++ ], map.get( in ) );
		}
	}

	@Test
	public void testIsEmpty()
	{
		final TestObjectPool pool = new TestObjectPool( 10 );
		final PoolObjectIntMap< TestObject, ByteMappedElement > lmap = new PoolObjectIntMap< TestObject, ByteMappedElement >( pool, noEntryValue );
		assertTrue( "Newly created map should be empty.", lmap.isEmpty() );
	}

	@Test
	public void testKeySet()
	{
		final Set< TestObject > keySet = map.keySet();
		for ( final TestObject in : ins )
		{
			assertTrue( "Did not find expected key " + in + " in key set.", keySet.contains( in ) );
		}
		assertEquals( "Key set does not have the expected size.", ins.size(), keySet.size() );

		// Test deletion by proxy
		final TestObject target = ins.get( 1 );
		final boolean removed = keySet.remove( target );
		assertTrue( "Could not remove key " + target + " from key set.", removed );
		assertFalse( "After removal from key set, map should not contain correspinding mapping.", map.containsKey( target ) );
	}

	@Test
	public void testPut()
	{
		for ( final TestObject toPut : notIns )
		{
			map.put( toPut, 99 );
		}
		assertEquals( "After adding new mappings, the map does not report the expected size.", ins.size() + notIns.size(), map.size() );
		for ( final TestObject in : ins )
		{
			assertTrue( "Map should contain the key " + in, map.containsKey( in ) );
		}
		for ( final TestObject out : notIns )
		{
			assertTrue( "Map should contain the key " + out, map.containsKey( out ) );
		}
	}

	@Test
	public void testPutIfAbsent()
	{
		// Try to put an existing key.
		final TestObject existing = ins.get( 2 );
		final int val = map.get( existing );
		final int current = map.putIfAbsent( existing, 500 );
		assertEquals( "Value returned by putIfAbsent is unexpected.", val, current );
		// This should not have changed the stored value
		final int newV                        al = map.get( existing );
	}

	@Test
	public void testPutAllMapOfQextendsKQextendsInteger()
	{
		fail( "Not yet implemented" );
	}

	@Test
	public void testRemove()
	{
		fail( "Not yet implemented" );
	}

	@Test
	public void testGetNoEntryValue()
	{
		fail( "Not yet implemented" );
	}

	@Test
	public void testSize()
	{
		fail( "Not yet implemented" );
	}

	@Test
	public void testValues()
	{
		fail( "Not yet implemented" );
	}

	@Test
	public void testValuesIntArray()
	{
		fail( "Not yet implemented" );
	}

	@Test
	public void testValueCollection()
	{
		fail( "Not yet implemented" );
	}

	@Test
	public void testPutAllTObjectIntMapOfQextendsK()
	{
		fail( "Not yet implemented" );
	}

	@Test
	public void testKeys()
	{
		fail( "Not yet implemented" );
	}

	@Test
	public void testKeysKArray()
	{
		fail( "Not yet implemented" );
	}

	@Test
	public void testIterator()
	{
		fail( "Not yet implemented" );
	}

	@Test
	public void testIncrement()
	{
		fail( "Not yet implemented" );
	}

	@Test
	public void testAdjustValue()
	{
		fail( "Not yet implemented" );
	}

	@Test
	public void testAdjustOrPutValue()
	{
		fail( "Not yet implemented" );
	}

	@Test
	public void testForEachKeyTObjectProcedureOfQsuperKK()
	{
		fail( "Not yet implemented" );
	}

	@Test
	public void testForEachKeyTObjectProcedureOfQsuperK()
	{
		fail( "Not yet implemented" );
	}

	@Test
	public void testForEachValue()
	{
		fail( "Not yet implemented" );
	}

	@Test
	public void testForEachEntryTObjectIntProcedureOfQsuperKK()
	{
		fail( "Not yet implemented" );
	}

	@Test
	public void testForEachEntryTObjectIntProcedureOfQsuperK()
	{
		fail( "Not yet implemented" );
	}

	@Test
	public void testTransformValues()
	{
		fail( "Not yet implemented" );
	}

	@Test
	public void testRetainEntriesTObjectIntProcedureOfQsuperKK()
	{
		fail( "Not yet implemented" );
	}

	@Test
	public void testRetainEntriesTObjectIntProcedureOfQsuperK()
	{
		fail( "Not yet implemented" );
	}

	@Test
	public void testCreateRef()
	{
		fail( "Not yet implemented" );
	}

	@Test
	public void testReleaseRef()
	{
		fail( "Not yet implemented" );
	}

}
