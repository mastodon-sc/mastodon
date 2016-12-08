package org.mastodon.revised.ui.selection;

import org.mastodon.graph.Edge;
import org.mastodon.graph.Vertex;

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
	 * Registers a HighlightListener to this highlight model, that will be
	 * notified when the highlighted vertex changes.
	 *
	 * @param listener
	 *            the listener to register.
	 * @return {@code true} if the listener was successfully registered.
	 *         {@code false} if it was already registered.
	 */
	public boolean addHighlightListener( final HighlightListener listener );

	/**
	 * Removes the specified listener from the listeners of this highlight
	 * model.
	 *
	 * @param listener
	 *            the listener to remove.
	 * @return {@code true} if the listener was present in the listeners of
	 *         this model and was succesfully removed.
	 */
	public boolean removeHighlightListener( final HighlightListener listener );
}
