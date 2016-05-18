package net.trackmate.collection.ref;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import net.trackmate.collection.ref.RefLinkedQueue;
import net.trackmate.pool.TestObject;
import net.trackmate.pool.TestObjectPool;

public class RefLinkedQueueTest
{
	private RefLinkedQueue< TestObject > queue;

	private ArrayList< TestObject > objects;

	@Before
	public void noSetup()
	{
		final TestObjectPool pool = new TestObjectPool( 10 );
		queue = new RefLinkedQueue< TestObject >( pool );
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
		queue = null;
	}

	@Test
	public void offerTest()
	{
		for ( final TestObject o : objects )
		{
			queue.offer( o );
		}
		final TestObject ref = queue.createRef();
		assertEquals( queue.peek( ref ), objects.get( 0 ) );
	}

	@Test
	public void pollTest()
	{
		for ( final TestObject o : objects )
		{
			queue.offer( o );
		}
		final TestObject ref = queue.createRef();
		for ( final TestObject o : objects )
		{
			assertEquals( o, queue.poll( ref ) );
		}
		assertTrue( queue.isEmpty() );
	}

	@Test
	public void peekTest()
	{
		final TestObject ref = queue.createRef();
		assertNull( queue.peek( ref ) );
	}

	@Test
	public void emptyTest()
	{
		assertTrue( queue.isEmpty() );
	}
}
