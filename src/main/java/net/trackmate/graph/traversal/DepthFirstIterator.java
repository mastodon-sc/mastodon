package net.trackmate.graph.traversal;

import net.trackmate.graph.Edge;
import net.trackmate.graph.Graph;
import net.trackmate.graph.Vertex;
import net.trackmate.graph.collection.RefStack;

/**
 * A Depth-first iterator, that traverses edges only following their direction.
 * <p>
 * With <code>A -> B</code>, the iterator will move from A to B, but not from B
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

	public DepthFirstIterator( final V root, final Graph< V, E > graph )
	{
		super( graph );
		stack = createVertexStack();
		stack.push( root );
		fetchNext();
		visited.add( root );
	}

	public static < V extends Vertex< E >, E extends Edge< V > > DepthFirstIterator< V, E > create( final V root, final Graph< V, E > graph )
	{
		return new DepthFirstIterator< V, E >( root, graph );
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
}
