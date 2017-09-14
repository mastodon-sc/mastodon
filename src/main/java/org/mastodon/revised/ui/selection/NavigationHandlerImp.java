package org.mastodon.revised.ui.selection;

import org.mastodon.revised.ui.grouping.GroupHandle;
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
	private final GroupHandle group;

	private final Listeners.List< NavigationListener< V, E > > listeners;

	public NavigationHandlerImp( final GroupHandle groupHandle )
	{
		this.group = groupHandle;
		this.listeners = new Listeners.SynchronizedList<>();
		group.add( this );
	}

	@Override
	public Listeners< NavigationListener< V, E > > listeners()
	{
		return listeners;
	}

	@Override
	public void notifyNavigateToVertex( final V vertex )
	{
		for( final NavigationHandlerImp< V, E > handler : group.allInSharedGroups( this ) )
			for ( final NavigationListener< V, E > listener : handler.listeners.list )
				listener.navigateToVertex( vertex );
	}

	@Override
	public void notifyNavigateToEdge( final E edge )
	{
		for( final NavigationHandlerImp< V, E > handler : group.allInSharedGroups( this ) )
			for ( final NavigationListener< V, E > listener : handler.listeners.list )
				listener.navigateToEdge( edge );
	}
}
