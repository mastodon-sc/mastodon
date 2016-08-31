package org.mastodon.graph.util;

import static org.junit.Assert.*;

import org.junit.Test;

import gnu.trove.deque.TIntArrayDeque;
import gnu.trove.iterator.TIntIterator;

public class TIntArrayDequeTest
{
	@Test
	public void addFirstTest()
	{
		final TIntArrayDeque deque = new TIntArrayDeque( 10, -1 );
		deque.addFirst( 10 );
		deque.addFirst( 11 );
		assertEquals( deque.iterator().next(), 11 );
	}

	@Test
	public void addLastTest()
	{
		final TIntArrayDeque deque = new TIntArrayDeque( 10, -1 );
		deque.addLast( 10 );
		deque.addLast( 11 );
		assertEquals( deque.iterator().next(), 10 );
	}

	@Test
	public void pollFirstTest()
	{
		final TIntArrayDeque deque = new TIntArrayDeque( 10, -1 );
		deque.addFirst( 10 );
		deque.addFirst( 11 );
		assertEquals( deque.pollFirst(), 11 );
	}

	@Test
	public void pollLastTest()
	{
		final TIntArrayDeque deque = new TIntArrayDeque( 10, -1 );
		deque.addLast( 10 );
		deque.addLast( 11 );
		assertEquals( deque.pollLast(), 11 );
	}

	@Test
	public void iteratorTest()
	{
		final TIntArrayDeque deque = new TIntArrayDeque( 10, -1 );
		deque.addLast( 10 );
		deque.addLast( 11 );
		final TIntIterator iter = deque.iterator();
		assertTrue( iter.hasNext() );
		assertEquals( iter.next(), 10 );
		assertTrue( iter.hasNext() );
		assertEquals( iter.next(), 11 );
		assertFalse( iter.hasNext() );
	}

	@Test
	public void dscendingIteratorTest()
	{
		final TIntArrayDeque deque = new TIntArrayDeque( 10, -1 );
		deque.addLast( 10 );
		deque.addLast( 11 );
		final TIntIterator iter = deque.descendingIterator();
		assertTrue( iter.hasNext() );
		assertEquals( iter.next(), 11 );
		assertTrue( iter.hasNext() );
		assertEquals( iter.next(), 10 );
		assertFalse( iter.hasNext() );
	}

	@Test
	public void containsTest()
	{
		final TIntArrayDeque deque = new TIntArrayDeque( 10, -1 );
		deque.addLast( 10 );
		deque.addLast( 11 );
		deque.addLast( 29 );
		deque.addLast( 51 );
		assertTrue( deque.contains( 10 ) );
		assertTrue( deque.contains( 11 ) );
		assertTrue( deque.contains( 29 ) );
		assertTrue( deque.contains( 51 ) );
		assertFalse( deque.contains( 100 ) );
	}

	@Test
	public void removeTest()
	{
		final TIntArrayDeque deque = new TIntArrayDeque( 10, -1 );
		deque.addLast( 1 );
		deque.addLast( 2 );
		deque.addLast( 3 );
		deque.addLast( 2 );
		deque.addLast( 4 );
		deque.remove( 2 );
		final TIntIterator iter = deque.iterator();
		assertTrue( iter.hasNext() );
		assertEquals( iter.next(), 1 );
		assertTrue( iter.hasNext() );
		assertEquals( iter.next(), 3 );
		assertTrue( iter.hasNext() );
		assertEquals( iter.next(), 2 );
		assertTrue( iter.hasNext() );
		assertEquals( iter.next(), 4 );
		assertFalse( iter.hasNext() );
	}

	@Test
	public void remove2Test()
	{
		final TIntArrayDeque deque = new TIntArrayDeque( 10, -1 );
		deque.addFirst( 4 );
		deque.addFirst( 2 );
		deque.addFirst( 3 );
		deque.addFirst( 2 );
		deque.addFirst( 1 );
		deque.remove( 2 );
		final TIntIterator iter = deque.iterator();
		assertTrue( iter.hasNext() );
		assertEquals( iter.next(), 1 );
		assertTrue( iter.hasNext() );
		assertEquals( iter.next(), 3 );
		assertTrue( iter.hasNext() );
		assertEquals( iter.next(), 2 );
		assertTrue( iter.hasNext() );
		assertEquals( iter.next(), 4 );
		assertFalse( iter.hasNext() );
	}

	@Test
	public void removeLastOccurrenceTest()
	{
		final TIntArrayDeque deque = new TIntArrayDeque( 10, -1 );
		deque.addLast( 1 );
		deque.addLast( 2 );
		deque.addLast( 3 );
		deque.addLast( 2 );
		deque.addLast( 4 );
		deque.removeLastOccurrence( 2 );
		final TIntIterator iter = deque.iterator();
		assertTrue( iter.hasNext() );
		assertEquals( iter.next(), 1 );
		assertTrue( iter.hasNext() );
		assertEquals( iter.next(), 2 );
		assertTrue( iter.hasNext() );
		assertEquals( iter.next(), 3 );
		assertTrue( iter.hasNext() );
		assertEquals( iter.next(), 4 );
		assertFalse( iter.hasNext() );
	}

	@Test
	public void removeLastOccurrence2Test()
	{
		final TIntArrayDeque deque = new TIntArrayDeque( 10, -1 );
		deque.addFirst( 4 );
		deque.addFirst( 2 );
		deque.addFirst( 3 );
		deque.addFirst( 2 );
		deque.addFirst( 1 );
		deque.removeLastOccurrence( 2 );
		final TIntIterator iter = deque.iterator();
		assertTrue( iter.hasNext() );
		assertEquals( iter.next(), 1 );
		assertTrue( iter.hasNext() );
		assertEquals( iter.next(), 2 );
		assertTrue( iter.hasNext() );
		assertEquals( iter.next(), 3 );
		assertTrue( iter.hasNext() );
		assertEquals( iter.next(), 4 );
		assertFalse( iter.hasNext() );
	}
}
