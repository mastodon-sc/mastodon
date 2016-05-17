package net.trackmate.graph.zzgraphinterfaces;

import net.trackmate.collection.RefCollection;

/**
 * A read-only graph consisting of vertices of type {@code V} and edges of type
 * {@code E}. "Read-only" means that the graph cannot be modified through this
 * interface. However, this does not imply that the graph is immutable.
 *
 * @param <V>
 *            the {@link Vertex} type of the {@link ReadOnlyGraph}.
 * @param <E>
 *            the {@link Edge} type of the {@link ReadOnlyGraph}.
 *
 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
 */
public interface ReadOnlyGraph< V extends Vertex< E >, E extends Edge< V > >
{
	public E getEdge( final V source, final V target );

	public E getEdge( final V source, final V target, final E edge );

	public V vertexRef();

	public E edgeRef();

	public void releaseRef( final V ref );

	public void releaseRef( final E ref );

	/**
	 * Returns the vertices of this graph as an unmodifiable collection. In the
	 * returned {@link RefCollection}, only {@code isEmpty(),} {@code size(),}
	 * {@code iterator(),} {@code createRef()}, and {@code releaseRef()} are
	 * guaranteed to be implemented.
	 *
	 * @return unmodifiable collection of vertices. Only {@code isEmpty(),}
	 *         {@code size(),} {@code iterator(),} {@code createRef()}, and
	 *         {@code releaseRef()} are guaranteed to be implemented.
	 */
	public RefCollection< V > vertices();

	/**
	 * Returns the edges of this graph as an unmodifiable collection. In the
	 * returned {@link RefCollection}, only {@code isEmpty(),} {@code size(),}
	 * {@code iterator(),} {@code createRef()}, and {@code releaseRef()} are
	 * guaranteed to be implemented.
	 *
	 * @return unmodifiable collection of edges. Only {@code isEmpty(),}
	 *         {@code size(),} {@code iterator(),} {@code createRef()}, and
	 *         {@code releaseRef()} are guaranteed to be implemented.
	 */
	public RefCollection< E > edges();
}
