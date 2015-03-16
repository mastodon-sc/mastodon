package net.trackmate.graph.algorithm;

import java.util.Iterator;

import net.trackmate.graph.Edge;
import net.trackmate.graph.Graph;
import net.trackmate.graph.PoolObject;
import net.trackmate.graph.Vertex;
import net.trackmate.graph.collection.RefDeque;
import net.trackmate.graph.collection.RefSet;

public class BreadthFirstIterator< V extends Vertex< E >, E extends Edge< V > > extends AbstractGraphIteratorAlgorithm< V, E > implements Iterator< V >
{
	private V next;

	private final V tmpRef;

	private final RefSet< V > visited;

	private final RefDeque< V > queue;

	public BreadthFirstIterator(  final V root, final Graph< V, E > graph)
	{
		super( graph );
		this.visited = createVertexSet();
		this.queue = createVertexDeque();
		queue.offer( root );
		this.next = vertexRef();
		this.tmpRef = vertexRef();
	}

	@Override
	public boolean hasNext()
	{
		return !queue.isEmpty();
	}

	@Override
	public V next()
	{
		while ( !queue.isEmpty() )
		{
			next = queue.poll( next );
			for ( final E e : next.outgoingEdges() )
			{
				final V target = e.getTarget( tmpRef );
				if ( !visited.contains( target ) )
				{
					visited.add( target );
					queue.offer( target );
				}
			}
			return next;
		}
		releaseRef( tmpRef );
		return null;
	}

	@Override
	public void remove()
	{
		throw new UnsupportedOperationException( "Remove is not supported for BreadthFirstIterator." );
	}

	@Override
	public boolean isRefIterator()
	{
		return next instanceof PoolObject;
	}

	public static < V extends Vertex< E >, E extends Edge< V > > BreadthFirstIterator< V, E > create( final V root, final Graph< V, E > graph )
	{
		return new BreadthFirstIterator< V, E >( root, graph );
	}
}
