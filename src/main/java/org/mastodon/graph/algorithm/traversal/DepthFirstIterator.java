package org.mastodon.graph.algorithm.traversal;

import org.mastodon.collection.RefStack;
import org.mastodon.graph.Edge;
import org.mastodon.graph.Graph;
import org.mastodon.graph.ReadOnlyGraph;
import org.mastodon.graph.Vertex;

/**
 * A Depth-first iterator, that traverses edges only following their direction.
 * <p>
 * With {@code A -> B}, the iterator will move from A to B, but not from B
 * to A.
 *
 * @author Jean-Yves Tinevez
 *
 * @param <V>
 *            the type of the graph vertices iterated.
 * @param <E>
 *            the type of the graph edges iterated.
 */
public class DepthFirstIterator< V extends Vertex< E >, E extends Edge< V > > extends AbstractGraphIteratorAlgorithm< V, E >
{
	private final RefStack< V > stack;

	public DepthFirstIterator( final V root, final ReadOnlyGraph< V, E > graph )
	{
		super( graph );
		stack = createVertexStack();
		stack.push( root );
		fetchNext();
		visited.add( root );
	}

	@Override
	protected Iterable< E > neighbors( final V vertex )
	{
		return vertex.outgoingEdges();
	}

	@Override
	protected V fetch( final V ref )
	{
		return stack.pop( ref );
	}

	@Override
	protected void toss( final V vertex )
	{
		stack.push( vertex );
	}

	@Override
	protected boolean canFetch()
	{
		return !stack.isEmpty();
	}

	static < V extends Vertex< E >, E extends Edge< V > > DepthFirstIterator< V, E > create( final V root, final Graph< V, E > graph )
	{
		return new DepthFirstIterator< V, E >( root, graph );
	}
}
