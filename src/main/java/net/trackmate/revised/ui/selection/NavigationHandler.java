package net.trackmate.revised.ui.selection;

import gnu.trove.iterator.TIntIterator;
import gnu.trove.set.TIntSet;

import java.util.HashMap;
import java.util.HashSet;

import net.trackmate.graph.Vertex;

/**
 *
 * @author Jean-Yves Tinevez
 *
 * @param <V>
 *            the type of the model vertices.
 */
public class NavigationHandler< V extends Vertex< ? > >
{
	private final HashMap< NavigationListener< V >, NavigationGroupHandler > listeners = new HashMap< NavigationListener< V >, NavigationGroupHandler >();

	/**
	 * Registers the specified listener to this handler. The specified
	 * {@link NavigationGroupReceiver} will be used to determine to what groups this
	 * listener belongs to when passing navigation events.
	 *
	 * @param l
	 *            the {@link NavigationListener} to register.
	 * @param g
	 *            the {@link NavigationGroupReceiver} that determines to what groups it
	 *            belongs.
	 */
	public void addNavigationListener( final NavigationListener< V > l, final NavigationGroupHandler g )
	{
		listeners.put( l, g );
	}

	public boolean removeNavigationListener( final NavigationListener< V > l )
	{
		return listeners.remove( l ) != null;
	}

	/**
	 * Notifies the registered listeners of this handler to center the view they
	 * managed on the vertex with the specified model id. Only the listener that
	 * belong to the specified groups will be notified.
	 *
	 * @param fromGroups
	 *            the listener groups to notify.
	 * @param vertex
	 *            the model vertex to center on.
	 */
	public void notifyListeners( final TIntSet fromGroups, final V vertex )
	{
		// Make sure listeners are notified only once even if they belong to
		// several groups.
		final HashSet< NavigationListener< V > > toNotify = new HashSet< NavigationListener< V > >();
		final TIntIterator it = fromGroups.iterator();
		while ( it.hasNext() )
		{
			final int group = it.next();
			for ( final NavigationListener< V > l : listeners.keySet() )
			{
				final NavigationGroupHandler g = listeners.get( l );
				if ( g.isInGroup( group ) )
				{
					toNotify.add( l );
				}
			}
		}

		for ( final NavigationListener< V > l : toNotify )
		{
			l.navigateToVertex( vertex );
		}
	}
}
