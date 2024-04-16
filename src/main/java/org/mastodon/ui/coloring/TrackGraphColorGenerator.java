/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2024 Tobias Pietzsch, Jean-Yves Tinevez
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
import org.mastodon.collection.RefIntMap;
import org.mastodon.collection.RefList;
import org.mastodon.collection.RefMaps;
import org.mastodon.graph.Edge;
import org.mastodon.graph.ListenableReadOnlyGraph;
import org.mastodon.graph.Vertex;
import org.mastodon.graph.algorithm.traversal.InverseDepthFirstIterator;
import org.mastodon.model.HasLabel;
import org.mastodon.views.trackscheme.util.AlphanumCompare;

public class TrackGraphColorGenerator< V extends Vertex< E > & HasLabel, E extends Edge< V > >
		implements GraphColorGenerator< V, E >
{

	private final RefIntMap< V > cache;

	private final GlasbeyLut lut;

	private final InverseDepthFirstIterator< V, E > iterator;

	private final RefList< V > toUpdate;

	private final ListenableReadOnlyGraph< V, E > graph;

	private final RootProvider< V, E > rootsProvider;

	private boolean rootsUpToDate;

	public TrackGraphColorGenerator( final ListenableReadOnlyGraph< V, E > graph )
	{
		this.rootsProvider = new RootProvider<>( graph );
		this.graph = graph;
		this.cache = RefMaps.createRefIntMap( graph.vertices(), 0 );
		this.lut = GlasbeyLut.create();
		this.iterator = new InverseDepthFirstIterator<>( graph );
		this.toUpdate = RefCollections.createRefList( graph.vertices() );

		rootsProvider.graphRebuilt();
		graph.addGraphListener( rootsProvider );

		rootsProvider.listeners().add( () -> rootsUpToDate = false );
		rootsUpToDate = false;
		rebuildRoots();
	}

	@Override
	public int color( final V v )
	{
		// Should we clear the cache?
		if ( !rootsUpToDate )
			rebuildRoots();

		// Do we know of a color already for this vertex?
		if ( cache.containsKey( v ) )
			return cache.get( v );

		return updateCache( v );
	}

	/**
	 * Iterates until we find a spot with a color. The roots have already a
	 * color by then, so at worst we will have to iterate depth-first, upward,
	 * until we meet a root. Then we will return its color, but before, we will
	 * store this color in all the vertices we have encountered during
	 * iteration.
	 */
	private synchronized int updateCache( final V v )
	{
		if ( cache.containsKey( v ) )
			return cache.get( v );
		iterator.reset( v );
		toUpdate.clear();
		toUpdate.add( v );
		while ( iterator.hasNext() )
		{
			final V n = iterator.next();
			if ( cache.containsKey( n ) )
			{
				final int c = cache.get( n );
				// Sets the color of the vertices we traversed.
				toUpdate.forEach( m -> cache.put( m, c ) );
				return c;
			}
			else
			{
				toUpdate.add( n );
			}
		}
		// Failed to find a root?!
		return 0;
	}

	@Override
	public int color( final E edge, final V source, final V target )
	{
		return color( source );
	}

	/**
	 * Only one thread can regenerate the cache. If another one tries at the
	 * same time, it is paused (synchronized keyword) and will access the new
	 * value in the cache after the first has regenerated it.
	 */
	private synchronized void rebuildRoots()
	{
		if ( rootsUpToDate )
			return;

		final RefList< V > sortedRoots = RefCollections.createRefList( graph.vertices() );
		sortedRoots.addAll( rootsProvider.get() );
		sortedRoots.sort( ( v1, v2 ) -> AlphanumCompare.compare( v1.getLabel(), v2.getLabel() ) );
		cache.clear();
		lut.reset();
		sortedRoots.forEach( r -> cache.put( r, lut.next() ) );
		rootsUpToDate = true;
	}

	public void close()
	{
		graph.removeGraphListener( rootsProvider );
	}
}
