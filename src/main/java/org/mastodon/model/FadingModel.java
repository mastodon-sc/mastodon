package org.mastodon.model;

import org.mastodon.graph.Edge;
import org.mastodon.graph.Vertex;
import org.scijava.listeners.Listeners;

/**
 * A class that manages the fading of vertices and edges in a graph.
 * <p>
 *
 * @author Stefan Hahmann
 *
 * @param <V>
 *            the type of the vertices.
 * @param <E>
 *            the type of the edges.
 */
public interface FadingModel< V extends Vertex< E >, E extends Edge< V > > extends TimepointListener
{

	/**
	 * Get the faded state of a vertex.
	 *
	 * @param vertex
	 *            a vertex.
	 * @return {@code true} if the vertex is faded.
	 */
	boolean isFaded( final V vertex );

	/**
	 * Get the faded state of an edge.
	 *
	 * @param edge
	 *            an edge.
	 * @return {@code true} if the edge is faded.
	 */
	boolean isFaded( final E edge );

	Listeners< FadingListener > listeners();
}
