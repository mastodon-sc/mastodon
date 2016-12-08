package org.mastodon.adapter;

import org.mastodon.revised.ui.selection.NavigationListener;

public class NavigationListenerAdapter< V, E, WV, WE >
		implements NavigationListener< V, E >
{
	private final NavigationListener< WV, WE > listener;

	private final RefBimap< V, WV > vertexMap;

	private final RefBimap< E, WE > edgeMap;

	public NavigationListenerAdapter(
			final NavigationListener< WV, WE > listener,
			final RefBimap< V, WV > vertexMap,
			final RefBimap< E, WE > edgeMap )
	{
		this.listener = listener;
		this.vertexMap = vertexMap;
		this.edgeMap = edgeMap;
	}

	@Override
	public void navigateToVertex( final V vertex )
	{
		final WV ref = vertexMap.reusableRightRef();
		listener.navigateToVertex( vertexMap.getRight( vertex, ref ) );
		vertexMap.releaseRef( ref );
	}

	@Override
	public void navigateToEdge( final E edge )
	{
		final WE ref = edgeMap.reusableRightRef();
		listener.navigateToEdge( edgeMap.getRight( edge, ref ) );
		edgeMap.releaseRef( ref );
	}

	@Override
	public int hashCode()
	{
		return listener.hashCode();
	}

	@Override
	public boolean equals( final Object obj )
	{
		return ( obj instanceof NavigationListenerAdapter )
				? listener.equals( ( ( NavigationListenerAdapter< ?, ?, ?, ? > ) obj ).listener )
				: false;
	}
}
