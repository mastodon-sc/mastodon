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
package org.mastodon.model;

import org.mastodon.collection.RefCollections;
import org.mastodon.collection.RefList;
import org.mastodon.collection.ref.RefArrayList;
import org.mastodon.graph.Edge;
import org.mastodon.graph.GraphListener;
import org.mastodon.graph.ListenableReadOnlyGraph;
import org.mastodon.graph.Vertex;
import org.mastodon.views.trackscheme.TrackSchemeGraph;
import org.mastodon.views.trackscheme.TrackSchemeVertex;

import java.util.List;

public class DefaultRootsModel< Spot extends Vertex< Link >, Link extends Edge< Spot > >
		implements RootsModel< TrackSchemeVertex >, GraphListener< Spot, Link >
{
	private final TrackSchemeGraph< Spot, Link > viewGraph;

	private final RefList< Spot > modelRoots;

	private final ListenableReadOnlyGraph< Spot, Link > modelGraph;

	public DefaultRootsModel( ListenableReadOnlyGraph< Spot, Link > modelGraph, TrackSchemeGraph< Spot, Link > viewGraph )
	{
		this.viewGraph = viewGraph;
		this.modelGraph = modelGraph;
		this.modelGraph.addGraphListener( this );
		this.modelRoots = RefCollections.createRefList( modelGraph.vertices() );
	}

	/**
	 * This method should be called when the model is no longer needed.
	 * It removes listeners to allow garbage collection.
	 */
	@Override
	public void close()
	{
		modelGraph.removeGraphListener( this );
	}

	@Override
	public void setRoots( List< TrackSchemeVertex > viewRoots )
	{
		modelRoots.clear();
		TrackSchemeVertex ref = viewGraph.vertexRef();
		for ( TrackSchemeVertex viewRoot : viewRoots )
			modelRoots.add( viewGraph.getVertexMap().getLeft( viewRoot ) );
		viewGraph.releaseRef( ref );
	}

	@Override
	public RefList< TrackSchemeVertex > getRoots()
	{
		RefArrayList< TrackSchemeVertex > viewRoots = new RefArrayList<>( viewGraph.getVertexPool() );
		TrackSchemeVertex ref = viewGraph.vertexRef();
		for ( Spot modelRoot : this.modelRoots )
			viewRoots.add( viewGraph.getVertexMap().getRight( modelRoot, ref ) );
		viewGraph.releaseRef( ref );
		return viewRoots;
	}

	@Override
	public void graphRebuilt()
	{
		modelRoots.clear();
	}

	@Override
	public void vertexAdded( Spot vertex )
	{

	}

	@Override
	public void vertexRemoved( Spot vertex )
	{
		modelRoots.remove( vertex );
	}

	@Override
	public void edgeAdded( Link edge )
	{

	}

	@Override
	public void edgeRemoved( Link edge )
	{

	}
}
