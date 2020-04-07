package org.mastodon.model;

import org.scijava.listeners.Listeners;

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
public class DefaultNavigationHandler< V, E > implements NavigationHandler< V, E >
{
	private final Listeners.List< NavigationListener< V, E > > listeners = new Listeners.SynchronizedList<>();

	@Override
	public Listeners< NavigationListener< V, E > > listeners()
	{
		return listeners;
	}

	@Override
	public void notifyNavigateToVertex( final V vertex )
	{
		if ( vertex != null )
			listeners.list.forEach( l -> l.navigateToVertex( vertex ) );
	}

	@Override
	public void notifyNavigateToEdge( final E edge )
	{
		if ( edge != null )
			listeners.list.forEach( l -> l.navigateToEdge( edge ) );
	}
}
