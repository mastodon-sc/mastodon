package org.mastodon.collection.ref;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import gnu.trove.set.TIntSet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import org.junit.Before;
import org.junit.Test;
import org.mastodon.collection.ref.RefSetImp;
import org.mastodon.graph.TestVertex;
import org.mastodon.graph.TestVertexPool;

public class RefSetImpTest
{

	private ArrayList< TestVertex > list;

	private RefSetImp< TestVertex > set;

	private TestVertexPool pool;

	private int[] storedIds;

	@Before
	public void setUp() throws Exception
	{
		pool = new TestVertexPool( 10 );
		list = new ArrayList< TestVertex >( 10 );
		// Creates 10 objects and store them.
		for ( int i = 0; i < 10; i++ )
		{
			list.add( pool.create( pool.createRef() ).init( i ) );
		}
		// Add half of it to the set.
		set = new RefSetImp<>( pool );
		storedIds = new int[ 5 ];
		for ( int i = 0; i < list.size(); i = i + 2 )
		{
			set.add( list.get( i ) );
			storedIds[ i / 2 ] = list.get( i ).getInternalPoolIndex();
		}
	}

	@Test
	public void testCreateRef()
	{
		set.createRef();
	}

	@Test
	public void testReleaseRef()
	{
		final TestVertex ref = set.createRef();
		set.releaseRef( ref );
	}

	@Test
	public void testGetIndexCollection()
	{
		final TIntSet ic = set.getIndexCollection();
		assertEquals( "Index collection does not have the expected size.", set.size(), ic.size() );
		final Iterator< TestVertex > it = set.iterator();
		while ( it.hasNext() )
		{
			final int poolIndex = it.next().getInternalPoolIndex();
			assertTrue( "An object in the set does not have its internal pool index in the index collection.", ic.contains( poolIndex ) );
		}
	}

	@Test
	public void testAdd()
	{
		// Add already present objects.
		for ( int i = 0; i < list.size(); i = i + 2 )
		{
			final boolean added = set.add( list.get( i ) );
			assertFalse( "Adding an already present value should not change the set.", added );
		}

		// Add new objects.
		for ( int i = 1; i < list.size(); i = i + 2 )
		{
			final boolean added = set.add( list.get( i ) );
			assertTrue( "Adding a new value should change the set.", added );
		}
		assertEquals( "Set does not have the expected size after addition.", list.size(), set.size() );
	}

	@Test
	public void testAddAll()
	{
		final boolean changed = set.addAll( list );
		assertTrue( "Adding new values should change the set.", changed );
		assertEquals( "Set does not have the expected size after addition.", list.size(), set.size() );
		for ( final TestVertex testVertex : list )
		{
			assertTrue( "New value should be present in the set.", set.contains( testVertex ) );
		}
	}

	@Test
	public void testClear()
	{
		set.clear();
		assertTrue( "Set should be empty after clear.", set.isEmpty() );
		assertEquals( "Set size should be 0 after clear.", 0, set.size() );
	}

	@Test
	public void testContains()
	{
		// Check for present objects.
		for ( int i = 0; i < list.size(); i = i + 2 )
		{
			assertTrue( "Value " + list.get( i ) + " should be present in the set.", set.contains( list.get( i ) ) );
		}

		// Check for non present objects.
		for ( int i = 1; i < list.size(); i = i + 2 )
		{
			assertFalse( "Value " + list.get( i ) + " should not be present in the set.", set.contains( list.get( i ) ) );
		}
	}

	@Test
	public void testContainsAll()
	{
		final boolean containsAll = set.containsAll( list );
		assertFalse( "Large collection is not contained in the set.", containsAll );

		final ArrayList< TestVertex > smallList = new ArrayList< TestVertex >( 2 );
		for ( int i = 0; i < 2; i = i + 2 )
		{
			smallList.add( list.get( i ) );
		}
		assertTrue( "Small collection is contained in the set.", set.containsAll( smallList ) );
	}

	@Test
	public void testIsEmpty()
	{
		assertFalse( "Set should not be empty.", set.isEmpty() );
		set.clear();
		assertTrue( "Cleared set should be empty.", set.isEmpty() );
		assertTrue( "New set should be empty.", new RefSetImp<>( pool ).isEmpty() );
	}

	@Test
	public void testIterator()
	{
		// Test iterate over all set.
		final Iterator< TestVertex > it = set.iterator();
		Arrays.sort( storedIds );
		int count = 0;
		while ( it.hasNext() )
		{
			final TestVertex v = it.next();
			final int i = Arrays.binarySearch( storedIds, v.getInternalPoolIndex() );
			assertTrue( "Iterator returns object: " + v, i >= 0 );
			count++;
		}
		assertEquals( "Iterator did not iterate over the whole set.", set.size(), count );

		// Test iterator removal.
		// Remove the 3rd whatsoever value.
		final int size = set.size();
		final Iterator< TestVertex > it2 = set.iterator();
		it2.next();
		it2.next();
		final TestVertex val = it2.next();
		it2.remove();
		assertEquals( "Map does not have the expected size after removal by keyset iterator.", size - 1, set.size() );
		assertFalse( "Map should not contain a mapping for key " + val + " after removal by keyset iterator.", set.contains( val ) );

		// Remove all.
		final Iterator< TestVertex > it3 = set.iterator();
		while ( it3.hasNext() )
		{
			it3.next();
			it3.remove();
		}
		assertTrue( "Map should be empty after removing all content with keyset iterator.", set.isEmpty() );
	}

