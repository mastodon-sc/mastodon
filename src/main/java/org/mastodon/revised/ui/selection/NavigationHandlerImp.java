package org.mastodon.revised.ui.selection;

import java.util.ArrayList;

import org.mastodon.revised.ui.grouping.GroupHandle;

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

	private final ArrayList< NavigationListener< V, E > > listeners;

	public NavigationHandlerImp( final GroupHandle groupHandle )
	{
		this.group = groupHandle;
		this.listeners = new ArrayList<>();
		group.add( this );
	}

	/**
	 * Registers the specified listener to this handler. The listener is
	 * notified of {@code navigateToVertex} events originating from any
	 * NavigationHandler in a shared group with this {@link NavigationHandlerImp}.
	 *
	 * @param listener
	 *            the {@link NavigationListener} to register.
	 * @return {@code true} if the specified listener was added to the
	 *         listeners of this handler. {@code false} if the specified
	 *         listener was already registered.
	 */
	@Override
	public synchronized boolean addNavigationListener( final NavigationListener< V, E > listener )
	{
		if ( !listeners.contains( listener ) )
		{
			listeners.add( listener );
			return true;
		}
		return false;
	}

	@Override
	public synchronized boolean removeNavigationListener( final NavigationListener< V, E > l )
	{
		return listeners.remove( l );
	}

	@Override
	public void notifyNavigateToVertex( final V vertex )
	{
		for( final NavigationHandlerImp< V, E > handler : group.allInSharedGroups( this ) )
			for ( final NavigationListener< V, E > listener : handler.listeners )
				listener.navigateToVertex( vertex );
	}

	@Override
	public void notifyNavigateToEdge( final E edge )
	{
		for( final NavigationHandlerImp< V, E > handler : group.allInSharedGroups( this ) )
			for ( final NavigationListener< V, E > listener : handler.listeners )
				listener.navigateToEdge( edge );
	}
}
