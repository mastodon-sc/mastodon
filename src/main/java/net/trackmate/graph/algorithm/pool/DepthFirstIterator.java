package net.trackmate.graph.algorithm.pool;

import java.util.Iterator;

import net.trackmate.graph.AbstractEdge;
import net.trackmate.graph.AbstractVertex;
import net.trackmate.graph.Pool;
import net.trackmate.graph.PoolObjectSet;
import net.trackmate.graph.PoolObjectStack;
import net.trackmate.graph.mempool.ByteMappedElement;
import net.trackmate.graph.mempool.MappedElement;
import net.trackmate.trackscheme.TrackSchemeEdge;
import net.trackmate.trackscheme.TrackSchemeGraph;
import net.trackmate.trackscheme.TrackSchemeVertex;

public class DepthFirstIterator< V extends AbstractVertex< V, E, T >, E extends AbstractEdge< E, V, T >, T extends MappedElement > implements Iterator< V >
{

	private PoolObjectSet< V, T > visited;

	private PoolObjectStack< V, T > stack;

	private final V returned;

	private final V next;

	private boolean hasNext;

	private final V v;

	private final Pool< V, T > pool;

	public DepthFirstIterator( final Pool< V, T > pool, final V startVertex )
	{
		this.pool = pool;
		this.visited = new PoolObjectSet< V, T >( pool );
		this.stack = new PoolObjectStack< V, T >( pool );
		this.returned = pool.createRef();
		this.next = pool.createRef();
		this.v = pool.createRef();
		this.hasNext = true;
		stack.push( startVertex );
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
		stack = null;
		visited = null;
	}

	protected void fetchNext()
	{
		if ( stack.size() < 1 )
		{
			hasNext = false;
			release();
			return;
		}

		stack.pop( v );
		if ( !visited.contains( v ) )
		{
			visited.add( v );
			next.refTo( v );
			for ( final E e : next.outgoingEdges() )
			{
				e.getTarget( v );
				stack.push( v );
			}
		}
		else
		{
			fetchNext();
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
		throw new UnsupportedOperationException( "Remove is not supported for DepthFirstIterator." );
	}

	public static void main( final String[] args )
	{
		final TrackSchemeGraph graph = new TrackSchemeGraph();

		final TrackSchemeVertex A = graph.addVertex().init( "A", 0, true );

		final TrackSchemeVertex B = graph.addVertex().init( "B", 1, true );
		graph.addEdge( A, B );

		final TrackSchemeVertex C = graph.addVertex().init( "C", 1, true );
		graph.addEdge( A, C );

		final TrackSchemeVertex E = graph.addVertex().init( "E", 1, true );
		graph.addEdge( A, E );

		final TrackSchemeVertex D = graph.addVertex().init( "D", 2, true );
		graph.addEdge( B, D );

		final TrackSchemeVertex F = graph.addVertex().init( "F", 2, true );
		graph.addEdge( B, F );

		final TrackSchemeVertex G = graph.addVertex().init( "G", 2, true );
		graph.addEdge( C, G );

		graph.addEdge( E, F );

//		new ShowTrackScheme( graph ); // does not work with non-tree graphs.

		final DepthFirstIterator< TrackSchemeVertex, TrackSchemeEdge, ByteMappedElement > iterator 
 = new DepthFirstIterator< TrackSchemeVertex, TrackSchemeEdge, ByteMappedElement >( graph.getVertexPool(), A );

		while ( iterator.hasNext() )
		{
			System.out.println( iterator.next().toString() );
		}
	}

}
