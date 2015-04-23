package net.trackmate.graph;

import java.util.Iterator;

/**
 * A graph consisting of vertices of type {@code V} and edges of type {@code E}.
 *
 * @param <V>
 *            the {@link Vertex} type of the {@link Graph}.
 * @param <E>
 *            the {@link Edge} type of the {@link Graph}.
 *
 * @author Tobias Pietzsch <tobias.pietzsch@gmail.com>
 */
public interface Graph< V extends Vertex< E >, E extends Edge< V > >
{
	public V addVertex();

	public V addVertex( final V vertex );

	public E addEdge( final V source, final V target );

	public E addEdge( final V source, final V target, final E edge );

	public E getEdge( final V source, final V target );

	public E getEdge( final V source, final V target, final E edge );

	public void remove( final V vertex );

	public void remove( final E edge );

	public void removeAllLinkedEdges( final V vertex );

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
