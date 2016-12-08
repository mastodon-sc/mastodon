package org.mastodon.revised.ui.selection;

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
	 * Registers the specified listener to this handler. The listener is
	 * notified of {@code navigateToVertex} events originating from any
	 * NavigationHandler in a shared group with this {@link NavigationHandler}.
	 *
	 * @param listener
	 *            the {@link NavigationListener} to register.
	 * @return {@code true} if the specified listener was added to the
	 *         listeners of this handler. {@code false} if the specified
	 *         listener was already registered.
	 */
	public boolean addNavigationListener( final NavigationListener< V, E > listener );

	public boolean removeNavigationListener( final NavigationListener< V, E > listener );
}
