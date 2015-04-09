package net.trackmate.graph;

import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Random;

import net.trackmate.graph.mempool.ByteMappedElement;

import org.junit.Before;
import org.junit.Test;

public class PoolObjectListTest
{

	private PoolObjectList< TestObject, ByteMappedElement > list;

	private ArrayList< TestObject > objects;

	@Before
	public void setUp() throws Exception
	{
		final TestObjectPool pool = new TestObjectPool( 10 );
		list = new PoolObjectList< TestObject, ByteMappedElement >( pool );
		final TestObject A = pool.create().init( 1 );
		final TestObject B = pool.create().init( 2 );
		final TestObject C = pool.create().init( 3 );
		final TestObject E = pool.create().init( 4 );
		final TestObject D = pool.create().init( 5 );
		final TestObject F = pool.create().init( 6 );
		final TestObject G = pool.create().init( 7 );

		list.add( A );
		list.add( B );
		list.add( C );
		list.add( D );
		list.add( E );
		list.add( F );
		list.add( G );

		objects = new ArrayList< TestObject >( 7 );
		objects.add( A );
		objects.add( B );
		objects.add( C );
		objects.add( D );
		objects.add( E );
		objects.add( F );
		objects.add( G );
	}

	@Test
	public void testSort()
	{
		list.shuffle( new Random( 1l ) );
		for ( final TestObject testObject : list )
		{
			System.out.println( testObject );// DEBUG
		}
		System.out.println();// DEBUG

		final Comparator< TestObject > comparator = new Comparator< TestObject >()
		{

			@Override
			public int compare( final TestObject o1, final TestObject o2 )
			{
				return o1.getId() - o2.getId();
			}
		};

		list.sort( comparator );
		for ( final TestObject testObject : list )
		{
			System.out.println( testObject );// DEBUG
		}
		System.out.println();// DEBUG
		fail( "Not yet implemented" );
	}

}
