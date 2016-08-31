package org.mastodon.collection.ref;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import gnu.trove.function.TObjectFunction;
import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.procedure.TIntObjectProcedure;
import gnu.trove.procedure.TIntProcedure;
import gnu.trove.procedure.TObjectProcedure;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Before;
import org.junit.Test;
import org.mastodon.collection.ref.IntRefArrayMap;
import org.mastodon.graph.TestVertex;
import org.mastodon.graph.TestVertexPool;

public class IntRefArrayMapTest
{

	private TestVertexPool pool;

	private IntRefArrayMap< TestVertex > map;

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
			final int id = 20 + i;
			final TestVertex a = pool.create( ref ).init( id );
			truthMap.put( Integer.valueOf( a.getId() ), Integer.valueOf( a.getInternalPoolIndex() ) );
		}

		map = new IntRefArrayMap<>( pool );
		storedIds = new int[] { 22, 23, 26, 28 };
		for ( final int id : storedIds )
		{
			final Integer poolIndex = truthMap.get( id );
			pool.getObject( poolIndex, ref );
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
			pool.getObject( poolIndex, ref );
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
			pool.getObject( poolIndex, ref2 );
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
		assertTrue( "New map should be empty.", new IntRefArrayMap<>( pool ).isEmpty() );
	}

	@Test
	public void testPutIntV()
	{
		final int key = 25;
		assertTrue( "Map should not yet contain a mapping for key " + key, !map.containsKey( key ) );

		final Integer poolIndex = truthMap.get( key );
		final TestVertex ref = map.createRef();
		pool.getObject( poolIndex, ref );
		final TestVertex put = map.put( key, ref );
		map.releaseRef( ref );
		assertNull( "There should not be a previous mapping for key " + key, put );
		assertTrue( "Map should now contain a mapping for key " + key, map.containsKey( key ) );
	}

	@Test
	public void testPutIntVV()
	{
		final int key = 25;
		assertTrue( "Map should not yet contain a mapping for key " + key, !map.containsKey( key ) );

		final Integer poolIndex = truthMap.get( key );
		final TestVertex ref1 = map.createRef();
		final TestVertex ref2 = map.createRef();
		pool.getObject( poolIndex, ref1 );
		final TestVertex put = map.put( key, ref1, ref2 );
		map.releaseRef( ref1 );
		map.releaseRef( ref2 );
		assertNull( "There should not be a previous mapping for key " + key, put );
		assertTrue( "Map should now contain a mapping for key " + key, map.containsKey( key ) );
	}

	@Test
	public void testRemoveInt()
	{
		final int key = 26;
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
		final int key = 26;
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
		final int[] toAdd = new int[] { 24, 25 };
		final TestVertex ref1 = map.createRef();
		final TestVertex ref2 = map.createRef();
		for ( final int add : toAdd )
		{
			final int poolIndex = truthMap.get( add );
			pool.getObject( poolIndex, ref1 );
			map.put( add, ref1, ref2 );
		}
		map.releaseRef( ref1 );
		map.releaseRef( ref2 );
		assertEquals( "Unexpected map size after addition.", storedIds.length + toAdd.length, map.size() );
		assertEquals( "Unexpected new map size.", 0, new IntRefArrayMap<>( pool ).size() );
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
		final TestVertex ref = pool.createRef();
		for ( final int id : storedIds )
		{
			final Integer poolIndex = truthMap.get( id );
			pool.getObject( poolIndex, ref );
			assertTrue( "Map should contain the value " + ref, map.containsValue( ref ) );
		}
		Arrays.sort( storedIds );
		for ( final Integer id : truthMap.keySet() )
		{
			if ( Arrays.binarySearch( storedIds, id ) < 0 )
			{
				final Integer poolIndex = truthMap.get( id );
				pool.getObject( poolIndex, ref );
				assertFalse( "Map should not contain the value " + ref, map.containsValue( ref ) );
			}
		}
		pool.releaseRef( ref );
	}

	@Test
	public void testPutIfAbsent()
	{
		final int index = 100;
		final TestVertex ref1 = pool.createRef();
		final TestVertex ref2 = pool.createRef();
		final TestVertex vertex = pool.create( ref1 ).init( index );
		final TestVertex absent = map.putIfAbsent( index, vertex );
		assertNull( "There was not a mapping for index " + index + " before; returned object should be null.", absent );
		assertEquals( "Unexpected mapping for new key " + index, vertex, map.get( index, ref2 ) );

		final int existingMapping = storedIds[ 0 ];
		final TestVertex absent2 = map.putIfAbsent( existingMapping, vertex );
		assertNotNull( "There was a mapping for index " + existingMapping + " before; returned object should not be null.", absent2 );

		final Integer poolIndex = truthMap.get( existingMapping );
		pool.getObject( poolIndex, ref1 );
		assertEquals( "Returned object by putIfAbsent is unexpected.", ref1, absent2 );

		pool.releaseRef( ref1 );
		pool.releaseRef( ref2 );
	}

	@Test
	public void testPutIfAbsentIntVV()
	{
		final int index = 100;
		final TestVertex ref1 = pool.createRef();
		final TestVertex ref2 = pool.createRef();
		final TestVertex vertex = pool.create( ref1 ).init( index );
		final TestVertex absent = map.putIfAbsent( index, vertex, ref2 );
		assertNull( "There was not a mapping for index " + index + " before; returned object should be null.", absent );
		assertEquals( "Unexpected mapping for new key " + index, vertex, map.get( index, ref2 ) );

		final int existingMapping = storedIds[ 0 ];
		final TestVertex ref3 = pool.createRef();
		final TestVertex absent2 = map.putIfAbsent( existingMapping, vertex, ref3 );
		assertNotNull( "There was a mapping for index " + existingMapping + " before; returned object should not be null.", absent2 );

		final Integer poolIndex = truthMap.get( existingMapping );
		pool.getObject( poolIndex, ref1 );
		assertEquals( "Returned object by putIfAbsent is unexpected.", ref1, absent2 );

		pool.releaseRef( ref1 );
		pool.releaseRef( ref2 );
		pool.releaseRef( ref3 );
	}

	@Test
	public void testPutAllMapOfQextendsIntegerQextendsV()
	{
		final Map< Integer, TestVertex > m = new HashMap<>();
		final int[] newIds = new int[] { 101, 102 };
		for ( final int id : newIds )
		{
			m.put( id, pool.create( pool.createRef() ).init( id ) );
		}

		final int size = map.size();
		map.putAll( m );
		assertEquals( "Map does not have the expected size after putAll.", size + m.size(), map.size() );
		for ( final int key : m.keySet() )
		{
			final TestVertex v = m.get( key );
			assertTrue( "Map should now contain a mapping for key " + key, map.containsKey( key ) );
			assertTrue( "Map should now contain a mapping for value " + v, map.containsValue( v ) );
			assertEquals( "New mapping is different than in the source map.", m.get( key ), v );
		}
	}

	@Test
	public void testPutAllTIntObjectMapOfQextendsV()
	{
		final IntRefArrayMap< TestVertex > m = new IntRefArrayMap<>( pool );
		final int[] newIds = new int[] { 101, 102 };
		final TestVertex ref = pool.createRef();
		for ( final int id : newIds )
		{
			m.put( id, pool.create( ref ).init( id ) );
		}

		final int size = map.size();
		map.putAll( m );
		assertEquals( "Map does not have the expected size after putAll.", size + m.size(), map.size() );
		for ( final int key : m.keys() )
		{
			final TestVertex v = m.get( key );
			assertTrue( "Map should now contain a mapping for key " + key, map.containsKey( key ) );
			assertTrue( "Map should now contain a mapping for value " + v, map.containsValue( v ) );
			assertEquals( "New mapping is different than in the source map.", m.get( key ), v );
		}

		pool.releaseRef( ref );
	}

	@Test
	public void testKeys()
	{
		final int[] keys = map.keys();
		assertEquals( "Key array does not have the expected length.", map.size(), keys.length );
		// We know they are in the right order.
		for ( int i = 0; i < keys.length; i++ )
		{
			assertEquals( "Unexpected key returned by keys().", storedIds[ i ], keys[ i ] );
		}
	}

	@Test
	public void testKeysIntArray()
	{
		final int[] arr = new int[ 100 ];
		final int[] keys = map.keys( arr );
		assertEquals( "Returned array and passed array are not the same instance.", arr, keys );
		// They should since arr is larger than the map size.

		// We know they are in the right order.
		for ( int i = 0; i < storedIds.length; i++ )
		{
			assertEquals( "Unexpected key returned by keys().", storedIds[ i ], keys[ i ] );
		}
		for ( int i = storedIds.length; i < keys.length; i++ )
		{
			assertEquals( "Unexpected key returned by keys().", map.getNoEntryKey(), keys[ i ] );
		}
	}

	@Test
	public void testValues()
	{
		final Object[] values = map.values();
		assertEquals( "values() array is not of the expected length.", map.size(), values.length );
		for ( final Object obj : values )
		{
			assertTrue( "Object returned by values() is not of the expected class.", obj instanceof TestVertex );
			assertTrue( "Object returned by values() should be in the map.", map.containsValue( obj ) );
		}
	}

	@Test
	public void testValuesVArray()
	{
		final TestVertex[] arr = new TestVertex[ 100 ];
		final TestVertex[] values = map.values( arr );
		assertEquals( "Returned array and passed array are not the same instance.", arr.hashCode(), values.hashCode() );
		for ( int i = 0; i < map.size(); i++ )
		{
			final TestVertex v = values[ i ];
			assertTrue( "Object returned by values() should be in the map.", map.containsValue( v ) );
		}
		for ( int i = map.size(); i < values.length; i++ )
		{
			assertNull( "Remaining elements should be null.", values[ i ] );
		}
	}

	@Test
	public void testIterator()
	{
		// Test iterate in the right order.
		final TIntObjectIterator< TestVertex > it = map.iterator();
		final TestVertex ref = pool.createRef();
		int index = 0;
		while ( it.hasNext() )
		{
			final int key = storedIds[ index++ ];
			final int poolIndex = truthMap.get( key );
			pool.getObject( poolIndex, ref );

			it.advance();
			assertEquals( "Iterator returns unexpected key.", key, it.key() );
			assertEquals( "Iterator returns unexpected value.", ref, it.value() );
		}

		// Test iterator removal.
		// Remove the 6.
		final int size = map.size();
		final TIntObjectIterator< TestVertex > it2 = map.iterator();
		it2.advance(); // 2
		it2.advance(); // 3
		it2.advance(); // 6
		final TestVertex val = it2.value();
		it2.remove();
		assertEquals( "Map does not have the expected size after removal by keyset iterator.", size - 1, map.size() );
		assertFalse( "Map should not contain a mapping for key " + val + " after removal by keyset iterator.", map.containsValue( val ) );

		// Remove all.
		final TIntObjectIterator< TestVertex > it3 = map.iterator();
		while ( it3.hasNext() )
		{
			it3.advance();
			it3.remove();
		}
		assertTrue( "Map should be empty after removing all content with keyset iterator.", map.isEmpty() );
	}

	@Test
	public void testForEachKey()
	{
		final AtomicInteger ai = new AtomicInteger( 0 );
		final TIntProcedure proc = new TIntProcedure()
		{
			@Override
			public boolean execute( final int value )
			{
				ai.incrementAndGet();
				assertTrue( "Iterated key is not contained in the map.", map.containsKey( value ) );
				return true;
			}
		};
		final boolean ok = map.forEachKey( proc );
		assertTrue( "ForEach procedure should have terminated ok.", ok );
		assertEquals( "All the values have not been iterated through.", map.size(), ai.get() );
	}

	@Test
	public void testForEachValue()
	{
		final AtomicInteger ai = new AtomicInteger( 0 );
		final TObjectProcedure< TestVertex > proc = new TObjectProcedure< TestVertex >()
		{
			@Override
			public boolean execute( final TestVertex value )
			{
				ai.incrementAndGet();
				assertTrue( "Iterated value is not contained in the map.", map.containsValue( value ) );
				return true;
			}
		};
		final boolean ok = map.forEachValue( proc );
		assertTrue( "ForEach procedure should have terminated ok.", ok );
		assertEquals( "All the values have not been iterated through.", map.size(), ai.get() );
	}

	@Test
	public void testForEachEntry()
	{
		final AtomicInteger ai = new AtomicInteger( 0 );
		final TIntObjectProcedure< TestVertex > proc = new TIntObjectProcedure< TestVertex >()
		{
			@Override
			public boolean execute( final int key, final TestVertex value )
			{
				ai.incrementAndGet();
				assertTrue( "Iterated key is not contained in the map.", map.containsKey( key ) );
				assertTrue( "Iterated value is not contained in the map.", map.containsValue( value ) );
				return true;
			}
		};
		final boolean ok = map.forEachEntry( proc );
		assertTrue( "ForEach procedure should have terminated ok.", ok );
		assertEquals( "All the values have not been iterated through.", map.size(), ai.get() );
	}

	@Test
	public void testTransformValues()
	{
		final TestVertex ref = pool.createRef();
		final TestVertex vertex = pool.create( ref ).init( 100 );
		final TObjectFunction< TestVertex, TestVertex > function = new TObjectFunction< TestVertex, TestVertex >()
		{
			@Override
			public TestVertex execute( final TestVertex value )
			{
				return vertex;
			}
		};
		map.transformValues( function );

		for ( final TestVertex value : map.valueCollection() )
		{
			assertEquals( "Unexpected value after change.", vertex, value );
		}
	}

	@Test
	public void testRetainEntries()
	{
		final TIntObjectProcedure< TestVertex > proc = new TIntObjectProcedure< TestVertex >()
		{

			@Override
			public boolean execute( final int a, final TestVertex b )
			{
				return b.getId() == storedIds[ 0 ];
			}
		};
		final boolean changed = map.retainEntries( proc );
		assertTrue( "RetainEntries should have changed the map.", changed );
		assertEquals( "There should be only 1 mapping left.", 1, map.size() );
		final TIntObjectIterator< TestVertex > it = map.iterator();
		it.advance();
		final TestVertex value = it.value();
		assertEquals( "Remaining value is not the right one.", storedIds[ 0 ], value.getId() );
	}
}
