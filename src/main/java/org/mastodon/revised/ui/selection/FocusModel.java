package org.mastodon.revised.ui.selection;

import org.mastodon.graph.Edge;
import org.mastodon.graph.Vertex;

/**
 * Class to manage the model vertex that has the "focus", regardless of how this
 * focus is used.
 *
 * @param <V>
 *            type of model vertices.
 * @param <E>
 *            the of model edges.
 */
// TODO: E parameter not needed
public interface FocusModel< V extends Vertex< E >, E extends Edge< V > >
{
	public void focusVertex( final V vertex );

	public V getFocusedVertex( final V ref );

	public boolean addFocusListener( final FocusListener listener );

	public boolean removeFocusListener( final FocusListener listener );
}
