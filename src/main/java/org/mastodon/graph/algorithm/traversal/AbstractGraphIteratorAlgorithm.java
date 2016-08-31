package org.mastodon.graph.algorithm.traversal;

import java.util.Iterator;

import org.mastodon.collection.MaybeRefIterator;
import org.mastodon.collection.RefSet;
import org.mastodon.graph.Edge;
import org.mastodon.graph.ReadOnlyGraph;
import org.mastodon.graph.Vertex;
import org.mastodon.graph.algorithm.AbstractGraphAlgorithm;
import org.mastodon.pool.PoolObject;

public abstract class AbstractGraphIteratorAlgorithm< V extends Vertex< E >, E extends Edge< V > > extends AbstractGraphAlgorithm< V, E > implements MaybeRefIterator, Iterator< V >
{
	/**
	 * Keep track of visited vertices.
	 */
	protected final RefSet< V > visited;

	/**
	 * Is returned by {@link #next()}.
	 */
	protected V next;

	/**
	 * Will be returned by following {@link #next()}.
	 */
	protected V fetched;

	/**
	 * A utility ref.
	 */
	protected final V tmpRef;

	public AbstractGraphIteratorAlgorithm( final ReadOnlyGraph< V, E > graph )
	{
		super( graph );
		visited = createVertexSet();
		next = vertexRef();
		fetched = vertexRef();
		tmpRef = vertexRef();
	}

	@Override
	public boolean isRefIterator()
	{
		final V v = graph.vertexRef();
		final boolean isRefIterator = v != null && v instanceof PoolObject;
		graph.releaseRef( v );
		return isRefIterator;
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

	protected void fetchNext()
	{
		while ( canFetch() )
		{
			fetched = fetch( fetched );
			for ( final E e : neighbors( fetched ) )
			{
				final V target = targetOf( e, tmpRef );
				if ( !visited.contains( target ) )
				{
					visited.add( target );
					toss( target );
				}
			}
			return;
		}
		releaseRef( tmpRef );
		releaseRef( fetched );
		// we cannot release next, because it might still be in used outside of
		// the iterator
		fetched = null;
	}

	@Override
	public void remove()
	{
		throw new UnsupportedOperationException( "Remove is not supported for " + this.getClass() + "." );
	}

	/**
	 * Returns the target vertex of the specified edge.
	 * <p>
	 * By default, this method returns the actual target of the edge, through
	 * {@link Edge#getTarget(Vertex)}. Overriding this method allows for coding
	 * reverse iterators.
	 *
	 * @param edge
	 *            the edge to return the target of.
	 * @param ref
	 *            a reference object, that might be used or discarded by this
	 *            call.
	 * @return the target of the edge, as defined by this concrete iterator
	 *         implementation.
	 */
	protected V targetOf( E edge, V ref )
	{
		return edge.getTarget( tmpRef );
	}

	/**
	 * Returns the edges neighboring the specified vertex, in whatever sense the
	 * concrete implementation sees this neighborhood.
	 *
	 * @param vertex
	 *            the vertex.
	 * @return an {@link Iterable} over neighbor edges.
	 */
	protected abstract Iterable< E > neighbors( final V vertex );

	/**
	 * Returns a vertex to process by the iterator main loop.
	 *
	 * @param ref
	 *            a reference object, that might be used or discarded by this
	 *            call.
	 *
	 * @return a vertex, which neighbors will be inspected.
	 */
	protected abstract V fetch( V ref );

	/**
	 * Adds the specified vertex to the collection of vertices to process.
	 *
	 * @param vertex
	 *            the vertex to add.
	 */
	protected abstract void toss( V vertex );

	/**
	 * Returns whether more elements can be fetched for processing.
	 *
	 * @return {@code false} if there are no more vertices to process.
	 */
	protected abstract boolean canFetch();
}
