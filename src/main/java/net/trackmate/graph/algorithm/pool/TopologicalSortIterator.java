package net.trackmate.graph.algorithm.pool;

import java.util.Iterator;

import net.trackmate.graph.AbstractEdge;
import net.trackmate.graph.AbstractVertex;
import net.trackmate.graph.Pool;
import net.trackmate.graph.PoolObjectSet;
import net.trackmate.graph.mempool.MappedElement;

public class TopologicalSortIterator< V extends AbstractVertex< V, E, T >, E extends AbstractEdge< E, V, T >, T extends MappedElement > implements Iterator< V >
{

	private final Pool< V, T > pool;

	private final Iterator< V > vertices;

	private final V returned;

	private final V next;

	private boolean hasNext;

	private final V v;

	private PoolObjectSet< V, T > marked;

	private PoolObjectSet< V, T > temporaryMarked;

	private boolean failed;

	public TopologicalSortIterator( final Pool< V, T > pool, final Iterator< V > vertices )
	{
		this.pool = pool;
		this.vertices = vertices;
		this.returned = pool.createRef();
		this.next = pool.createRef();
		this.v = pool.createRef();
		this.hasNext = true;
		this.failed = false;
		this.marked = new PoolObjectSet< V, T >( pool );
		this.temporaryMarked = new PoolObjectSet< V, T >( pool );
		fetchNext();
	}

	@Override
	public boolean hasNext()
	{
		return hasNext;
	}

	/**
	 * Returns <code>true</code> if the iterator stopped prematurely because the
	 * graph it iterates has a cycle.
	 * 
	 * @return <code>true</code> if the graph iterated is not a directed acyclic
	 *         graph.
	 */
	public boolean hasFailed()
	{
		return failed;
	}

	/**
	 * Do this when you are done with the iterator.
	 */
	public void release()
	{
		pool.releaseRef( returned );
		pool.releaseRef( next );
		pool.releaseRef( v );
		marked = null;
		temporaryMarked = null;
	}

	protected void fetchNext()
	{
		while ( vertices.hasNext() && !failed )
		{
			v.refTo( vertices.next() );
			if ( !marked.contains( v ) )
			{
				visit( v );
			}
		}
		hasNext = false;
		release();
	}

	protected void visit( final V vertex )
	{
		if ( temporaryMarked.contains( vertex ) )
		{
			failed = true;
			return;
		}

		if ( !marked.contains( vertex ) )
		{

		}
	}

	@Override
	public V next()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void remove()
	{
		throw new UnsupportedOperationException( "Remove is not supported for TopologicalSortIterator." );
	}

}
