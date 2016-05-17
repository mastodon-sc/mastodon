package net.trackmate.graph;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import gnu.trove.TIntCollection;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.procedure.TIntProcedure;
import gnu.trove.set.TIntSet;
import net.trackmate.graph.collection.pool.IntPoolObjectMap;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Before;
import org.junit.Test;

public class IntPoolObjectMapKeySetTest
{

	private TestVertexPool pool;

	private IntPoolObjectMap< TestVertex > map;

	private HashMap< Integer, Integer > truthMap;

	private int[] storedIds;

	private TIntSet keySet;

	@Before
	public void setUp() throws Exception
	{
		pool = new TestVertexPool( 10 );
		truthMap = new HashMap<>( 10 );
		final TestVertex ref = pool.createRef();
		for ( int i = 0; i < 10; i++ )
		{
			final int id = 20 + i;
			final TestVertex a = pool.create( ref ).init( id );
			truthMap.put( Integer.valueOf( a.getId() ), Integer.valueOf( a.getInternalPoolIndex() ) );
		}

		map = new IntPoolObjectMap<>( pool, -1 );
		storedIds = new int[] { 22, 23, 26, 28 };
		for ( final int id : storedIds )
		{
			final Integer poolIndex = truthMap.get( id );
			pool.getByInternalPoolIndex( poolIndex, ref );
			map.put( id, ref );
		}
		pool.releaseRef( ref );
		keySet = map.keySet();
	}

	@Test
	public void testGetNoEntryValue()
	{
		final int noEntryValue = keySet.getNoEntryValue();
		assertTrue( "The no-entry value must be negative.", noEntryValue < 0 );
	}

	@Test
	public void testSize()
	{
		assertEquals( "Keyset does not have the expected size.", map.size(), keySet.size() );
		map.remove( 2 );
		assertEquals( "Keyset does not have the expected size.", map.size(), keySet.size() );
	}

	@Test
	public void testIsEmpty()
	{
		assertFalse( "Keyset should not be empty.", keySet.isEmpty() );
		map.clear();
		assertTrue( "Keyset should be empty after map.clear().", keySet.isEmpty() );
	}

	@Test
	public void testContains()
	{
		assertFalse( "Keyset should not contain the no-entry value.", keySet.contains( map.getNoEntryKey() ) );
		for ( final int i : storedIds )
		{
			assertTrue( "Keyset should contain value " + i, keySet.contains( i ) );
		}
		for ( int i = 100; i < 120; i++ )
		{
			assertFalse( "Keyset should not contain value " + i, keySet.contains( i ) );
		}
		for ( final int i : storedIds )
		{
			map.remove( i );
		}
		for ( final int i : storedIds )
		{
			assertFalse( "Keyset should not contain removed value " + i, keySet.contains( i ) );
		}
	}

	@Test
	public void testIterator()
	{
		// Test iterate in the right order.
		final TIntIterator it = keySet.iterator();
		int count = 0;
		Arrays.sort( storedIds );
		while ( it.hasNext() )
		{
			final int val = it.next();
			final int s = Arrays.binarySearch( storedIds, val );
			assertTrue( "Iterator returns a value not in the keyset: " + val, s >= 0 );
			count++;
		}
		assertEquals( "Iterator did not iterate over the whole collection.", map.size(), count );

		// Test iterator removal.
		// Remove the 3rd whatsoever element.
		final int size = map.size();
		final TIntIterator it2 = keySet.iterator();
		it2.next();
		it2.next();
		final int val = it2.next();
		it2.remove();
		assertEquals( "Map does not have the expected size after removal by keyset iterator.", size - 1, map.size() );
		assertFalse( "Map should not contain a mapping for key " + val + " after removal by keyset iterator.", map.containsKey( val ) );

		// Remove all.
		final TIntIterator it3 = keySet.iterator();
		while ( it3.hasNext() )
		{
			it3.next();
			it3.remove();
		}
		assertTrue( "Map should be empty after removing all content with keyset iterator.", map.isEmpty() );
	}

	@Test
	public void testToArray()
	{
		Arrays.sort( storedIds );
		final int[] array = keySet.toArray();
		for ( int i = 0; i < array.length; i++ )
		{
			final int s = Arrays.binarySearch( storedIds, array[ i ] );
			assertTrue( "Unexpected value for array returned by keysey.toArray().", s >= 0 );
		}
		assertEquals( "Array does not have the expected length.", map.size(), array.length );
	}

