package net.trackmate.graph.object;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Queue;
import java.util.Set;

public class BreadthFirstIterator< K > implements Iterator< ObjectVertex< K > >
{

	private Set< ObjectVertex< K >> visited;

	private Queue< ObjectVertex< K > > queue;

	private ObjectVertex< K > next;

	private boolean hasNext;

	public BreadthFirstIterator( final ObjectVertex< K > startVertex )
	{
		this.visited = new HashSet< ObjectVertex< K > >();
		this.queue = new ArrayDeque< ObjectVertex< K > >();
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

		next = queue.poll();
		for ( final ObjectEdge< K > e : next.outgoingEdges() )
		{
			final ObjectVertex< K > target = e.getTarget();
			if ( !visited.contains( target ) )
			{
				visited.add( target );
				queue.offer( target );
			}
		}
	}

	@Override
	public ObjectVertex< K > next()
	{
		final ObjectVertex< K > returned = next;
		fetchNext();
		return returned;
	}

	@Override
	public void remove()
	{
		throw new UnsupportedOperationException( "Remove is not supported for BreadthFirstIterator." );
	}
}
