/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2023 Tobias Pietzsch, Jean-Yves Tinevez
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
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

	private final ForwardedListeners< NavigationListener< WV, WE > > listeners =
			new ForwardedListeners.SynchronizedList<>(
					new Listeners< NavigationListener< WV, WE > >()
					{
						@Override
						public boolean add( final NavigationListener< WV, WE > listener )
						{
							return navigationHandler.listeners()
									.add( new NavigationListenerAdapter<>( listener, vertexMap, edgeMap ) );
						}

						@Override
						public boolean add( final int index, final NavigationListener< WV, WE > listener )
						{
							return navigationHandler.listeners().add( index,
									new NavigationListenerAdapter<>( listener, vertexMap, edgeMap ) );
						}

						@Override
						public boolean remove( final NavigationListener< WV, WE > listener )
						{
							return navigationHandler.listeners()
									.remove( new NavigationListenerAdapter<>( listener, vertexMap, edgeMap ) );
						}
					}
			);

	@Override
	public ForwardedListeners< NavigationListener< WV, WE > > listeners()
	{
		return listeners;
	}
}
