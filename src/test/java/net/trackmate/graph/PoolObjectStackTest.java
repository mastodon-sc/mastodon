package net.trackmate.graph;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import net.trackmate.graph.mempool.ByteMappedElement;
import net.trackmate.trackscheme.TrackSchemeGraph;
import net.trackmate.trackscheme.TrackSchemeVertex;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class PoolObjectStackTest
{
	private PoolObjectStack< TrackSchemeVertex, ByteMappedElement > stack;

	private ArrayList< TrackSchemeVertex > objects;

	@Before
	public void noSetup()
	{
		final TrackSchemeGraph graph = new TrackSchemeGraph();
		stack = new PoolObjectStack< TrackSchemeVertex, ByteMappedElement >( graph.vertexPool );
		final TrackSchemeVertex A = graph.addVertex().init( "A", 0, true );
		final TrackSchemeVertex B = graph.addVertex().init( "B", 1, true );
		final TrackSchemeVertex C = graph.addVertex().init( "C", 1, true );
		final TrackSchemeVertex E = graph.addVertex().init( "E", 1, true );
		final TrackSchemeVertex D = graph.addVertex().init( "D", 2, true );
		final TrackSchemeVertex F = graph.addVertex().init( "F", 2, true );
		final TrackSchemeVertex G = graph.addVertex().init( "G", 2, true );
		objects = new ArrayList< TrackSchemeVertex >( 7 );
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
		for ( final TrackSchemeVertex o : objects )
		{
			stack.push( o );
		}
		final TrackSchemeVertex ref = stack.createRef();
		assertEquals( stack.peek( ref ), objects.get( objects.size() - 1 ) );
	}

	@Test
	public void popTest()
	{
		for ( final TrackSchemeVertex o : objects )
		{
			stack.push( o );
		}
		final TrackSchemeVertex ref = stack.createRef();
		for ( int i = objects.size() - 1; i >= 0; i-- )
		{
			final TrackSchemeVertex o = objects.get( i );
			assertEquals( o, stack.pop( ref ) );
		}
		assertTrue( stack.isEmpty() );
	}

	@Test( expected = ArrayIndexOutOfBoundsException.class )
	public void peekTest()
	{
		final TrackSchemeVertex ref = stack.createRef();
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
		for ( final TrackSchemeVertex o : objects )
		{
			stack.push( o );
		}
		
		final int target = 3;
		final int index = stack.search( objects.get( target ) );
		assertEquals( "Object is not at the right place in the stack.", objects.size() - target, index );
		
		final TrackSchemeVertex ref = stack.createRef();
		stack.pop( ref );
		final int search = stack.search( ref );
		assertTrue( "Object was found but should not be in the stack.", search < 0 );
		
	}

}
