package net.trackmate.graph;

import java.util.Iterator;

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
 * @author Tobias Pietzsch <tobias.pietzsch@gmail.com>
 */
public interface ReadOnlyGraph< V extends Vertex< E >, E extends Edge< V > >
{
	public E getEdge( final V source, final V target );

	public E getEdge( final V source, final V target, final E edge );

	public V vertexRef();

	public E edgeRef();

	public void releaseRef( final V ref );

	public void releaseRef( final E ref );

	public void releaseRef( final V ... refs );

	public void releaseRef( final E ... refs );

	/**
	 * Returns an iterator that will iterate over all the vertices of this
	 * graph.
	 *
	 * @return a new {@link Iterator}.
	 */
	public Iterator< V > vertexIterator();

	/**
	 * Returns an iterator that will iterate over all the edges of this graph.
	 *
	 * @return a new {@link Iterator}.
	 */
	public Iterator< E > edgeIterator();

}
