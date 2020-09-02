package org.mastodon.adapter;

import org.mastodon.model.NavigationHandler;
import org.mastodon.model.NavigationListener;
import org.scijava.listeners.Listeners;

public class NavigationHandlerAdapter< V, E, WV, WE >
		implements NavigationHandler< WV, WE >
{
	private final NavigationHandler< V, E > navigationHandler;

	private final RefBimap< V, WV > vertexMap;

	private final RefBimap< E, WE > edgeMap;

	public NavigationHandlerAdapter(
			final NavigationHandler< V, E > navigationHandler,
			final RefBimap< V, WV > vertexMap,
			final RefBimap< E, WE > edgeMap )
	{
		this.navigationHandler = navigationHandler;
		this.vertexMap = vertexMap;
		this.edgeMap = edgeMap;
	}

	@Override
	public void notifyNavigateToVertex( final WV vertex )
	{
		navigationHandler.notifyNavigateToVertex( vertexMap.getLeft( vertex ) );
	}

	@Override
	public void notifyNavigateToEdge( final WE edge )
	{
		navigationHandler.notifyNavigateToEdge( edgeMap.getLeft( edge ) );
	}

	private final ForwardedListeners< NavigationListener< WV, WE > > listeners = new ForwardedListeners.SynchronizedList<>(
			new Listeners< NavigationListener< WV, WE > >()
			{
				@Override
				public boolean add( final NavigationListener< WV, WE > listener )
				{
					return navigationHandler.listeners().add( new NavigationListenerAdapter<>( listener, vertexMap, edgeMap ) );
				}

				@Override
				public boolean add( final int index, final NavigationListener< WV, WE > listener )
				{
					return navigationHandler.listeners().add( index, new NavigationListenerAdapter<>( listener, vertexMap, edgeMap ) );
				}

				@Override
				public boolean remove( final NavigationListener< WV, WE > listener )
				{
					return navigationHandler.listeners().remove( new NavigationListenerAdapter<>( listener, vertexMap, edgeMap ) );
				}
			}
	);

	@Override
	public ForwardedListeners< NavigationListener< WV, WE > > listeners()
	{
		return listeners;
	}
}
