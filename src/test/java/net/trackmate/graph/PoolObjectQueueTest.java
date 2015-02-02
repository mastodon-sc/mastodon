package net.trackmate.graph;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import net.trackmate.graph.mempool.ByteMappedElement;
import net.trackmate.trackscheme.TrackSchemeGraph;
import net.trackmate.trackscheme.TrackSchemeVertex;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class PoolObjectQueueTest
{
	private PoolObjectQueue< TrackSchemeVertex, ByteMappedElement > queue;

	private ArrayList< TrackSchemeVertex > objects;

	@Before
	public void noSetup()
	{
		final TrackSchemeGraph graph = new TrackSchemeGraph();
		queue = new PoolObjectQueue< TrackSchemeVertex, ByteMappedElement >( graph.vertexPool );
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
		queue = null;
	}

	@Test
	public void offerTest()
	{
		for ( final TrackSchemeVertex o : objects )
		{
			queue.offer( o );
		}
		final TrackSchemeVertex ref = queue.createRef();
		assertEquals( queue.peek( ref ), objects.get( 0 ) );
	}

	@Test
	public void pollTest()
	{
		for ( final TrackSchemeVertex o : objects )
		{
			queue.offer( o );
		}
		final TrackSchemeVertex ref = queue.createRef();
		for ( final TrackSchemeVertex o : objects )
		{
			assertEquals( o, queue.poll( ref ) );
		}
		assertTrue( queue.isEmpty() );
	}

	@Test
	public void peekTest()
	{
		final TrackSchemeVertex ref = queue.createRef();
		assertNull( queue.peek( ref ) );
	}

	@Test
	public void emptyTest()
	{
		assertTrue( queue.isEmpty() );
	}
}
