package org.mastodon.graph.util;

import gnu.trove.map.hash.TIntIntHashMap;

import org.junit.Test;

public class TIntIntHashMapTest
{

	@Test
	public void test()
	{
		final TIntIntHashMap map = new TIntIntHashMap( 10, 0.5f, -100000, -2000000 );

		map.put( 1, 0 );
		map.put( 2, 5 );
		map.put( 3, 2 );
		map.put( 4, 3 );

//		final TIntCollection values = map.valueCollection();
//		final int initSize = values.size();

		/*
		 * Known problem. Check https://bitbucket.org/trove4j/trove/issue/25/
		 * _k__v_hashmaptvalueviewremove-is
		 */

//		final boolean removed0 = values.remove( 5 );
//		assertTrue( "Could not remove an existing value.", removed0 );
//		assertEquals( "Value collection has not been shrinked by iterator.remove().", initSize - 1, values.size() );
//		assertEquals( "Corresponding map has not been shrinked by iterator.remove().", initSize - 1, map.size() );

	}

}
