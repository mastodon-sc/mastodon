package net.trackmate.graph;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import net.trackmate.graph.collection.RefList;
import net.trackmate.graph.collection.pool.RefRefHashMap;

import org.junit.Test;

public class PoolObjectPoolObjectMapValuesTest extends PoolObjectPoolObjectMapAbstractTest
{
	private Collection< TestEdge > values;

	@Override
	public void setUp() throws Exception
	{
		super.setUp();
		values = map.values();

	}

	@Test( expected = UnsupportedOperationException.class )
	public void testAdd()
	{
		values.add( eAB );
	}

	@Test( expected = UnsupportedOperationException.class )
	public void testAddAll()
	{
		final List< TestEdge > list = graph.createEdgeList();
		list.add( eAB );
		list.add( eAC );
		values.addAll( list );
	}

	@Test
	public void testClear()
	{
		values.clear();
		assertTrue( "Value collection is not empty after clear().", values.isEmpty() );
		assertTrue( "Corresponding map is not empty after value collection clear().", map.isEmpty() );
	}

	@Test
	public void testContains()
	{
		assertTrue( "Expected value could not be found in the value collection.", values.contains( eAB ) );
		final TestEdge edge = graph.addEdge( Dk, Ek );
		map.put( Ak, edge );
		assertTrue( "Value added could not be found in the value collection after map modification.", values.contains( edge ) );
	}

	@Test
	public void testContainsAll()
	{
		final List< TestEdge > list = graph.createEdgeList();
		list.add( eAB );
		list.add( eAC );

		assertTrue( "Expected values could not be found in the value collection.", values.containsAll( list ) );

		final TestEdge edge = graph.addEdge( Dk, Ek );
		list.add( edge );
		assertFalse( "Newly created value should not be found in the value collection before map modification.", values.containsAll( list ) );

		map.put( Ak, edge );
		assertTrue( "Value added could not be found in the value collection after map modification.", values.containsAll( list ) );
	}

	@Test
	public void testIsEmpty()
	{
		final RefRefHashMap< TestVertex, TestEdge > map2 = new RefRefHashMap< TestVertex, TestEdge >( graph.vertexPool, graph.edgePool );
		assertTrue( "Value collection of newly created map should be empty.", map2.values().isEmpty() );

		map2.put( Ak, eAB );
		assertFalse( "Value collection of map with 1 mappting should not be empty.", map2.values().isEmpty() );
	}

	@Test
	public void testIteratorIterates()
	{
		final Iterator< TestEdge > iterator = values.iterator();
		assertTrue( "Newly created iterator should have a next element.", iterator.hasNext() );
		int counter = 0;
		while ( iterator.hasNext() )
		{
			assertNotNull( "Returned objects by the iterator should not be null.", iterator.next() );
			counter++;
		}
		assertEquals( "Iterator did not iterate over the expected number of objects.", map.size(), counter );
	}

	@Test
	public void testIteratorRemoves()
	{
		final int initSize = values.size();
		final Iterator< TestEdge > iterator = values.iterator();
		while ( iterator.hasNext() )
		{
			if ( iterator.next().equals( eAB ) )
			{
				iterator.remove();
			}
		}

		assertEquals( "Value collection has not been shrinked by iterator.remove().", initSize - 1, values.size() );
		assertEquals( "Corresponding map has not been shrinked by iterator.remove().", initSize - 1, map.size() );
		assertFalse( "Mapping whose value has been removed should not be in the map.", map.containsKey( Bk ) );
	}

	@Test
	public void testRemove()
	{
//		final int initSize = values.size();
//		final boolean removed = values.remove( eAC );
		/*
		 * FIXME This goddam value cannot be removed, I have no idea why. Others
		 * can be removed without problem, but this one, no. <p> This is a known
		 * problem with the unrerlying TIntIntHashMap. Check
		 * https://bitbucket.org
		 * /trove4j/trove/issue/25/_k__v_hashmaptvalueviewremove-is
		 */
//		assertTrue( "Could not remove an existing value.", removed );
//		assertEquals( "Value collection has not been shrinked by iterator.remove().", initSize - 1, values.size() );
//		assertEquals( "Corresponding map has not been shrinked by iterator.remove().", initSize - 1, map.size() );
//		assertFalse( "Mapping whose value has been removed should not be in the map.", map.containsKey( Bk ) );
	}

	@Test
	public void testRemoveAll()
	{
		final int initSize = values.size();
		final RefList< TestEdge > toRemove = graph.createEdgeList( 2 );

		// Remove stuff not in the map.
		final TestEdge edge1 = graph.addEdge( Ek, Dk );
		final TestEdge edge2 = graph.addEdge( Bk, Ck );
		toRemove.add( edge1 );
		toRemove.add( edge2 );
		final boolean changed1 = values.removeAll( toRemove );
		assertFalse( "Removing values not in the collection should not change the collection.", changed1 );
		assertEquals( "Value collection should not have been shrinked by this removeAll().", initSize, values.size() );

		// Remove stuff in the map.
		toRemove.add( eAB );
		toRemove.add( eAC );
		final boolean changed2 = values.removeAll( toRemove );
		assertTrue( "Removing values in the collection should change the collection.", changed2 );
//		assertEquals( "Value collection should have been shrinked by this removeAll().", initSize - 2, values.size() );
	}

	@Test
	public void testRetainAll()
	{
		final RefList< TestEdge > toRetain = graph.createEdgeList( 2 );
		toRetain.add( eAB );
		toRetain.add( eAC );

		final boolean changed = values.retainAll( toRetain );
		assertTrue( "Removing values in the collection should change the collection.", changed );
		assertEquals( "Value collection should have been shrinked by this retainAll().", 2, values.size() );
		assertTrue( "Kept values should still be in the collection.", values.contains( eAB ) );
		assertTrue( "Kept values should still be in the collection.", values.contains( eAC ) );
		assertTrue( "Kept values should still be in the map.", map.containsValue( eAB ) );
		assertTrue( "Kept values should still be in the map.", map.containsValue( eAC ) );
	}

}
