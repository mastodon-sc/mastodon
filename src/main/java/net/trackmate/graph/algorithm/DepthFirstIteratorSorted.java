package net.trackmate.graph.algorithm;

import java.util.Comparator;

import net.trackmate.graph.Edge;
import net.trackmate.graph.Graph;
import net.trackmate.graph.Vertex;
import net.trackmate.graph.collection.RefStack;

/**
 * A sorted Depth-first iterator, that traverses edges only following their
 * direction. When meeting a vertex that have several non-visited children, they
 * will be visited according to an order specified by a {@link Comparator}.
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

public class DepthFirstIteratorSorted< V extends Vertex< E >, E extends Edge< V > > extends AbstractGraphSortedIteratorAlgorithm< V, E >
{
	private final RefStack< V > stack;

	/*
	 * CONSTRUCTORS
	 */

	/**
	 * Creates a new iterator, depth first, following edge direction. Careful:
	 * the successor vertices are visited in the <b>opposite</b> order than the
	 * one specified by the comparator.
	 *
	 * @param root
	 *            the vertex to start iteration with.
	 * @param graph
	 *            the graph to iterate.
	 * @param comparator
	 *            the comparator to use. Can be <code>null</code>, then natural
	 *            ordering will be used. Careful: the successor vertices are
	 *            visited in the <b>opposite</b> order than the one specified by
	 *            this comparator.
	 */
	public DepthFirstIteratorSorted( final V root, final Graph< V, E > graph, final Comparator< V > comparator )
	{
		super( graph, comparator );
		this.stack = createVertexStack();
		stack.push( root );
		fetchNext();
	}

	public DepthFirstIteratorSorted( final V root, final Graph< V, E > graph )
	{
		this( root, graph, null );
	}

	/*
	 * METHODS
	 */

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

	/*
	 * STATIC METHODS
	 */

	public static < V extends Vertex< E >, E extends Edge< V > > DepthFirstIteratorSorted< V, E > create( final V root, final Graph< V, E > graph, final Comparator< V > comparator )
	{
		return new DepthFirstIteratorSorted< V, E >( root, graph, comparator );
	}

	public static < V extends Vertex< E >, E extends Edge< V > > DepthFirstIteratorSorted< V, E > create( final V root, final Graph< V, E > graph )
	{
		return new DepthFirstIteratorSorted< V, E >( root, graph );
	}


}
