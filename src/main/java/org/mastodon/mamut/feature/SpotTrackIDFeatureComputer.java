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
package org.mastodon.mamut.feature;

import org.mastodon.graph.algorithm.traversal.BreadthFirstCrossComponentSearch;
import org.mastodon.graph.algorithm.traversal.GraphSearch.SearchDirection;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.graph.algorithm.traversal.SearchListener;
import org.mastodon.properties.IntPropertyMap;
import org.scijava.ItemIO;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

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
		final BreadthFirstCrossComponentSearch< Spot, Link > search = new BreadthFirstCrossComponentSearch<>( graph, SearchDirection.UNDIRECTED );
		final SearchListener< Spot, Link, BreadthFirstCrossComponentSearch< Spot, Link > > l =
				new SearchListener< Spot, Link, BreadthFirstCrossComponentSearch< Spot, Link > >()
		{

			private int id = 0;

			@Override
			public void processVertexLate( final Spot spot, final BreadthFirstCrossComponentSearch< Spot, Link > search )
			{}

			@Override
			public void processVertexEarly( final Spot spot, final BreadthFirstCrossComponentSearch< Spot, Link > search )
			{
				output.map.set( spot, id );
			}

			@Override
			public void processEdge( final Link link, final Spot source, final Spot target, final BreadthFirstCrossComponentSearch< Spot, Link > search )
			{}

			@Override
			public void crossComponent( final Spot endSpot, final Spot startSpot, final BreadthFirstCrossComponentSearch< Spot, Link > search )
			{
				id++;
			}
		};
		search.setTraversalListener( l );
		search.start( graph.vertices().iterator().next() );
	}
}
