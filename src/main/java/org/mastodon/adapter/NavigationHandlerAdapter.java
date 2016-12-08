package org.mastodon.adapter;

import org.mastodon.revised.ui.selection.NavigationHandler;
import org.mastodon.revised.ui.selection.NavigationListener;

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

	@Override
	public boolean addNavigationListener( final NavigationListener< WV, WE > listener )
	{
		return navigationHandler.addNavigationListener( new NavigationListenerAdapter<>( listener, vertexMap, edgeMap ) );
	}

	@Override
	public boolean removeNavigationListener( final NavigationListener< WV, WE > listener )
	{
		return navigationHandler.removeNavigationListener( new NavigationListenerAdapter<>( listener, vertexMap, edgeMap ) );
	}
}
