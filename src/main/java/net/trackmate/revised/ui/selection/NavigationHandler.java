package net.trackmate.revised.ui.selection;

import java.util.HashSet;
import java.util.Set;

import net.trackmate.graph.Vertex;
import net.trackmate.revised.ui.grouping.GroupHandle;

// TODO remove "extends Vertex< ? > ?"
public class NavigationHandler< V extends Vertex< ? > >
{
	private final GroupHandle group;

	private final Set< NavigationListener< V > > listeners;

	public NavigationHandler( final GroupHandle groupHandle )
	{
		this.group = groupHandle;
		this.listeners = new HashSet<>();
		group.add( this );
	}

	/**
	 * Registers the specified listener to this handler. The listener is
	 * notified of {@code navigateToVertex} events originating from any
	 * NavigationHandler in a shared group with this {@link NavigationHandler}.
	 *
	 * @param l
	 *            the {@link NavigationListener} to register.
	 */
	public void addNavigationListener( final NavigationListener< V > l )
	{
		listeners.add( l );
	}

	public boolean removeNavigationListener( final NavigationListener< V > l )
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
