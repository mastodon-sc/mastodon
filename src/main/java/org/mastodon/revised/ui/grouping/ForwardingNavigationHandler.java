package org.mastodon.revised.ui.grouping;

import org.mastodon.model.NavigationHandler;
import org.mastodon.model.NavigationHandlerImp;
import org.mastodon.model.NavigationListener;
import org.mastodon.util.Listeners;

/**
 * A {@link NavigationHandler} forwarding to another (switchable)
 * {@link NavigationHandler}.
 * <p>
 * Used for grouping views, see {@link GroupManager}.
 *
 * @param <V>
 *            the type of vertices.
 * @param <E>
 *            the type of edges.
 *
 * @author Tobias Pietzsch
 */
public class ForwardingNavigationHandler< V, E > implements NavigationHandler< V, E >, NavigationListener< V, E >, ForwardingModel< NavigationHandler< V, E > >
{
	private NavigationHandler< V, E > handler;

	private final Listeners.List< NavigationListener< V, E > > listeners = new Listeners.SynchronizedList<>();

	@Override
	public Listeners< NavigationListener< V, E > > listeners()
	{
		return listeners;
	}

	@Override
	public void navigateToVertex( final V vertex )
	{
		listeners.list.forEach( l -> l.navigateToVertex( vertex ) );
	}

	@Override
	public void navigateToEdge( final E edge )
	{
		listeners.list.forEach( l -> l.navigateToEdge( edge ) );
	}

	@Override
	public void notifyNavigateToVertex( final V vertex )
	{
		handler.notifyNavigateToVertex( vertex );
	}

	@Override
	public void notifyNavigateToEdge( final E edge )
	{
		handler.notifyNavigateToEdge( edge );
	}

	@Override
	public void linkTo( final NavigationHandler< V, E > newHandler, final boolean copyCurrentStateToNewModel )
	{
		if ( handler != null )
			handler.listeners().remove( this );
		newHandler.listeners().add( this );
		handler = newHandler;
	}

	public static class Factory< V, E> implements GroupableModelFactory< NavigationHandler< V, E > >
	{
		@Override
		public NavigationHandler< V, E > createBackingModel()
		{
			return new NavigationHandlerImp<>();
		}

		@Override
		public ForwardingModel< NavigationHandler< V, E > > createForwardingModel()
		{
			return new ForwardingNavigationHandler<>();
		}
	};
}
