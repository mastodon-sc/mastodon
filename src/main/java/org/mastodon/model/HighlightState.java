package org.mastodon.model;

public interface HighlightState< V, E >
{
	/**
	 * Returns the highlighted vertex.
	 *
	 * @param ref
	 *            a vertex reference used for retrieval.
	 * @return the highlighted vertex, or {@code null} if no vertex is
	 *         highlighted.
	 */
	public V getHighlightedVertex( final V ref );

	/**
	 * Returns the highlighted edge.
	 *
	 * @param ref
	 *            an edge reference used for retrieval.
	 * @return the highlighted edge, or {@code null} if no edge is highlighted.
	 */
	public E getHighlightedEdge( final E ref );
}
