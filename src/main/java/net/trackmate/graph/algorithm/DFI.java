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
	private final RefStack< V > stack;

	private final RefSet< V > visited;

	private V next;

	public DFI( final V root, final Graph< V, E > graph )
	{
		this.visited = CollectionUtils.createVertexSet( graph );
		this.stack = CollectionUtils.createVertexStack( graph );
		stack.push( root );
		next = graph.vertexRef();
	}

	@Override
	public boolean hasNext()
	{
		return !stack.isEmpty();
	}

	@Override
	public V next()
	{
		while( !stack.isEmpty() )
		{
			next = stack.pop( next );
			if ( !visited.contains( next ) )
			{
				visited.add( next );
				for ( final E e : next.outgoingEdges() )
				{
					final V target = e.getTarget();
					stack.push( target );
				}
				return next;
			}
		}
		return null;
	}

	@Override
	public void remove()
	{
		throw new UnsupportedOperationException( "Remove is not supported for DepthFirstIterator." );
	}
}
