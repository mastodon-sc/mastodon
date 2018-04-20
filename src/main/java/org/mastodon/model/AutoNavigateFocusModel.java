package org.mastodon.model;

import org.mastodon.graph.Edge;
import org.mastodon.graph.Vertex;
import org.mastodon.util.Listeners;

/**
 * A {@code FocusModel} that calls {@code notifyNavigateToVertex()} on
 * {@code focusVertex()}.
 * <p>
 * This allows to implement view-follows-focus behaviour on demand (without
 * having to hard-wire navigation into {@code FocusActions}).
 */
public class AutoNavigateFocusModel< V extends Vertex< E >, E extends Edge< V > > implements FocusModel< V, E >
{
	private final FocusModel< V, E > focus;

	private final NavigationHandler< V, E > navigation;

	public AutoNavigateFocusModel(
			final FocusModel< V, E > focus,
			final NavigationHandler< V, E > navigation )
	{
		this.focus = focus;
		this.navigation = navigation;
	}

	@Override
	public void focusVertex( final V vertex )
	{
		focus.focusVertex( vertex );
		navigation.notifyNavigateToVertex( vertex );
	}

	@Override
	public V getFocusedVertex( final V ref )
	{
		return focus.getFocusedVertex( ref );
	}

	@Override
	public Listeners< FocusListener > listeners()
	{
		return focus.listeners();
	}
}
