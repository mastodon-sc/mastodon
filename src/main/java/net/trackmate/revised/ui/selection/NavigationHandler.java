package net.trackmate.revised.ui.selection;

import java.util.ArrayList;

import net.trackmate.graph.Vertex;
import net.trackmate.revised.ui.grouping.GroupHandle;

// TODO remove "extends Vertex< ? > ?"
public class NavigationHandler< V extends Vertex< ? > >
{
	private final GroupHandle group;

	private final ArrayList< NavigationListener< V > > listeners;

	public NavigationHandler( final GroupHandle groupHandle )
	{
		this.group = groupHandle;
		this.listeners = new ArrayList<>();
		group.add( this );
	}

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
	public synchronized boolean addNavigationListener( final NavigationListener< V > listener )
	{
		if ( !listeners.contains( listener ) )
		{
			listeners.add( listener );
			return true;
		}
		return false;
	}

	public synchronized boolean removeNavigationListener( final NavigationListener< V > l )
	{
		return listeners.remove( l );
	}

	public void notifyNavigateToVertex( final V vertex )
	{
		for( final NavigationHandler< V > handler : group.allInSharedGroups( this ) )
			for ( final NavigationListener< V > listener : handler.listeners )
				listener.navigateToVertex( vertex );
	}
}
