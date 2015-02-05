package net.trackmate.graph.algorithm.pool;

import java.util.Iterator;
import java.util.List;

import net.trackmate.graph.AbstractEdge;
import net.trackmate.graph.AbstractVertex;
import net.trackmate.graph.Edge;
import net.trackmate.graph.Pool;
import net.trackmate.graph.PoolObjectList;
import net.trackmate.graph.PoolObjectSet;
import net.trackmate.graph.mempool.MappedElement;

/**
 * A topological order sort for a direct acyclic graph.
 * <p>
 * If the graph provided is not acyclic, the flag returned by the
 * {@link #hasFailed()} method set to <code>true</code> to indicate the problem.
 *
 * @author Jean-Yves Tinevez
 *
 * @param <V>
 *            the vertices type.
 */
public class TopologicalSort< V extends AbstractVertex< V, ? extends AbstractEdge< ?, V, ? >, ? extends MappedElement > >
{

	private final Pool< V, ? > pool;

	private PoolObjectSet< V, ? > marked;

	private PoolObjectSet< V, ? > temporaryMarked;

	private boolean failed;

	private final PoolObjectList< V, ? > list;

	@SuppressWarnings( { "rawtypes", "unchecked" } )
	public TopologicalSort( final Pool< V, ? > pool )
	{
		this.pool = pool;
		this.failed = false;
		this.marked = new PoolObjectSet( pool );
		this.temporaryMarked = new PoolObjectSet( pool );
		this.list = new PoolObjectList( pool, pool.size() );
		fetchList();
	}

	/**
	 * Returns the topologically sorted vertices in a list.
	 * 
	 * @return the list resulting from this sort operation.
	 */
	public List< V > get()
	{
		return list;
	}

	/**
	 * Returns <code>true</code> if the graph iterated has a cycle.
	 *
	 * @return <code>true</code> if the graph iterated is not a directed acyclic
	 *         graph.
	 */
	public boolean hasFailed()
	{
		return failed;
	}

	private void release()
	{
		marked = null;
		temporaryMarked = null;
	}

	private void fetchList()
	{
		final Iterator< V > vertices = pool.iterator();
		final V v1 = pool.createRef();
		while ( vertices.hasNext() && !failed )
		{
			v1.refTo( vertices.next() );
			if ( !marked.contains( v1 ) )
			{
				visit( v1 );
			}
		}
		pool.releaseRef( v1 );
		release();
	}

	private void visit( final V vertex )
	{
		if ( temporaryMarked.contains( vertex ) )
		{
			failed = true;
			return;
		}

		if ( !marked.contains( vertex ) )
		{
			final V v2 = pool.createRef();
			temporaryMarked.add( vertex );
			for ( final Edge< V > e : vertex.outgoingEdges() )
			{
				e.getTarget(v2);
				visit( v2 );
			}
			pool.releaseRef( v2 );

			marked.add( vertex );
			temporaryMarked.remove( vertex );
			list.add( vertex );
		}
	}
}
