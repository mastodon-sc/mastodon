package net.trackmate.graph;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import gnu.trove.TIntCollection;
import gnu.trove.function.TIntFunction;
import gnu.trove.iterator.TObjectIntIterator;
import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.procedure.TIntProcedure;
import gnu.trove.procedure.TObjectIntProcedure;
import gnu.trove.procedure.TObjectProcedure;
import net.trackmate.collection.ref.RefIntHashMap;

public class PoolObjectIntMapTest
{

	private int noEntryValue;

	private RefIntHashMap< TestObject > map;

	private ArrayList< TestObject > notIns;

	private ArrayList< TestObject > ins;

	private int[] values;

	@Before
	public void setUp() throws Exception
	{
		final TestObjectPool pool = new TestObjectPool( 10 );
		noEntryValue = -1;
		map = new RefIntHashMap< TestObject >( pool, noEntryValue );
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
		final RefIntHashMap< TestObject > lmap = new RefIntHashMap< TestObject >( pool, noEntryValue );
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
	public void testKeys()
	{
		final Object[] keys = map.keys();
		for ( final Object key : keys )
		{
			assertTrue( "Did not find the returned key " + key + " in object list the map was built with.", ins.contains( key ) );
		}
		assertEquals( "Key set does not have the expected size.", ins.size(), keys.length );
	}

	@Test
	public void testKeysKArray()
	{
		final TestObject[] array = new TestObject[ 2 * ins.size() ];
		final TestObject[] keys = map.keys( array );
		for ( int i = 0; i < map.size(); i++ )
		{
			final TestObject key = keys[ i ];
			assertTrue( "Did not find the returned key " + key + " in object list the map was built with.", ins.contains( key ) );
		}
		for ( int i = map.size(); i < keys.length; i++ )
		{
			assertNull( "Remaining elements should be null.", keys[ i ] );
		}
		assertEquals( "Key set does not have the expected size.", 2 * ins.size(), keys.length );
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
		final int newVal = map.get( existing );
		assertEquals( "Value should not have changed since a mapping was present.", val, newVal );

		// Try to put a new key
		final TestObject newKey = notIns.get( 1 );
		final int targetVal = 1000;
		final int newCurrent = map.putIfAbsent( newKey, targetVal );
		assertEquals( "Should have returned the 'no_entry_value' for non existing mapping.", map.getNoEntryValue(), newCurrent );
		final int newVal2 = map.get( newKey );
		assertEquals( "New mapping should have the desired value now.", targetVal, newVal2 );
	}

	@Test
	public void testPutAllMapOfQextendsKQextendsInteger()
	{
		final Map< TestObject, Integer > nmap = new HashMap< TestObject, Integer >( notIns.size() + 1 );
		final int targetVal = 1000;
		for ( final TestObject toPut : notIns )
		{
			nmap.put( toPut, Integer.valueOf( targetVal ) );
		}
		final TestObject existing = ins.get( 1 );
		nmap.put( existing, Integer.valueOf( targetVal ) );

		map.putAll( nmap );
		assertEquals( "Map does not have the expected size after putAll.", ins.size() + notIns.size(), map.size() );
		for ( final TestObject in : ins )
		{
			assertTrue( "Map should contain a mapping for " + in, map.containsKey( in ) );
		}
		for ( final TestObject in : notIns )
		{
			assertTrue( "Map should contain a mapping for " + in, map.containsKey( in ) );
		}

		for ( final TestObject nmkey : nmap.keySet() )
		{
			assertEquals( "Mappings from extra map have unexpected values.", targetVal, map.get( nmkey ) );
		}
	}

	@Test
	public void testPutAllTObjectIntMapOfQextendsK()
	{
		final TObjectIntHashMap< TestObject > nmap = new TObjectIntHashMap< TestObject >();
		final int targetVal = 1000;
		for ( final TestObject toPut : notIns )
		{
			nmap.put( toPut, Integer.valueOf( targetVal ) );
		}
		final TestObject existing = ins.get( 1 );
		nmap.put( existing, Integer.valueOf( targetVal ) );

		map.putAll( nmap );
		assertEquals( "Map does not have the expected size after putAll.", ins.size() + notIns.size(), map.size() );
		for ( final TestObject in : ins )
		{
			assertTrue( "Map should contain a mapping for " + in, map.containsKey( in ) );
		}
		for ( final TestObject in : notIns )
		{
			assertTrue( "Map should contain a mapping for " + in, map.containsKey( in ) );
		}

		for ( final TestObject nmkey : nmap.keySet() )
		{
			assertEquals( "Mappings from extra map have unexpected values.", targetVal, map.get( nmkey ) );
		}
	}

	@Test
	public void testRemove()
	{
		final int tindex = 1;
		final TestObject target = ins.get( tindex );
		final int val = map.remove( target );
		assertEquals( "Map does not have the expected size after removal.", ins.size() - 1, map.size() );
		assertEquals( "Unexpected value returned by removal.", values[ tindex ], val );

		final TestObject notHere = notIns.get( 1 );
		final int remove = map.remove( notHere );
		assertEquals( "Map size should not have changed after trying to remove a non preset mapping.", ins.size() - 1, map.size() );
		assertEquals( "Unexpected value returned by removal of a non present mapping.", map.getNoEntryValue(), remove );
	}

	@Test
	public void testGetNoEntryValue()
	{
		final TestObjectPool pool = new TestObjectPool( 10 );
		final int lNoEntryValue = 1000;
		final RefIntHashMap< TestObject > lmap = new RefIntHashMap< TestObject >( pool, lNoEntryValue );
		assertEquals( "Unexpected 'no_entry_value'.", lNoEntryValue, lmap.getNoEntryValue() );
	}

	@Test
	public void testSize()
	{
		assertEquals( "Map does not report the expected size.", ins.size(), map.size() );
		for ( final TestObject nin : notIns )
		{
			map.put( nin, 1000 );
		}
		assertEquals( "Map does not report the expected size.", ins.size() + notIns.size(), map.size() );
	}

	@Test
	public void testValues()
	{
		final int[] vals = map.values();
		assertEquals( "Array returned by values() does not have the expected size.", ins.size(), vals.length );

		Arrays.sort( vals );
		for ( final int val : values )
		{
			final int index = Arrays.binarySearch( vals, val );
			assertTrue( "Could not find expected value " + val + " in the array returned by values().", index >= 0 );
		}
	}

	@Test
	public void testValuesIntArray()
	{
		int[] vals = new int[ values.length * 2 ];
		vals = map.values( vals );
		assertEquals( "Array returned by values() does not have the expected size.", values.length * 2, vals.length );

		for ( int i = values.length; i < vals.length; i++ )
		{
			assertEquals( "End of array returned by values() should have 0-value.", 0, vals[ i ] );
		}

		Arrays.sort( vals );
		for ( final int val : values )
		{
			final int index = Arrays.binarySearch( vals, val );
			assertTrue( "Could not find expected value " + val + " in the array returned by values().", index >= 0 );
		}
	}

	@Test
	public void testValueCollection()
	{
		final TIntCollection valueCollection = map.valueCollection();
		assertEquals( "valueCollection does not have the expected size.", map.size(), valueCollection.size() );

		Arrays.sort( values );
		for ( final int val : valueCollection.toArray() )
		{
			final int index = Arrays.binarySearch( values, val );
			assertTrue( "Could not find expected value " + val + " in the valueCollection().", index >= 0 );
		}

		// Modify the value collection
		final TestObject target = ins.get( 1 );
		final int tval = map.get( target );
		final boolean removed = valueCollection.remove( tval );
		assertTrue( "Could not remove a value from the valueCollection().", removed );
		assertFalse( "After removal of value from the value collection, mapping should not be present in the map anymore.", map.containsKey( target ) );
		valueCollection.clear();
		assertTrue( "Map should be empty after clearing its value collection.", map.isEmpty() );
	}

	@Test
	public void testIterator()
	{
		final TObjectIntIterator< TestObject > it = map.iterator();
		while ( it.hasNext() )
		{
			it.advance();
			final TestObject key = it.key();
			final int value = it.value();

			final boolean removed = ins.remove( key );
			assertTrue( "The iterator returns a key not present a map.", removed );
			final int eval = map.get( key );
			assertEquals( "The iterator does not return the expected value for the iterated key.", eval, value );
		}
		assertTrue( "The iterator did not iterate over all the map keys.", ins.isEmpty() );
	}

	@Test
	public void testIncrement()
	{
		for ( final TestObject key : map.keySet() )
		{
			final int pval = map.get( key );
			map.increment( key );
			assertEquals( "Value for key " + key + " did not increment.", pval + 1, map.get( key ) );
		}
	}

	@Test
	public void testAdjustValue()
	{
		final int amount = new Random().nextInt( 1000 );
		for ( final TestObject key : map.keySet() )
		{
			final int pval = map.get( key );
			map.adjustValue( key, amount );
			assertEquals( "Value for key " + key + " was not correctly adjusted.", pval + amount, map.get( key ) );
		}
	}

	@Test
	public void testAdjustOrPutValue()
	{
		final int amount = new Random().nextInt( 1000 );
		final int putAmount = -10 - new Random().nextInt( 1000 );

		final Collection< TestObject > all = new ArrayList< TestObject >( ins );
		all.addAll( notIns );

		for ( final TestObject key : all )
		{
			map.adjustOrPutValue( key, amount, putAmount );
		};

		int index = 0;
		for ( final TestObject key : ins )
		{
			final int pval = values[ index++ ];
			assertEquals( "Value for key " + key + " was not correctly adjusted.", pval + amount, map.get( key ) );
		}
		for ( final TestObject key : notIns )
		{
			assertEquals( "Value for new key " + key + " was not correctly adjusted.", putAmount, map.get( key ) );
		}
	}


	@Test
	public void testForEachEntryTObjectIntProcedureOfQsuperK()
	{
		final TObjectIntProcedure< TestObject > procedure = new TObjectIntProcedure< TestObject >()
		{
			@Override
			public boolean execute( final TestObject key, final int val )
			{
				final int eval = map.get( key );
				assertEquals( "Value passed to the procedure is not the right one.", eval, val );
				// Switch it.
				map.put( key, -val );
				return true;
			}
		};
		final boolean ok = map.forEachEntry( procedure );
		assertTrue( "Procedure should have concluded successfully.", ok );

		int index = 0;
		for ( final TestObject in : ins )
		{
			assertEquals( "Did not retrieve the expected value for key " + in, -values[ index++ ], map.get( in ) );
		}
	}

	@Test
	public void testForEachValue()
	{
		final TIntProcedure procedure = new TIntProcedure()
		{
			@Override
			public boolean execute( final int val )
			{

				final int index = Arrays.binarySearch( values, val );
				assertTrue( "Procedure is passed an unexpected value.", index >= 0 );
				return true;
			}
		};
		Arrays.sort( values );
		final boolean ok = map.forEachValue( procedure );
		assertTrue( "Procedure should have concluded successfully.", ok );
	}

	@Test
	public void testForEachEntryTObjectIntProcedureOfQsuperKK()
	{
		final TObjectIntProcedure< TestObject > procedure = new TObjectIntProcedure< TestObject >()
		{
			@Override
			public boolean execute( final TestObject key, final int val )
			{
				final int eval = map.get( key );
				assertEquals( "Value passed to the procedure is not the right one.", eval, val );
				// Switch it.
				map.put( key, -val );
				return true;
			}
		};
		final TestObject ref = map.createRef();
		final boolean ok = map.forEachEntry( procedure, ref );
		assertTrue( "Procedure should have concluded successfully.", ok );

		int index = 0;
		for ( final TestObject in : ins )
		{
			assertEquals( "Did not retrieve the expected value for key " + in, -values[ index++ ], map.get( in ) );
		}
	}

	@Test
	public void testForEachKeyTObjectProcedureOfQsuperKK()
	{
		final TObjectProcedure< TestObject > procedure = new TObjectProcedure< TestObject >()
		{
			@Override
			public boolean execute( final TestObject key )
			{
				assertTrue( "Procedure iterates over keys that are not in the map.", ins.remove( key ) );
				return true;
			}
		};
		final TestObject ref = map.createRef();
		final boolean ok = map.forEachKey( procedure, ref );
		assertTrue( "Procedure should have concluded successfully.", ok );
		assertTrue( "Procedure was not provided all the keys in the map.", ins.isEmpty() );
	}

	@Test
	public void testForEachKeyTObjectProcedureOfQsuperK()
	{
		final TObjectProcedure< TestObject > procedure = new TObjectProcedure< TestObject >()
		{
			@Override
			public boolean execute( final TestObject key )
			{
				assertTrue( "Procedure iterates over keys that are not in the map.", ins.remove( key ) );
				return true;
			}
		};
		final boolean ok = map.forEachKey( procedure );
		assertTrue( "Procedure should have concluded successfully.", ok );
		assertTrue( "Procedure was not provided all the keys in the map.", ins.isEmpty() );
	}

	@Test
	public void testTransformValues()
	{
		final int amount = new Random().nextInt( 1000 );
		final int[] clone = values.clone();
		final TIntFunction function = new TIntFunction()
		{

			@Override
			public int execute( final int val )
			{
				final int index = Arrays.binarySearch( clone, val );
				assertTrue( "Function is passed an unexpected value.", index >= 0 );
				return val + amount;
			}
		};
		Arrays.sort( clone );
		map.transformValues( function );
		int index = 0;
		for ( final TestObject in : ins )
		{
			assertEquals( "Did not retrieve the expected value for key " + in + " after value processing.", values[ index++ ] + amount, map.get( in ) );
		}

	}

	@Test
	public void testRetainEntriesTObjectIntProcedureOfQsuperKK()
	{
		// Retain 1 value
		final TestObject target = ins.get( 1 );
		final int targetVal = map.get( target );
		final TObjectIntProcedure< TestObject > procedure = new TObjectIntProcedure< TestObject >()
		{

			@Override
			public boolean execute( final TestObject key, final int val )
			{
				assertTrue( "Procedure iterates over keys that are not in the map.", ins.contains( key ) );
				return val == targetVal;
			}
		};
		final TestObject ref = map.createRef();
		final boolean changed = map.retainEntries( procedure, ref );
		assertTrue( "Procedure should have changed the map.", changed );
		assertEquals( "After procedure filtering, the map has not the expcted size.", 1, map.size() );
	}

	@Test
	public void testRetainEntriesTObjectIntProcedureOfQsuperK()
	{
		// Retain 1 value
		final TestObject target = ins.get( 1 );
		final int targetVal = map.get( target );
		final TObjectIntProcedure< TestObject > procedure = new TObjectIntProcedure< TestObject >()
		{

			@Override
			public boolean execute( final TestObject key, final int val )
			{
				assertTrue( "Procedure iterates over keys that are not in the map.", ins.contains( key ) );
				return val == targetVal;
			}
		};
		final boolean changed = map.retainEntries( procedure );
		assertTrue( "Procedure should have changed the map.", changed );
		assertEquals( "After procedure filtering, the map has not the expcted size.", 1, map.size() );
	}
}
