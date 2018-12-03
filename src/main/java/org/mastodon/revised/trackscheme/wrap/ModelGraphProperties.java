package org.mastodon.revised.trackscheme.wrap;

import org.mastodon.revised.model.HasLabel;
import org.mastodon.revised.trackscheme.TrackSchemeGraph;
import org.mastodon.spatial.HasTimepoint;

/**
 * Interface for accessing model graph properties.
 * <p>
 * To make {@link TrackSchemeGraph} adaptable to various model graph type
 * without requiring the graph to implement specific interfaces, we access
 * properties of model vertices and edges (for example the label of a vertex)
 * through {@link ModelGraphProperties}.
 * <p>
 * For model graphs that implement the required additional interfaces (
 * {@link HasTimepoint}, {@link HasLabel}, etc),
 * {@link DefaultModelGraphProperties} can be used.
 *
 * @param <V>
 *            the type of vertices in the model graph (not the TrackScheme
 *            graph).
 * @param <E>
 *            the type of edges in the graph.
 *
 * @author Tobias Pietzsch
 */
public interface ModelGraphProperties< V, E >
{
	public int getTimepoint( V v );

	public String getLabel( V v );

	public void setLabel( V v, String label );

	public E addEdge( V source, V target, E ref );

	public E insertEdge( V source, final int sourceOutIndex, V target, final int targetInIndex, final E ref );

	public E initEdge( E e );

	public void removeEdge( E e );

	public void removeVertex( V v );

	public void notifyGraphChanged();
}
