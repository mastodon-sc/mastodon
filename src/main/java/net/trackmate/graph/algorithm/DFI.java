package net.trackmate.graph.algorithm;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.Set;

import net.trackmate.graph.CollectionUtils;
import net.trackmate.graph.Edge;
import net.trackmate.graph.Vertex;

public class DFI< V extends Vertex< E >, E extends Edge< V >> implements Iterator< V >
{

	private Deque< V > stack;

	private V next;

	private boolean hasNext;

	private Set< V > visited;

	public DFI( final V root )
	{
		this.visited = CollectionUtils.createSet( root );
		this.stack = new ArrayDeque< V >();
		stack.push( root );
		this.hasNext = true;
		fetchNext();
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
		return returned;
	}

	protected void fetchNext()
	{
		if ( stack.size() < 1 )
		{
			hasNext = false;
			release();
			return;
		}

		next = stack.pop();
		if ( !visited.contains( next ) )
		{
			visited.add( next );
			for ( final E e : next.outgoingEdges() )
			{
				final V target = e.getTarget();
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
		stack = null;
		visited = null;
	}

}
