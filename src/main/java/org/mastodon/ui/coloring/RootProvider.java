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
package org.mastodon.ui.coloring;

import org.mastodon.collection.RefCollections;
import org.mastodon.collection.RefSet;
import org.mastodon.graph.Edge;
import org.mastodon.graph.GraphListener;
import org.mastodon.graph.ReadOnlyGraph;
import org.mastodon.graph.Vertex;
import org.mastodon.graph.algorithm.RootFinder;
import org.scijava.listeners.Listeners;

/**
 * A class that listens to a graph to provide the set if its roots.
 * <p>
 * The root set is updated by listening to graph changes. So to work, an
 * instance of this class <b>must</b> be first registered as a
 * {@link GraphListener} to the graph it will analyze, and then have its
 * {@link #graphRebuilt()} method called once.
 * <p>
 * Classes that use this root provider can register as a listener to it. They
 * will be notified when the set of roots have changed. Not all changes in the
 * graph result in a change in the root set.
 * 
 * @param <V>
 *            the type of vertices in the graph.
 * @param <E>
 *            the type of edges in the graph.
 */
public class RootProvider< V extends Vertex< E >, E extends Edge< V > > implements GraphListener< V, E >
{

	public interface RootsListener
	{
		void rootsChanged();
	}

	private final RefSet< V > roots;

	private final Listeners.List< RootsListener > listeners;

	private final ReadOnlyGraph< V, E > graph;

	public RootProvider( final ReadOnlyGraph< V, E > graph )
	{
		this.graph = graph;
		this.roots = RefCollections.createRefSet( graph.vertices() );
		this.listeners = new Listeners.List<>();
	}

	public Listeners.List< RootsListener > listeners()
	{
		return listeners;
	}

	private void notifyListeners()
	{
		listeners.list.forEach( l -> l.rootsChanged() );
	}

	@Override
	public void graphRebuilt()
	{
		roots.clear();
		roots.addAll( RootFinder.getRoots( graph ) );
		notifyListeners();
	}

	@Override
	public void vertexAdded( final V v )
	{
		roots.add( v );
		notifyListeners();
	}

	@Override
	public void vertexRemoved( final V v )
	{
		if ( roots.remove( v ) )
			notifyListeners();
	}

	@Override
	public void edgeAdded( final E e )
	{
		final V ref = graph.vertexRef();
		final V target = e.getTarget( ref );
		// Cannot be a root anymore
		if ( roots.remove( target ) )
			notifyListeners();
		graph.releaseRef( ref );
	}

	@Override
	public void edgeRemoved( final E e )
	{
		final V ref = graph.vertexRef();
		final V target = e.getTarget( ref );
		// Is it the last edge -> if yes it will become a root.
		if ( target.incomingEdges().size() == 1 )
		{
			roots.add( target );
			notifyListeners();
		}
		graph.releaseRef( ref );
	}

	public RefSet< V > get()
	{
		return roots;
	}
}
