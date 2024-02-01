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
package org.mastodon.views.trackscheme;

import org.mastodon.adapter.RefBimap;
import org.mastodon.graph.Edge;
import org.mastodon.graph.GraphIdBimap;
import org.mastodon.graph.Vertex;

public class TrackSchemeVertexBimap< V extends Vertex< E >, E extends Edge< V > >
		implements RefBimap< V, TrackSchemeVertex >
{
	private final GraphIdBimap< V, E > idmap;

	private final TrackSchemeGraph< V, E > tsgraph;

	public TrackSchemeVertexBimap( final TrackSchemeGraph< V, E > tsgraph )
	{
		this.idmap = tsgraph.getGraphIdBimap();
		this.tsgraph = tsgraph;
	}

	@Override
	public V getLeft( final TrackSchemeVertex right )
	{
		return right == null ? null : idmap.getVertex( right.getModelVertexId(), reusableLeftRef( right ) );
	}

	@Override
	public TrackSchemeVertex getRight( final V left, final TrackSchemeVertex ref )
	{
		return left == null ? null : tsgraph.getTrackSchemeVertexForModelId( idmap.getVertexId( left ), ref );
	}

	@SuppressWarnings( "unchecked" )
	@Override
	public V reusableLeftRef( final TrackSchemeVertex ref )
	{
		return ( V ) ref.modelVertex.getReusableRef();
	}

	@Override
	public TrackSchemeVertex reusableRightRef()
	{
		return tsgraph.vertexRef();
	}

	@Override
	public void releaseRef( final TrackSchemeVertex ref )
	{
		tsgraph.releaseRef( ref );
	}
}
