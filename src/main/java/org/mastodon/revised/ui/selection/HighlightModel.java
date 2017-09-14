package org.mastodon.revised.ui.selection;

import org.mastodon.graph.Edge;
import org.mastodon.graph.Vertex;
import org.mastodon.util.Listeners;

/**
 * Manages the highlighted vertex.
 * <p>
 * A highlighted vertex is a vertex that is drawn in a salient manner across all
 * the views opened on a single model. It is meant to quickly highlight a single
 * vertex on all views while the mouse is hovering above its representation in
 * any view.
 *
 * @param <V>
 *            the type of the model vertices.
 * @param <E>
 *            the type of the model edges.
 */
public interface HighlightModel< V extends Vertex< E >, E extends Edge< V > >
{
	/**
	 * Sets the specified vertex highlighted in this model.
	 *
	 * @param vertex
	 *            the vertex to highlight, or {@code null} to clear highlight.
	 */
	public void highlightVertex( final V vertex );

	/**
	 * Sets the specified edge highlighted in this model.
	 *
	 * @param edge
	 *            the edge to highlight, or {@code null} to clear highlight.
	 */
	public void highlightEdge( final E edge );

	/**
	 * Clear highlight.
	 */
	public void clearHighlight();

	/**
	 * Returns the vertex highlighted in this model.
	 *
	 * @param ref
	 *            a vertex reference used for retrieval.
	 * @return the highlighted vertex, or {@code null} if no vertex is
	 *         highlighted.
	 */
	public V getHighlightedVertex( final V ref );

	/**
	 * Returns the edge highlighted in this model.
	 *
	 * @param ref
	 *            an edge reference used for retrieval.
	 * @return the highlighted edge, or {@code null} if no edge is
	 *         highlighted.
	 */
	public E getHighlightedEdge( final E ref );

	/**
	 * Get the list of highlight listeners. Add a {@link HighlightListener} to
	 * this list, for being notified when the highlighted vertex/edge changes.
	 *
	 * @return the list of listeners
	 */
	public Listeners< HighlightListener > listeners();
}
