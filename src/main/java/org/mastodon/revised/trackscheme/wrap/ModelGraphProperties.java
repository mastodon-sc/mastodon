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
 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
 */
public interface ModelGraphProperties< V, E >
{
	public int getTimepoint( V v );

	public String getLabel( V v );

	// TODO move to separate interface? ModelGraphModifyProperties?
	public void setLabel( V v, String label );

	public void notifyGraphChanged();
}
