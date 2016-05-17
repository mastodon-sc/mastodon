package net.trackmate.graph;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Random;

import org.junit.Before;
import org.junit.Test;

import net.trackmate.graph.collection.pool.PoolObjectList;

public class PoolObjectListTest
{

	private PoolObjectList< TestObject > list;

	private ArrayList< TestObject > objects;

	@Before
	public void setUp() throws Exception
	{
		final int nobj = 1000;
		final TestObjectPool pool = new TestObjectPool( nobj );
		list = new PoolObjectList< TestObject >( pool );

		final Random rand = new Random();

		final int[] ids = new int[ nobj ];
		for ( int i = 0; i < ids.length; i++ )
		{
			ids[ i ] = i + 1;
		}
		for ( int i = ids.length; i > 1; i-- )
		{
			final int temp = ids[ i - 1 ];
			final int j = rand.nextInt( i );
			ids[ i - 1 ] = ids[ j ];
			ids[ j ] = temp;
		}

		objects = new ArrayList< TestObject >( nobj );
		for ( int i = 0; i < ids.length; i++ )
		{
			final TestObject o = pool.create().init( ids[ i ] );
			list.add( o );
			objects.add( o );
		}
	}

	@Test
	public void testSort()
	{
		final Comparator< TestObject > comparator = new Comparator< TestObject >()
		{
			@Override
			public int compare( final TestObject o1, final TestObject o2 )
			{
				return o1.getId() - o2.getId();
			}
		};
		list.sort( comparator );

		int previousID = Integer.MIN_VALUE;
		for ( final TestObject testObject : list )
		{
			assertTrue( "List around ID " + previousID + " is not sorted.", previousID < testObject.getId() );
			previousID = testObject.getId();
		}
	}

}
