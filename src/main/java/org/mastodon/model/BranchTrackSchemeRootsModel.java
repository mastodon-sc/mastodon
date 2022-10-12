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

import org.mastodon.adapter.RefBimap;
import org.mastodon.collection.RefList;
import org.mastodon.collection.ref.RefArrayList;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.model.branch.BranchLink;
import org.mastodon.mamut.model.branch.BranchSpot;
import org.mastodon.mamut.model.branch.ModelBranchGraph;
import org.mastodon.views.trackscheme.TrackSchemeGraph;
import org.mastodon.views.trackscheme.TrackSchemeVertex;

import java.util.List;

public class BranchTrackSchemeRootsModel
		implements RootsModel<TrackSchemeVertex>
{
	private final ModelGraph modelGraph;

	private final ModelBranchGraph branchGraph;

	private final TrackSchemeGraph<BranchSpot, BranchLink> viewGraph;

	private final RefArrayList<Spot> list;

	public BranchTrackSchemeRootsModel( ModelGraph graph, ModelBranchGraph branchGraph, TrackSchemeGraph<BranchSpot, BranchLink> viewGraph )
	{
		this.modelGraph = graph;
		this.branchGraph = branchGraph;
		this.viewGraph = viewGraph;
		this.list = new RefArrayList<>( graph.vertices().getRefPool() );
	}

	@Override
	public void setRoots( List<TrackSchemeVertex> roots )
	{
		list.clear();
		Spot spotRef = modelGraph.vertexRef();
		RefBimap<BranchSpot, TrackSchemeVertex> vertexMap = viewGraph.getVertexMap();
		for ( TrackSchemeVertex root : roots )
		{
			BranchSpot branchSpot = vertexMap.getLeft( root );
			Spot spot = branchGraph.getFirstLinkedVertex( branchSpot, spotRef );
			list.add( spot );
		}
		modelGraph.releaseRef( spotRef );
	}

	@Override
	public RefList<TrackSchemeVertex> getRoots()
	{
		RefArrayList<TrackSchemeVertex> roots = new RefArrayList<>( viewGraph.getVertexPool() );
		RefBimap<BranchSpot, TrackSchemeVertex> vertexMap = viewGraph.getVertexMap();
		BranchSpot branchSpotRef = branchGraph.vertexRef();
		TrackSchemeVertex vertexRef = viewGraph.vertexRef();
		for ( Spot root : list )
		{
			BranchSpot branchSpot = branchGraph.getBranchVertex( root, branchSpotRef );
			TrackSchemeVertex vertex = vertexMap.getRight( branchSpot, vertexRef );
			roots.add( vertex );
		}
		viewGraph.releaseRef( vertexRef );
		branchGraph.releaseRef( branchSpotRef );
		return roots;
	}
}
