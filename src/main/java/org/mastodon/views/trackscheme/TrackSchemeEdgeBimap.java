/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2022 Tobias Pietzsch, Jean-Yves Tinevez
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
package org.mastodon.views.trackscheme;

import org.mastodon.adapter.RefBimap;
import org.mastodon.graph.Edge;
import org.mastodon.graph.GraphIdBimap;
import org.mastodon.graph.Vertex;

public class TrackSchemeEdgeBimap< V extends Vertex< E >, E extends Edge< V > >
		implements RefBimap< E, TrackSchemeEdge >
{
	private final GraphIdBimap< V, E > idmap;

	private final TrackSchemeGraph< V, E > tsgraph;

	public TrackSchemeEdgeBimap(
			final TrackSchemeGraph< V, E > tsgraph )
	{
		this.idmap = tsgraph.getGraphIdBimap();
		this.tsgraph = tsgraph;
	}

	@Override
	public E getLeft( final TrackSchemeEdge right )
	{
		return right == null ? null : idmap.getEdge( right.getModelEdgeId(), reusableLeftRef( right ) );
	}

	@Override
	public TrackSchemeEdge getRight( final E left, final TrackSchemeEdge ref )
	{
		return left == null ? null : tsgraph.getTrackSchemeEdgeForModelId( idmap.getEdgeId( left ), ref );
	}

	@SuppressWarnings( "unchecked" )
	@Override
	public E reusableLeftRef( final TrackSchemeEdge ref )
	{
		return ( E ) ref.modelEdge.getReusableRef();
	}

	@Override
	public TrackSchemeEdge reusableRightRef()
	{
		return tsgraph.edgeRef();
	}

	@Override
	public void releaseRef( final TrackSchemeEdge ref )
	{
		tsgraph.releaseRef( ref );
	}
}
