package org.mastodon.revised.ui.selection;

import org.mastodon.revised.ui.grouping.ForwardingModel;
import org.mastodon.revised.ui.grouping.GroupHandle;
import org.mastodon.revised.ui.grouping.GroupableModelFactory;
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
public class NavigationHandlerImp< V, E > implements NavigationHandler< V, E >
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
		listeners.list.forEach( l -> l.navigateToVertex( vertex ) );
	}

	@Override
	public void notifyNavigateToEdge( final E edge )
	{
		listeners.list.forEach( l -> l.navigateToEdge( edge ) );
	}
}
