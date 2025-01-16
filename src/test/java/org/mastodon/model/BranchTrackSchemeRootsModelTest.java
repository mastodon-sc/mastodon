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
package org.mastodon.model;

import static org.junit.Assert.assertEquals;

import java.util.Collections;

import org.junit.Test;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.model.branch.BranchLink;
import org.mastodon.mamut.model.branch.BranchSpot;
import org.mastodon.mamut.model.branch.ModelBranchGraph;
import org.mastodon.views.trackscheme.TrackSchemeGraph;
import org.mastodon.views.trackscheme.TrackSchemeVertex;
import org.mastodon.views.trackscheme.wrap.DefaultModelGraphProperties;
import org.mastodon.views.trackscheme.wrap.ModelGraphProperties;

/**
 * Tests for {@link BranchTrackSchemeRootsModel}.
 */
public class BranchTrackSchemeRootsModelTest
{
	@Test
	public void testUpdateWhenRootIsRemoved()
	{
		// setup
		final Model model = new Model();
		final ModelGraph modelGraph = model.getGraph();
		final Spot spotA = modelGraph.addVertex().init( 0, new double[ 3 ], 1 );
		final ModelBranchGraph branchGraph = model.getBranchGraph();
		branchGraph.graphRebuilt();
		final TrackSchemeGraph< BranchSpot, BranchLink > viewGraph = createTrackSchemeGraph( branchGraph );
		final TrackSchemeVertex vertexA = viewGraph.getRoots().iterator().next();

		// run
		final RootsModel< TrackSchemeVertex > rootsModel = new BranchTrackSchemeRootsModel( modelGraph, branchGraph, viewGraph );
		rootsModel.setRoots( Collections.singletonList( vertexA ) );
		modelGraph.remove( spotA );

		// test
		assertEquals( 0, rootsModel.getRoots().size() );
	}

	private static TrackSchemeGraph< BranchSpot, BranchLink > createTrackSchemeGraph( ModelBranchGraph branchGraph )
	{
		final ModelGraphProperties< BranchSpot, BranchLink > properties =
				new DefaultModelGraphProperties< BranchSpot, BranchLink >()
				{

					@Override
					public String getFirstLabel( final BranchSpot branchSpot )
					{
						return branchSpot.getFirstLabel();
					}

					@Override
					public int getFirstTimePoint( final BranchSpot branchSpot )
					{
						return branchSpot.getFirstTimePoint();
					}
				};
		return new TrackSchemeGraph<>( branchGraph, branchGraph.getGraphIdBimap(), properties );
	}
}
