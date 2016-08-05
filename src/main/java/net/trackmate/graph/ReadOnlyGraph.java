package net.trackmate.graph;

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
	/**
	 * Returns the directed edge from vertex {@code source} to {@code target} if
	 * it exists, or {@code null} otherwise.
	 *
	 * @param source
	 *            the source vertex of the directed edge.
	 * @param target
	 *            the target vertex of the directed edge.
	 * @return the directed edge from vertex {@code source} to {@code target} if
	 *         it exists, or {@code null} otherwise.
	 */
	public E getEdge( final V source, final V target );

	/**
	 * Returns the directed edge from vertex {@code source} to {@code target} if
	 * it exists, or {@code null} otherwise.
	 *
	 * <p>
	 * This method is a (potentially) allocation-free version of
	 * {@link #getEdge(Vertex, Vertex)}
	 *
	 * @param source
	 *            the source vertex of the directed edge.
	 * @param target
	 *            the target vertex of the directed edge.
	 * @param ref
	 *            an edge reference that can be used for retrieval. Depending on
	 *            concrete implementation, this object can be cleared, ignored
	 *            or re-used.
	 * @return the directed edge from vertex {@code source} to {@code target} if
	 *         it exists, or {@code null} otherwise. The object actually
	 *         returned might be the specified {@code ref}, depending on
	 *         concrete implementation.
	 */
	public E getEdge( final V source, final V target, final E ref );

	/**
	 * Generates a vertex reference that can be used for retrieval. Depending on
	 * concrete implementation this method may return {@code null.}
	 *
	 * @return a new, uninitialized, vertex reference.
	 */
	public V vertexRef();

	/**
	 * Generates an edge reference that can be used for retrieval. Depending on
	 * concrete implementation this method may return {@code null.}
	 *
	 * @return a new, uninitialized, edge reference.
	 */
	public E edgeRef();

	/**
	 * Releases a previously created vertex reference. Depending on concrete
	 * implementation, this method might not do anything.
	 *
	 * @param ref
	 *            the vertex reference to release.
	 */
	public void releaseRef( final V ref );

	/**
	 * Releases a previously created edge reference. Depending on concrete
	 * implementation, this method might not do anything.
	 *
	 * @param ref
	 *            the edge reference to release.
	 */
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
