package net.trackmate.graph;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;

import org.junit.Test;

import net.trackmate.graph.collection.pool.PoolObjectDeque;

public class PoolObjectDequeTest
{
	@Test
	public void addFirstTest()
	{
		final TestObjectPool pool = new TestObjectPool( 10 );
		final TestObject _ref = pool.createRef();
		final PoolObjectDeque< TestObject > deque = new PoolObjectDeque< TestObject >( pool );

		deque.addFirst( pool.create( _ref ).init( 10 ) );
		deque.addFirst( pool.create( _ref ).init( 11 ) );
		assertEquals( deque.iterator().next().getId(), 11 );
	}

	@Test
	public void addLastTest()
	{
		final TestObjectPool pool = new TestObjectPool( 10 );
		final TestObject _ref = pool.createRef();
		final PoolObjectDeque< TestObject > deque = new PoolObjectDeque< TestObject >( pool );

		deque.addLast( pool.create( _ref ).init( 10 ) );
		deque.addLast( pool.create( _ref ).init( 11 ) );
		assertEquals( deque.iterator().next().getId(), 10 );
	}

	@Test
	public void pollFirstTest()
	{
		final TestObjectPool pool = new TestObjectPool( 10 );
		final TestObject _ref = pool.createRef();
		final PoolObjectDeque< TestObject > deque = new PoolObjectDeque< TestObject >( pool );

		deque.addFirst( pool.create( _ref ).init( 10 ) );
		deque.addFirst( pool.create( _ref ).init( 11 ) );
		assertEquals( deque.pollFirst( _ref ).getId(), 11 );
	}

	@Test
	public void pollLastTest()
	{
		final TestObjectPool pool = new TestObjectPool( 10 );
		final TestObject _ref = pool.createRef();
		final PoolObjectDeque< TestObject > deque = new PoolObjectDeque< TestObject >( pool );

		deque.addLast( pool.create( _ref ).init( 10 ) );
		deque.addLast( pool.create( _ref ).init( 11 ) );
		assertEquals( deque.pollLast( _ref ).getId(), 11 );
	}

	@Test
	public void iteratorTest()
	{
		final TestObjectPool pool = new TestObjectPool( 10 );
		final TestObject _ref = pool.createRef();
		final PoolObjectDeque< TestObject > deque = new PoolObjectDeque< TestObject >( pool );

		deque.addLast( pool.create( _ref ).init( 10 ) );
		deque.addLast( pool.create( _ref ).init( 11 ) );
		final Iterator< TestObject > iter = deque.iterator();
		assertTrue( iter.hasNext() );
		assertEquals( iter.next().getId(), 10 );
		assertTrue( iter.hasNext() );
		assertEquals( iter.next().getId(), 11 );
		assertFalse( iter.hasNext() );
	}

	@Test
	public void descendingIteratorTest()
	{
		final TestObjectPool pool = new TestObjectPool( 10 );
		final TestObject _ref = pool.createRef();
		final PoolObjectDeque< TestObject > deque = new PoolObjectDeque< TestObject >( pool );

		deque.addLast( pool.create( _ref ).init( 10 ) );
		deque.addLast( pool.create( _ref ).init( 11 ) );
		final Iterator< TestObject > iter = deque.descendingIterator();
		assertTrue( iter.hasNext() );
		assertEquals( iter.next().getId(), 11 );
		assertTrue( iter.hasNext() );
		assertEquals( iter.next().getId(), 10 );
		assertFalse( iter.hasNext() );
	}

	@Test
	public void containsTest()
	{
		final TestObjectPool pool = new TestObjectPool( 10 );
		final PoolObjectDeque< TestObject > deque = new PoolObjectDeque< TestObject >( pool );
		final TestObject o10 = pool.create().init( 10 );
		final TestObject o11 = pool.create().init( 11 );
		final TestObject o29 = pool.create().init( 29 );
		final TestObject o51 = pool.create().init( 51 );
		final TestObject o100 = pool.create().init( 100 );
		deque.addLast( o10 );
		deque.addLast( o11 );
		deque.addLast( o29 );
		deque.addLast( o51 );
		assertTrue( deque.contains( o10 ) );
		assertTrue( deque.contains( o11 ) );
		assertTrue( deque.contains( o29 ) );
		assertTrue( deque.contains( o51 ) );
		assertFalse( deque.contains( o100 ) );
	}

}
