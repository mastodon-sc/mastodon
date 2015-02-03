package net.trackmate.graph.object;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Stack;

public class DepthFirstIterator< K > implements Iterator< ObjectVertex< K > >
{

	private Set< ObjectVertex< K > > visited;

	private Stack< ObjectVertex< K > > stack;

	private ObjectVertex< K > next;

	private boolean hasNext;


	public DepthFirstIterator( final ObjectVertex< K > startVertex )
	{
		this.visited = new HashSet< ObjectVertex< K > >();
		this.stack = new Stack< ObjectVertex< K > >();
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
		stack = null;
		visited = null;
	}

	protected void fetchNext()
	{
		if ( stack.isEmpty() )
		{
			hasNext = false;
			release();
			return;
		}

		next = stack.pop();
		if ( !visited.contains( next ) )
		{
			visited.add( next );
			for ( final ObjectEdge< K > e : next.outgoingEdges() )
			{
				final ObjectVertex< K > vertex = e.getTarget();
				stack.push( vertex );
			}
		}
		else
		{
			fetchNext();
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
		throw new UnsupportedOperationException( "Remove is not supported for DepthFirstIterator." );
	}
}
