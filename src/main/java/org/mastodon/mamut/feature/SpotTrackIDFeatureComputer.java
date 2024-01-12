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
package org.mastodon.mamut.feature;

import org.mastodon.collection.RefCollections;
import org.mastodon.collection.RefList;
import org.mastodon.graph.algorithm.RootFinder;
import org.mastodon.graph.algorithm.traversal.BreadthFirstSearch;
import org.mastodon.graph.algorithm.traversal.GraphSearch.SearchDirection;
import org.mastodon.graph.algorithm.traversal.SearchListener;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.properties.IntPropertyMap;
import org.mastodon.views.trackscheme.util.AlphanumCompare;
import org.scijava.ItemIO;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import io.humble.ferry.AtomicInteger;

@Plugin( type = MamutFeatureComputer.class )
public class SpotTrackIDFeatureComputer implements MamutFeatureComputer
{

	@Parameter
	private ModelGraph graph;

	@Parameter( type = ItemIO.OUTPUT )
	private SpotTrackIDFeature output;

	@Override
	public void createOutput()
	{
		if ( null == output )
			output = new SpotTrackIDFeature( new IntPropertyMap<>( graph.vertices().getRefPool(), -1 ) );
	}

	@Override
	public void run()
	{
		output.map.beforeClearPool();

		if ( graph.vertices().isEmpty() )
			return;

		/*
		 * Iterate over sorted roots, so that the track id has the same order
		 * than in default TrackScheme.
		 */
		final AtomicInteger trackID = new AtomicInteger( 0 );
		final RefList< Spot > sortedRoots = RefCollections.createRefList( graph.vertices() );
		sortedRoots.addAll( RootFinder.getRoots( graph ) );
		sortedRoots.sort( ( v1, v2 ) -> AlphanumCompare.compare( v1.getLabel(), v2.getLabel() ) );

		final BreadthFirstSearch< Spot, Link > search = new BreadthFirstSearch<>( graph, SearchDirection.UNDIRECTED );
		sortedRoots.forEach( r -> {
			search.setTraversalListener( new SearchListener< Spot, Link, BreadthFirstSearch< Spot, Link > >()
			{

				@Override
				public void processVertexLate( final Spot vertex, final BreadthFirstSearch< Spot, Link > search )
				{}

				@Override
				public void processVertexEarly( final Spot vertex, final BreadthFirstSearch< Spot, Link > search )
				{
					output.map.set( vertex, trackID.get() );
				}

				@Override
				public void processEdge( final Link edge, final Spot from, final Spot to, final BreadthFirstSearch< Spot, Link > search )
				{}

				@Override
				public void crossComponent( final Spot from, final Spot to, final BreadthFirstSearch< Spot, Link > search )
				{}
			} );
			search.start( r );
			trackID.incrementAndGet();
		} );
	}
}