	@Test
	public void testToArrayInt()
	{
		final int[] arr = new int[ 100 + keySet.size() ];
		final int[] array = keySet.toArray( arr );
		assertEquals( "The returned array is not the right instance.", arr, array );

		Arrays.sort( storedIds );
		for ( int i = 0; i < storedIds.length; i++ )
		{
			final int s = Arrays.binarySearch( storedIds, array[ i ] );
			assertTrue( "Unexpected value for array returned by keysey.toArray().", s >= 0 );
		}
		for ( int i = storedIds.length; i < arr.length; i++ )
		{
			assertEquals( "Remaining slots should have the map no-entry key.", map.getNoEntryKey(), array[ i ] );
		}
	}

	@Test( expected = UnsupportedOperationException.class )
	public void testAdd()
	{
		keySet.add( 5 );
	}

	@Test
	public void testRemove()
	{
		final int notPresent = 42;
		final boolean removed = keySet.remove( notPresent );
		assertFalse( "Removing a non-present value should not have changed the keyset.", removed );
		final boolean removed2 = keySet.remove( storedIds[ 0 ] );
		assertTrue( "Removing a present value should change the keyset.", removed2 );
		assertFalse( "Map should not contain a mapping for the alue removed in keyset.", map.containsKey( storedIds[ 0 ] ) );
	}

	@Test
	public void testContainsAllCollection()
	{
		final Collection< Integer > allIn = Arrays.asList( new Integer[] { 22, 23, 26, 28 } );
		final Collection< Integer > notIn = Arrays.asList( new Integer[] { 21, 27, 29 } );
		final Collection< Integer > partlyIn = Arrays.asList( new Integer[] { 22, 23, 29 } );
		assertTrue( "This whole collection is contained in the keyset: " + allIn, keySet.containsAll( allIn ) );
		assertFalse( "This collection is not fully contained in the keyset: " + partlyIn, keySet.containsAll( partlyIn ) );
		assertFalse( "This collection is not in the keyset: " + notIn, keySet.containsAll( notIn ) );
	}

	@Test
	public void testContainsAllInt()
	{
		final int[] allIn = new int[] { 22, 23, 26, 28 };
		final int[] notIn = new int[] { 21, 27, 29 };
		final int[] partlyIn = new int[] { 22, 23, 29 };
		assertTrue( "This whole collection is contained in the keyset: " + allIn, keySet.containsAll( allIn ) );
		assertFalse( "This collection is not fully contained in the keyset: " + partlyIn, keySet.containsAll( partlyIn ) );
		assertFalse( "This collection is not in the keyset: " + notIn, keySet.containsAll( notIn ) );
	}

	@Test
	public void testContainsAllTIntCollection()
	{
		final TIntCollection allIn = TIntArrayList.wrap( new int[] { 22, 23, 26, 28 } );
		final TIntArrayList notIn = TIntArrayList.wrap( new int[] { 21, 27, 29 } );
		final TIntArrayList partlyIn = TIntArrayList.wrap( new int[] { 22, 23, 29 } );
		assertTrue( "This whole collection is contained in the keyset: " + allIn, keySet.containsAll( allIn ) );
		assertFalse( "This collection is not fully contained in the keyset: " + partlyIn, keySet.containsAll( partlyIn ) );
		assertFalse( "This collection is not in the keyset: " + notIn, keySet.containsAll( notIn ) );
	}

	@Test( expected = UnsupportedOperationException.class )
	public void testAddAllCollection()
	{
		final Collection< Integer > c = Arrays.asList( new Integer[] { 2, 3, 6, 8 } );
		keySet.addAll( c );
	}

	@Test( expected = UnsupportedOperationException.class )
	public void testAddAllTIntCollection()
	{
		final TIntCollection c = TIntArrayList.wrap( new int[] { 2, 3, 6, 8 } );
		keySet.addAll( c );
	}

	@Test( expected = UnsupportedOperationException.class )
	public void testAddAllInt()
	{
		final int[] c = new int[] { 2, 3, 6, 8 };
		keySet.addAll( c );
	}

	@Test
	public void testRetainAllCollection()
	{
		final Collection< Integer > c = Arrays.asList( new Integer[] { 22, 23, 29 } );
		final boolean changed = keySet.retainAll( c );
		assertTrue( "Removal should have changed the keyset.", changed );
		assertEquals( "Map does not have the expected size after retainAll on keyset.", 2, map.size() );
		assertTrue( "Map should contain mappting for key 2 after retainAll().", map.containsKey( 22 ) );
		assertTrue( "Map should contain mappting for key 3 after retainAll().", map.containsKey( 23 ) );
		assertFalse( "Map should not contain mappting for key 6 after retainAll().", map.containsKey( 26 ) );
		assertFalse( "Map should not contain mappting for key 8 after retainAll().", map.containsKey( 28 ) );
	}

