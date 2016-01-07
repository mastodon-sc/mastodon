package gnu.trove.map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.junit.Before;
import org.junit.Test;

public class TIntObjectArrayMapValueCollectionTest
{
	private TIntObjectArrayMap< String > map;

	private HashMap< Integer, String > truthMap;

	private int[] storedIds;

	private Collection< String > valueCollection;

	@Before
	public void setUp() throws Exception
	{
		truthMap = new HashMap<>( 10 );
		for ( int i = 0; i < 10; i++ )
		{
			final String a = "a-" + i;
			truthMap.put( Integer.valueOf( i ), a );
		}

		map = new TIntObjectArrayMap< >();
		storedIds = new int[] { 2, 3, 6, 8 };
		for ( final int id : storedIds )
		{
			final String val = truthMap.get( id );
			map.put( id, val );
		}
		valueCollection = map.valueCollection();
	}

	@Test( expected = UnsupportedOperationException.class )
	public void testAdd()
	{
		final String v = "Can I get a pay raise?";
		valueCollection.add( v );
	}

	@Test( expected = UnsupportedOperationException.class )
	public void testAddAll()
	{
		final HashSet< String > set = new HashSet< >();
		for ( int i = 0; i < 5; i++ )
		{
			set.add( "" + 100 + i );
		}
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
		for ( final int id : storedIds )
		{
			final String val = truthMap.get( id );
			assertTrue( "ValueCollection should contain the object " + val, valueCollection.contains( val ) );
		}
		for ( final int id : new int[] { 0, 1, 4, 5, 7, 9 } )
		{
			final String val = truthMap.get( id );
			assertFalse( "ValueCollection should not contain the object " + val, valueCollection.contains( val ) );
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
		assertTrue( "ValueCollection from new map should be empty.", new TIntObjectArrayMap< >().valueCollection().isEmpty() );
	}

	@Test
	public void testIterator()
	{
		// Test iterate in the right order.
		final Iterator< String > it = valueCollection.iterator();
		int index = 0;
		while ( it.hasNext() )
		{
			final String actual = it.next();

			final int key = storedIds[ index++ ];
			final String expected = truthMap.get( key );

			assertEquals( "Iterator returns unexpected value.", expected, actual );
		}

		// Test iterator removal.
		// Remove the 6.
		final int size = map.size();
		final Iterator< String > it2 = valueCollection.iterator();
		it2.next(); // 2
		it2.next(); // 3
		final String val = it2.next(); // 6
		it2.remove();
		assertEquals( "Map does not have the expected size after removal by keyset iterator.", size - 1, map.size() );
		assertFalse( "Map should not contain a mapping for key " + val + " after removal by keyset iterator.", map.containsValue( val ) );

		// Remove all.
		final Iterator< String > it3 = valueCollection.iterator();
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
		for ( int i = 0; i < 5; i++ )
		{
			map.put( 100 + i, "more" );
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
		int index = 0;
		final ArrayList< String > list = createListFromKeys( storedIds );
		for ( final String expected : list )
		{
			assertEquals( "Unexpected object in the array returned by toArray().", expected, array[ index++ ] );
		}
	}

	@Test
	public void testToArrayArray()
	{
		final String[] array = valueCollection.toArray( new String[ 100 ] );
		int index = 0;
		final ArrayList< String > list = createListFromKeys( storedIds );
		for ( final String expected : list )
		{
			assertEquals( "Unexpected object in the array returned by toArray(T[]).", expected, array[ index++ ] );
		}
		for ( int j = index; j < array.length; j++ )
		{
			assertNull( "Remaining array slots should be null.", array[ j ] );
		}
	}

	private ArrayList< String > createListFromKeys( final int[] keys )
	{
		final ArrayList< String > list = new ArrayList< >();
		for ( final int key : keys )
		{
			final String val = truthMap.get( key );
			list.add( val );
		}
		return list;
	}
}
