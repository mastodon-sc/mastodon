/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2021 Tobias Pietzsch, Jean-Yves Tinevez
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
package org.mastodon.model;

import org.mastodon.grouping.ForwardingModel;
import org.mastodon.grouping.GroupManager;
import org.mastodon.grouping.GroupableModelFactory;
import org.scijava.listeners.Listeners;

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
			return new DefaultNavigationHandler<>();
		}

		@Override
		public ForwardingModel< NavigationHandler< V, E > > createForwardingModel()
		{
			return new ForwardingNavigationHandler<>();
		}
	};
}
