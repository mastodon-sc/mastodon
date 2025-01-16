/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2025 Tobias Pietzsch, Jean-Yves Tinevez
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
package org.mastodon.model.branch;

import org.mastodon.graph.Edge;
import org.mastodon.graph.GraphIdBimap;
import org.mastodon.graph.ReadOnlyGraph;
import org.mastodon.graph.Vertex;
import org.mastodon.graph.branch.BranchGraph;
import org.mastodon.model.NavigationHandler;
import org.mastodon.model.NavigationListener;
import org.scijava.listeners.Listeners;

public class BranchGraphNavigationHandlerAdapter<
		V extends Vertex< E >,
		E extends Edge< V >,
		BV extends Vertex< BE >,
		BE extends Edge< BV > >
		extends AbstractBranchGraphAdapter< V, E, BV, BE >
		implements NavigationHandler< BV, BE >
{

	private final NavigationHandler< V, E > navigation;

	private final Listeners< NavigationListener< BV, BE > > listeners;

	public BranchGraphNavigationHandlerAdapter(
			final BranchGraph< BV, BE, V, E > branchGraph,
			final ReadOnlyGraph< V, E > graph,
			final GraphIdBimap< V, E > idBimap,
			final NavigationHandler< V, E > navigation )
	{
		super( branchGraph, graph, idBimap );
		this.navigation = navigation;
		this.listeners = new MyListeners( navigation.listeners() );
	}

	@Override
	public void notifyNavigateToVertex( final BV vertex )
	{
		final V vref = graph.vertexRef();
		final V v = branchGraph.getLastLinkedVertex( vertex, vref );
		if ( isValid( v ) )
			navigation.notifyNavigateToVertex( v );
		graph.releaseRef( vref );
	}

	@Override
	public void notifyNavigateToEdge( final BE edge )
	{
		final E eref = graph.edgeRef();
		final E e = branchGraph.getLinkedEdge( edge, eref );
		if ( isValid( e ) )
			navigation.notifyNavigateToEdge( e );
		graph.releaseRef( eref );
	}

	@Override
	public Listeners< NavigationListener< BV, BE > > listeners()
	{
		return listeners;
	}

	private class MyListeners implements Listeners< NavigationListener< BV, BE > >
	{

		private final Listeners< NavigationListener< V, E > > wrapped;

		public MyListeners( final Listeners< NavigationListener< V, E > > wrapped )
		{
			this.wrapped = wrapped;
		}

		@Override
		public boolean add( final NavigationListener< BV, BE > l )
		{
			return wrapped.add( translate( l ) );
		}

		@Override
		public boolean add( final int i, final NavigationListener< BV, BE > l )
		{
			return wrapped.add( i, translate( l ) );
		}

		@Override
		public boolean remove( final NavigationListener< BV, BE > l )
		{
			return wrapped.remove( translate( l ) );
		}

		private NavigationListener< V, E > translate( final NavigationListener< BV, BE > l )
		{
			return new NavigationListenerBranchTranslator( l );
		}
	}

	/**
	 * Translate a listener for the branch graph to a listener for the linked
	 * graph.
	 *
	 * @author Jean-Yves Tinevez
	 */
	private class NavigationListenerBranchTranslator implements NavigationListener< V, E >
	{

		private final NavigationListener< BV, BE > listener;

		public NavigationListenerBranchTranslator( final NavigationListener< BV, BE > listener )
		{
			this.listener = listener;
		}

		@Override
		public void navigateToVertex( final V vertex )
		{
			final BV bvref = branchGraph.vertexRef();
			try
			{
				final BV bv = branchGraph.getBranchVertex( vertex, bvref );
				if ( bv != null )
					listener.navigateToVertex( bv );
			}
			finally
			{
				branchGraph.releaseRef( bvref );
			}
		}

		@Override
		public void navigateToEdge( final E edge )
		{
			final BE beref = branchGraph.edgeRef();
			final BV bvref = branchGraph.vertexRef();
			try
			{
				final BE be = branchGraph.getBranchEdge( edge, beref );
				if ( be != null )
					listener.navigateToEdge( be );
				final BV bv = branchGraph.getBranchVertex( edge, bvref );
				if ( bv != null )
					listener.navigateToVertex( bv );
			}
			finally
			{
				branchGraph.releaseRef( beref );
				branchGraph.releaseRef( bvref );
			}
		}
	}
}
