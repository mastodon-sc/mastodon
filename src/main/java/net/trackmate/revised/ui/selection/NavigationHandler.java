package net.trackmate.revised.ui.selection;

import gnu.trove.iterator.TIntIterator;
import gnu.trove.set.TIntSet;

import java.util.HashMap;
import java.util.HashSet;

/**
 *
 * @author Jean-Yves Tinevez
 *
 * @param <V>
 *            the type of the model vertices.
 */
public class NavigationHandler
{
	private final HashMap< NavigationListener, NavigationGroupReceiver > listeners = new HashMap< NavigationListener, NavigationGroupReceiver >();

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
	public void addNavigationListener( final NavigationListener l, final NavigationGroupReceiver g )
	{
		listeners.put( l, g );
	}

	public boolean removeNavigationListener( final NavigationListener l )
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
	 * @param modelVertexId
	 *            the model if of the vertex to center on.
	 */
	public void notifyListeners( final TIntSet fromGroups, final int modelVertexId )
	{
		// Make sure listeners are notified only once even if they belong to
		// several groups.
		final HashSet< NavigationListener > toNotify = new HashSet< NavigationListener >();
		final TIntIterator it = fromGroups.iterator();
		while ( it.hasNext() )
		{
			final int group = it.next();
			for ( final NavigationListener l : listeners.keySet() )
			{
				final NavigationGroupReceiver g = listeners.get( l );
				if ( g.isInGroup( group ) )
				{
					toNotify.add( l );
				}
			}
		}

		for ( final NavigationListener l : toNotify )
		{
			l.navigateToVertex( modelVertexId );
		}
	}
}
