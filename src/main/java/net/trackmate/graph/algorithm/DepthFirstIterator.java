package net.trackmate.graph.algorithm;

import java.util.Iterator;

import net.trackmate.graph.Edge;
import net.trackmate.graph.Graph;
import net.trackmate.graph.Vertex;
import net.trackmate.graph.collection.RefSet;
import net.trackmate.graph.collection.RefStack;

public class DepthFirstIterator< V extends Vertex< E >, E extends Edge< V > > extends AbstractGraphIteratorAlgorithm< V, E > implements Iterator< V >
{
	private final RefStack< V > stack;

	private final RefSet< V > visited;

	/**
	 * is returned by {@link #next()}.
	 */
	private V next;

	/**
	 * will be returned by following {@link #next()}.
	 */
	private V fetched;

	/**
	 * A utility ref.
	 */
	private final V tmpRef;

	public DepthFirstIterator( final V root, final Graph< V, E > graph )
	{
		super( graph );
		visited = createVertexSet();
		stack = createVertexStack();
		next = vertexRef();
		fetched = vertexRef();
		tmpRef = vertexRef();

		stack.push( root );
		fetchNext();
	}

	public static < V extends Vertex< E >, E extends Edge< V > > DepthFirstIterator< V, E > create( final V root, final Graph< V, E > graph )
	{
		return new DepthFirstIterator< V, E >( root, graph );
	}

	@Override
	public boolean hasNext()
	{
		return fetched != null;
	}

	@Override
	public V next()
	{
		next = assign( fetched, next );
		fetchNext();
		return next;
	}

	private void fetchNext()
	{
		while( !stack.isEmpty() )
		{
			fetched = stack.pop( fetched );
			if ( !visited.contains( fetched ) )
			{
				visited.add( fetched );
				for ( final E e : fetched.outgoingEdges() )
				{
					final V target = e.getTarget( tmpRef );
					stack.push( target );
				}
				return;
			}
		}
		releaseRef( tmpRef );
		releaseRef( fetched );
		// we cannot release next, because it might still be in used outside of the iterator
		fetched = null;
	}

	@Override
	public void remove()
	{
		throw new UnsupportedOperationException( "Remove is not supported for DepthFirstIterator." );
	}
}
