package net.trackmate.graph.traversal;

import java.util.Comparator;
import java.util.Iterator;

import net.trackmate.graph.Edge;
import net.trackmate.graph.Graph;
import net.trackmate.graph.Vertex;
import net.trackmate.graph.collection.RefDeque;

/**
 * A sorted Breadth-first iterator, that traverses edges only following their
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
public class BreadthFirstIteratorSorted< V extends Vertex< E >, E extends Edge< V > > extends AbstractGraphSortedIteratorAlgorithm< V, E > implements Iterator< V >
{
	private final RefDeque< V > queue;

	/*
	 * CONSTRUCTORS
	 */

	public BreadthFirstIteratorSorted( final V root, final Graph< V, E > graph, final Comparator< V > comparator )
	{
		super( graph, comparator );
		this.queue = createVertexDeque();
		queue.offer( root );
		fetchNext();
		visited.add( root );
	}

	public BreadthFirstIteratorSorted( final V root, final Graph< V, E > graph )
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
		return queue.poll( ref );
	}

	@Override
	protected void toss( final V vertex )
	{
		queue.offer( vertex );
	}

	@Override
	protected boolean canFetch()
	{
		return !queue.isEmpty();
	}

	/*
	 * STATIC METHODS
	 */

	public static < V extends Vertex< E >, E extends Edge< V > > BreadthFirstIteratorSorted< V, E > create( final V root, final Graph< V, E > graph, final Comparator< V > comparator )
	{
		return new BreadthFirstIteratorSorted< V, E >( root, graph, comparator );
	}

	public static < V extends Vertex< E >, E extends Edge< V > > BreadthFirstIteratorSorted< V, E > create( final V root, final Graph< V, E > graph )
	{
		return new BreadthFirstIteratorSorted< V, E >( root, graph );
	}
}
