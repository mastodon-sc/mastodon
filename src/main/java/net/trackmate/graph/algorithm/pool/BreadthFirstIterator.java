package net.trackmate.graph.algorithm.pool;

import java.util.Iterator;

import net.trackmate.graph.AbstractEdge;
import net.trackmate.graph.AbstractVertex;
import net.trackmate.graph.Pool;
import net.trackmate.graph.PoolObjectQueue;
import net.trackmate.graph.PoolObjectSet;
import net.trackmate.graph.mempool.ByteMappedElement;
import net.trackmate.graph.mempool.MappedElement;
import net.trackmate.trackscheme.TrackSchemeEdge;
import net.trackmate.trackscheme.TrackSchemeGraph;
import net.trackmate.trackscheme.TrackSchemeVertex;

public class BreadthFirstIterator< V extends AbstractVertex< V, E, T >, E extends AbstractEdge< E, V, T >, T extends MappedElement > implements Iterator< V >
{


	private final Pool< V, T > pool;

	private PoolObjectSet< V, T > visited;

	private PoolObjectQueue< V, T > queue;

	private final V next;

	private final V returned;

	private final V v;

	private boolean hasNext;

	public BreadthFirstIterator( final Pool< V, T > pool, final V startVertex )
	{
		this.pool = pool;
		this.visited = new PoolObjectSet< V, T >( pool );
		this.queue = new PoolObjectQueue< V, T >( pool );
		this.returned = pool.createRef();
		this.next = pool.createRef();
		this.v = pool.createRef();
		this.hasNext = true;
		queue.offer( startVertex );
		fetchNext();
	}

	@Override
	public boolean hasNext()
	{
		return hasNext;
	}

	/**
	 * Do this when you are done with the iterator.
	 */
	public void release()
	{
		pool.releaseRef( returned );
		pool.releaseRef( next );
		pool.releaseRef( v );
		queue = null;
		visited = null;
	}

	protected void fetchNext()
	{
		if ( queue.isEmpty() )
		{
			hasNext = false;
			release();
			return;
		}

		queue.element( v );
		next.refTo( v );
		for ( final E e : next.outgoingEdges() )
		{
			e.getTarget( v );
			if ( !visited.contains( v ) )
			{
				visited.add( v );
				queue.offer( v );
			}
		}
	}

	@Override
	public V next()
	{
		returned.refTo( next );
		fetchNext();
		return returned;
	}

	@Override
	public void remove()
	{
		throw new UnsupportedOperationException( "Remove is not supported for BreadthFirstIterator." );
	}

	public static void main( final String[] args )
	{
		final TrackSchemeGraph graph = new TrackSchemeGraph();

		// We use X to store expected iteration order.

		final TrackSchemeVertex A = graph.addVertex().init( "A", 0, true );
		A.setLayoutX( 0 );

		final TrackSchemeVertex B = graph.addVertex().init( "B", 1, true );
		B.setLayoutX( 1 );
		graph.addEdge( A, B );

		final TrackSchemeVertex C = graph.addVertex().init( "C", 1, true );
		C.setLayoutX( 2 );
		graph.addEdge( A, C );

		final TrackSchemeVertex D = graph.addVertex().init( "D", 1, true );
		D.setLayoutX( 3 );
		graph.addEdge( A, D );

		final TrackSchemeVertex E = graph.addVertex().init( "E", 2, true );
		E.setLayoutX( 4 );
		graph.addEdge( B, E );

		final TrackSchemeVertex F = graph.addVertex().init( "F", 2, true );
		F.setLayoutX( 5 );
		graph.addEdge( B, F );

		final TrackSchemeVertex G = graph.addVertex().init( "G", 2, true );
		G.setLayoutX( 6 );
		graph.addEdge( D, G );

		final TrackSchemeVertex H = graph.addVertex().init( "H", 2, true );
		H.setLayoutX( 7 );
		graph.addEdge( D, H );

		final TrackSchemeVertex I = graph.addVertex().init( "I", 3, true );
		I.setLayoutX( 8 );
		graph.addEdge( E, I );

		final TrackSchemeVertex J = graph.addVertex().init( "J", 3, true );
		J.setLayoutX( 9 );
		graph.addEdge( E, J );

		final TrackSchemeVertex K = graph.addVertex().init( "K", 3, true );
		K.setLayoutX( 10 );
		graph.addEdge( E, K );

		final TrackSchemeVertex L = graph.addVertex().init( "L", 3, true );
		L.setLayoutX( 11 );
		graph.addEdge( G, L );

		final TrackSchemeVertex M = graph.addVertex().init( "M", 3, true );
		M.setLayoutX( 12 );
		graph.addEdge( G, M );

		final BreadthFirstIterator< TrackSchemeVertex, TrackSchemeEdge, ByteMappedElement > i1 = new BreadthFirstIterator< TrackSchemeVertex, TrackSchemeEdge, ByteMappedElement >( graph.getVertexPool(), A );
		while ( i1.hasNext() )
		{
			System.out.println( i1.next().toString() );
		}

		System.out.println();

		final DepthFirstIterator< TrackSchemeVertex, TrackSchemeEdge, ByteMappedElement > i2 = new DepthFirstIterator< TrackSchemeVertex, TrackSchemeEdge, ByteMappedElement >( graph.getVertexPool(), A );
		while ( i2.hasNext() )
		{
			System.out.println( i2.next().toString() );
		}

	}

}
