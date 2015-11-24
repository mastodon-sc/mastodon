package net.trackmate.revised.ui.selection;

import gnu.trove.iterator.TIntIterator;
import gnu.trove.set.TIntSet;

import java.util.ArrayList;
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

	private final ArrayList< NavigationListener > listeners = new ArrayList< NavigationListener >();

	public NavigationHandler()
	{
	}

	public boolean addNavigationListener( final NavigationListener l )
	{
		return listeners.add( l );
	}

	public boolean removeNavigationListener( final NavigationListener l )
	{
		return listeners.remove( l );
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
			for ( final NavigationListener l : listeners )
			{
				if ( l.isInGroup( group ) )
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
