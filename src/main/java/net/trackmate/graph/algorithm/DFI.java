package net.trackmate.graph.algorithm;

import java.util.Iterator;

import net.trackmate.graph.Edge;
import net.trackmate.graph.Graph;
import net.trackmate.graph.Vertex;
import net.trackmate.graph.collection.CollectionUtils;
import net.trackmate.graph.collection.RefSet;
import net.trackmate.graph.collection.RefStack;

public class DFI< V extends Vertex< E >, E extends Edge< V >> implements Iterator< V >
{

	private RefStack< V > stack;

	/**
	 * The ref that will be returned by {@link #next()}.
	 */
	private V next;

	private boolean hasNext;

	private RefSet< V > visited;

	/**
	 * A utility ref.
	 */
	private final V v;

	public DFI( final V root, final Graph< V, E > graph )
	{
		this.visited = CollectionUtils.createVertexSet( graph );
		this.stack = CollectionUtils.createVertexStack( graph );
		stack.push( root );
		this.hasNext = true;
		// Allocate references
		next = root;
		v = graph.vertexRef();
	}

	@Override
	public boolean hasNext()
	{
		return hasNext;
	}

	@Override
	public V next()
	{
		final V returned = next;
		fetchNext();
		if ( stack.isEmpty() )
		{
			hasNext = false;
			release();
		}
		return returned;
	}

	protected void fetchNext()
	{
		next = stack.pop( next );
		if ( !visited.contains( next ) )
		{
			visited.add( next );
			for ( final E e : next.outgoingEdges() )
			{
				final V target = e.getTarget( v );
				stack.push( target );
			}
		}
		else
		{
			fetchNext();
		}
	}

	@Override
	public void remove()
	{
		throw new UnsupportedOperationException( "Remove is not supported for DepthFirstIterator." );
	}

	/**
	 * Do this when you are done with the iterator.
	 */
	public void release()
	{
		stack.releaseRef( next );
		stack.releaseRef( v );
		stack = null;
		visited = null;
	}

}