	@Test
	public void testRetainAllTIntCollection()
	{
		final TIntArrayList c = TIntArrayList.wrap( new int[] { 22, 23, 29 } );
		final boolean changed = keySet.retainAll( c );
		assertTrue( "Removal should have changed the keyset.", changed );
		assertEquals( "Map does not have the expected size after retainAll on keyset.", 2, map.size() );
		assertTrue( "Map should contain mappting for key 2 after retainAll().", map.containsKey( 22 ) );
		assertTrue( "Map should contain mappting for key 3 after retainAll().", map.containsKey( 23 ) );
		assertFalse( "Map should not contain mappting for key 6 after retainAll().", map.containsKey( 26 ) );
		assertFalse( "Map should not contain mappting for key 8 after retainAll().", map.containsKey( 28 ) );
	}

	@Test
	public void testRetainAllInt()
	{
		final int[] c = new int[] { 22, 23, 29 };
		final boolean changed = keySet.retainAll( c );
		assertTrue( "Removal should have changed the keyset.", changed );
		assertEquals( "Map does not have the expected size after retainAll on keyset.", 2, map.size() );
		assertTrue( "Map should contain mappting for key 2 after retainAll().", map.containsKey( 22 ) );
		assertTrue( "Map should contain mappting for key 3 after retainAll().", map.containsKey( 23 ) );
		assertFalse( "Map should not contain mappting for key 6 after retainAll().", map.containsKey( 26 ) );
		assertFalse( "Map should not contain mappting for key 8 after retainAll().", map.containsKey( 28 ) );
	}

	@Test
	public void testRemoveAllCollection()
	{
		final Collection< Integer > c = Arrays.asList( new Integer[] { 22, 23, 29 } );
		final boolean changed = keySet.removeAll( c );
		assertTrue( "Removal should have changed the keyset.", changed );
		assertEquals( "Map does not have the expected size after removeAll on keyset.", 2, map.size() );
		assertTrue( "Map should contain mappting for key 6 after removeAll().", map.containsKey( 26 ) );
		assertTrue( "Map should contain mappting for key 8 after removeAll().", map.containsKey( 28 ) );
		assertFalse( "Map should not contain mappting for key 2 after removeAll().", map.containsKey( 22 ) );
		assertFalse( "Map should not contain mappting for key 3 after removeAll().", map.containsKey( 23 ) );
	}

	@Test
	public void testRemoveAllTIntCollection()
	{
		final TIntArrayList c = TIntArrayList.wrap( new int[] { 22, 23, 29 } );
		final boolean changed = keySet.removeAll( c );
		assertTrue( "Removal should have changed the keyset.", changed );
		assertEquals( "Map does not have the expected size after removeAll on keyset.", 2, map.size() );
		assertTrue( "Map should contain mappting for key 6 after removeAll().", map.containsKey( 26 ) );
		assertTrue( "Map should contain mappting for key 8 after removeAll().", map.containsKey( 28 ) );
		assertFalse( "Map should not contain mappting for key 2 after removeAll().", map.containsKey( 22 ) );
		assertFalse( "Map should not contain mappting for key 3 after removeAll().", map.containsKey( 23 ) );
	}

	@Test
	public void testRemoveAllInt()
	{
		final int[] c = new int[] { 22, 23, 29 };
		final boolean changed = keySet.removeAll( c );
		assertTrue( "Removal should have changed the keyset.", changed );
		assertEquals( "Map does not have the expected size after removeAll on keyset.", 2, map.size() );
		assertTrue( "Map should contain mappting for key 6 after removeAll().", map.containsKey( 26 ) );
		assertTrue( "Map should contain mappting for key 8 after removeAll().", map.containsKey( 28 ) );
		assertFalse( "Map should not contain mappting for key 2 after removeAll().", map.containsKey( 22 ) );
		assertFalse( "Map should not contain mappting for key 3 after removeAll().", map.containsKey( 23 ) );
	}

	@Test
	public void testClear()
	{
		keySet.clear();
		assertTrue( "Map should be empty after keyset clear().", map.isEmpty() );
	}

	@Test
	public void testForEach()
	{
		final AtomicInteger ai = new AtomicInteger(0);
		final TIntProcedure proc = new TIntProcedure()
		{
			@Override
			public boolean execute( final int value )
			{
				ai.incrementAndGet();
				assertTrue( "Iterated value is not contained a key in the map.", map.containsKey( value ) );
				return true;
			}
		};
		final boolean ok = keySet.forEach( proc );
		assertTrue( "ForEach procedure should have terminated ok.", ok );
		assertEquals( "All the values have not been iterated through.", map.size(), ai.get() );
	}

}