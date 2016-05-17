package net.trackmate.graph;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import net.trackmate.graph.collection.pool.RefArrayStack;

public class PoolObjectStackTest
{
	private RefArrayStack< TestObject > stack;

	private ArrayList< TestObject > objects;

	@Before
	public void noSetup()
	{
		final TestObjectPool pool = new TestObjectPool( 10 );
		stack = new RefArrayStack< TestObject >( pool );
		final TestObject A = pool.create().init( 1 );
		final TestObject B = pool.create().init( 2 );
		final TestObject C = pool.create().init( 3 );
		final TestObject E = pool.create().init( 4 );
		final TestObject D = pool.create().init( 5 );
		final TestObject F = pool.create().init( 6 );
		final TestObject G = pool.create().init( 7 );
		objects = new ArrayList< TestObject >( 7 );
		objects.add( A );
		objects.add( B );
		objects.add( C );
		objects.add( D );
		objects.add( E );
		objects.add( F );
		objects.add( G );
	}

	@After
	public void noTearDown()
	{
		stack = null;
	}

	@Test
	public void pushTest()
	{
		for ( final TestObject o : objects )
		{
			stack.push( o );
		}
		final TestObject ref = stack.createRef();
		assertEquals( stack.peek( ref ), objects.get( objects.size() - 1 ) );
	}

	@Test
	public void popTest()
	{
		for ( final TestObject o : objects )
		{
			stack.push( o );
		}
		final TestObject ref = stack.createRef();
		for ( int i = objects.size() - 1; i >= 0; i-- )
		{
			final TestObject o = objects.get( i );
			assertEquals( o, stack.pop( ref ) );
		}
		assertTrue( stack.isEmpty() );
	}

	@Test( expected = ArrayIndexOutOfBoundsException.class )
	public void peekTest()
	{
		final TestObject ref = stack.createRef();
		stack.peek( ref );
	}

	@Test
	public void emptyTest()
	{
		assertTrue( stack.isEmpty() );
	}

	@Test
	public void searchTest()
	{
		for ( final TestObject o : objects )
		{
			stack.push( o );
		}

		final int target = 3;
		final int index = stack.search( objects.get( target ) );
		assertEquals( "Object is not at the right place in the stack.", objects.size() - target, index );

		final TestObject ref = stack.createRef();
		stack.pop( ref );
		final int search = stack.search( ref );
		assertTrue( "Object was found but should not be in the stack.", search < 0 );

	}

}
