package org.mastodon.model;

import org.mastodon.util.Listeners;

/**
 * Class that centralizes receiving and sending navigation events to navigation
 * listeners.
 *
 * @param <V>
 *            the type of vertices.
 * @param <E>
 *            the type of edges.
 *
 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
 */
public interface NavigationHandler< V, E >
{
	public void notifyNavigateToVertex( final V vertex );

	public void notifyNavigateToEdge( final E edge );

	/**
	 * Get the list of navigation listeners. Add a {@link NavigationListener} to
	 * this list, for being notified about navigation events. Listeners are
	 * notified of {@code navigateToVertex} events originating from any
	 * NavigationHandler in a shared group with this {@link NavigationHandler}.
	 *
	 * @return the list of listeners
	 */
	public Listeners< NavigationListener< V, E > > listeners();
}