	@Test
	public void testRemove()
	{
		// Remove non present objects.
		for ( int i = 1; i < list.size(); i = i + 2 )
		{
			final boolean removed = set.remove( list.get( i ) );
			assertFalse( "Removing a non present value should not change the set.", removed );
		}

		// Remove present objects.
		for ( int i = 0; i < list.size(); i = i + 2 )
		{
			final boolean removed = set.remove( list.get( i ) );
			assertTrue( "Removing a present value should change the set.", removed );
		}
		assertEquals( "Set does not have the expected size after removing all values.", 0, set.size() );
	}

	@Test
	public void testRemoveAll()
	{
		final ArrayList< TestVertex > smallList = new ArrayList< TestVertex >( 2 );
		// Not in the set.
		for ( int i = 1; i < 2; i = i + 2 )
		{
			smallList.add( list.get( i ) );
		}
		final int size = set.size();
		final boolean changed = set.removeAll( smallList );
		assertFalse( "Removing small collection of non-present values should not change the set.", changed );
		assertEquals( "Removing small collection of non-present values should not change the set size.", size, set.size() );

		// In the set + 1 not in the set
		smallList.clear();
		for ( int i = 0; i < 2; i = i + 2 )
		{
			smallList.add( list.get( i ) );
		}
		smallList.add( list.get( 9 ) );
		final boolean changed2 = set.removeAll( smallList );
		assertTrue( "Removing small collection of present values should change the set.", changed2 );
		assertEquals( "Removing small collection of present values should change the set size.", size - smallList.size() + 1, set.size() );

		final boolean changed3 = set.removeAll( list );
		assertTrue( "Removing all values should change the set.", changed3 );
		assertTrue( "Removing all values should leave the set empty.", set.isEmpty() );
	}

	@Test
	public void testRetainAll()
	{
		// Retain with a list that contains the whole set.
		final int size = set.size();
		final boolean changed1 = set.retainAll( list );
		assertFalse( "Retaining large collection of all present values should not change the set.", changed1 );
		assertEquals( "Retaining small collection of present values should not change the set size.", size, set.size() );

		final ArrayList< TestVertex > smallList = new ArrayList< TestVertex >( 2 );

		// In the set + 1 not in the set
		for ( int i = 0; i < 2; i = i + 2 )
		{
			smallList.add( list.get( i ) );
		}
		smallList.add( list.get( 9 ) );
		final boolean changed2 = set.retainAll( smallList );
		assertTrue( "Retaining small collection of present values should change the set.", changed2 );
		assertEquals( "Retaining small collection of present values should change the set size.", smallList.size() - 1, set.size() );
		for ( final TestVertex v : set )
		{
			assertTrue( "All values of the set should not be in the small collection.", smallList.contains( v ) );
		}

		// Not in the set.
		smallList.clear();
		for ( int i = 1; i < 2; i = i + 2 )
		{
			smallList.add( list.get( i ) );
		}
		final boolean changed = set.retainAll( smallList );
		assertTrue( "Retaining small collection of non-present values should change the set.", changed );
		assertEquals( "Retaining small collection of non-present values should change the set size to 0.", 0, set.size() );
		assertTrue( "The set should now be empty.", set.isEmpty() );
	}

	@Test
	public void testSize()
	{
		final int size = 5; // hardcoded.
		assertEquals( "Set is expected to have a size of " + size, size, set.size() );
		set.remove( list.get( 0 ) );
		assertEquals( "Set size is expected to decrease by 1 after 1 element removal.", size - 1, set.size() );
		set.add( list.get( 1 ) );
		set.add( list.get( 3 ) );
		assertEquals( "Set size is expected to increase by 2 after adding 2 elements.", size + 1, set.size() );
		set.add( list.get( 2 ) );
		assertEquals( "Set size is expected not to change after adding an element already present.", size + 1, set.size() );
		set.clear();
		assertEquals( "Set size is expected be 0 after clearing.", 0, set.size() );
	}

	@Test
	public void testToArray()
	{
		final Object[] array = set.toArray();
		assertEquals( "Created array does not have the expected length.", set.size(), array.length );
		for ( final Object obj : array )
		{
			assertTrue( "Unexpected object in the array returned by toArray(): ", set.contains( obj ) );
		}
	}

	@Test
	public void testToArrayAArray()
	{
		final TestVertex[] arr = new TestVertex[ 100 ];
		// Initialize it with non-null values.
		final TestVertex v = pool.create( pool.createRef() ).init( 100 );
		for ( int i = 0; i < arr.length; i++ )
		{
			arr[ i ] = v;
		}

		final TestVertex[] array = set.toArray( arr );
		for ( int i = 0; i < set.size(); i++ )
		{
			assertTrue( "Unexpected object in the array returned by toArray(): " + array[ i ], set.contains( array[ i ] ) );
		}
		for ( int j = set.size(); j < array.length; j++ )
		{
			assertNull( "Remaining array slots should be null.", array[ j ] );
		}
	}
}
