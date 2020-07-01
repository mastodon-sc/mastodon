package org.mastodon.ui.coloring;

import org.mastodon.graph.Edge;
import org.mastodon.graph.Vertex;

/**
 * Interface that can associate colors to a graph's edges.
 *
 * @param <V>
 *            the type of the vertices.
 * @param <E>
 *            the type of the edges.
 *
 * @author Jean-Yves Tinevez.
 * @author Tobias Pietzsch
 */
public interface EdgeColorGenerator< V extends Vertex< E >, E extends Edge< V > >
{
	/**
	 * Gets the color for the specified edge (ARGB bytes packed into
	 * {@code int}).
	 * <p>
	 * The {@code source} and {@code target} vertices of the edge are provided
	 * for convenience, in case the edge coloring is determined by its vertices.
	 * <p>
	 * The special value {@code 0x00000000} is used to denote that no color is
	 * assigned to the edge (which should be drawn in default color then).
	 *
	 * @param edge
	 *            the edge
	 * @param source
	 *            the source vertex of the edge
	 * @param target
	 *            the target vertex of the edge
	 * @return a color (as ARGB bytes packed into {@code int}).
	 */
	int color( E edge, V source, V target );
}
