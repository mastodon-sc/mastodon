package org.mastodon.feature.update;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.function.Consumer;

import org.junit.Before;
import org.junit.Test;

public class SizedDequeTest
{

	private SizedDeque< String > queue;

	@Before
	public void setUp()
	{
		this.queue = new SizedDeque<>( 5 );
		queue.push( "One" );
		queue.push( "Two" );
		queue.push( "Three" );
	}

	@Test
	public void testPush()
	{
		testGeneral( queue::push, "One" );
	}

	@Test
	public void testAdd()
	{
		testGeneral( queue::add, "Five" );
	}

	@Test
	public void testAddInt()
	{
		final Consumer< String > method = new Consumer< String >()
		{

			@Override
			public void accept( final String element )
			{
				queue.add( 2, element );
			}
		};
		testGeneral( method, "One" );
	}

	@Test
	public void testAddFirst()
	{
		testGeneral( queue::addFirst, "One" );
	}

	@Test
	public void testAddLast()
	{
		testGeneral( queue::addLast, "Five" );
	}

	private void testGeneral( final Consumer< String > method, final String removedOne )
	{
		method.accept( "Four" );
		assertEquals( "Wrong stack size after push.", 4, queue.size() );
		method.accept( "Five" );
		assertEquals( "Wrong stack size after push.", 5, queue.size() );
		method.accept( "Six" );
		assertEquals( "Stack size should not have increased after push.", 5, queue.size() );
		assertTrue( "Stack should contain 2nd element.", queue.contains( "Two" ) );
		assertFalse( "Stack should not contain 1st element anymore.", queue.contains( removedOne ) );
	}

	@Test
	public void testAddAllCollection1()
	{
		final boolean val = queue.addAll( Arrays.asList( new String[] { "Un", "Deux" } ) );
		assertEquals( "Wrong stack size after addAll.", 5, queue.size() );
		assertTrue( "Stack should have been modified.", val );
	}

	@Test
	public void testAddAllCollection2()
	{
		final boolean val = queue.addAll( Arrays.asList( new String[] { "Un", "Deux", "Trois", "Quatre" } ) );
		assertEquals( "Wrong stack size after addAll.", 5, queue.size() );
		assertTrue( "Stack should have been modified.", val );
	}

	@Test
	public void testAddAllCollection3()
	{
		final boolean val = queue.addAll( Arrays.asList( new String[] { "Un", "Deux", "Trois", "Quatre", "Cinq", "Six" } ) );
		assertEquals( "Wrong stack size after addAll.", 5, queue.size() );
		assertTrue( "Stack should have been modified.", val );
	}

	@Test
	public void testAddAllIntCollection1()
	{
		final boolean val = queue.addAll( 2, Arrays.asList( new String[] { "Un", "Deux" } ) );
		assertEquals( "Wrong stack size after addAll.", 5, queue.size() );
		assertTrue( "Stack should have been modified.", val );
	}

	@Test
	public void testAddAllIntCollection2()
	{
		final boolean val = queue.addAll( 2, Arrays.asList( new String[] { "Un", "Deux", "Trois", "Quatre" } ) );
		assertEquals( "Wrong stack size after addAll.", 5, queue.size() );
		assertTrue( "Stack should have been modified.", val );
	}

	@Test
	public void testAddAllIntCollection3()
	{
		final boolean val = queue.addAll( 2, Arrays.asList( new String[] { "Un", "Deux", "Trois", "Quatre", "Cinq", "Six"  } ) );
		assertEquals( "Wrong stack size after addAll.", 5, queue.size() );
		assertTrue( "Stack should have been modified.", val );
	}

	@Test
	public void testSize()
	{
		queue.push( "Four" );
		assertEquals( "Wrong stack size after push.", 4, queue.size() );
		queue.push( "Five" );
		assertEquals( "Wrong stack size after push.", 5, queue.size() );
		for ( int i = 0; i < 100; i++ )
		{
			queue.push( "" + i );
			assertEquals( "Wrong stack size after push.", 5, queue.size() );
		}
		for ( int i = 0; i < queue.size(); i++ )
		{
			queue.poll();
			assertEquals( "Wrong stack size after poll.", 4 - i, queue.size() );
		}
	}

}
