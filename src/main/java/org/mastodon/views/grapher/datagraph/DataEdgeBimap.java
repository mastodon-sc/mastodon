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
package org.mastodon.views.grapher.datagraph;

import org.mastodon.adapter.RefBimap;
import org.mastodon.graph.Edge;
import org.mastodon.graph.GraphIdBimap;
import org.mastodon.graph.Vertex;
import org.mastodon.spatial.HasTimepoint;

public class DataEdgeBimap< V extends Vertex< E > & HasTimepoint, E extends Edge< V > >
		implements RefBimap< E, DataEdge >
{
	private final GraphIdBimap< V, E > idmap;

	private final DataGraph< V, E > dataGraph;

	public DataEdgeBimap(
			final DataGraph< V, E > dataGraph )
	{
		this.idmap = dataGraph.getGraphIdBimap();
		this.dataGraph = dataGraph;
	}

	@Override
	public E getLeft( final DataEdge right )
	{
		return right == null ? null : idmap.getEdge( right.getModelEdgeId(), reusableLeftRef( right ) );
	}

	@Override
	public DataEdge getRight( final E left, final DataEdge ref )
	{
		return left == null ? null : dataGraph.getDataEdgeForModelId( idmap.getEdgeId( left ), ref );
	}

	@SuppressWarnings( "unchecked" )
	@Override
	public E reusableLeftRef( final DataEdge ref )
	{
		return ( E ) ref.modelEdge.getReusableRef();
	}

	@Override
	public DataEdge reusableRightRef()
	{
		return dataGraph.edgeRef();
	}

	@Override
	public void releaseRef( final DataEdge ref )
	{
		dataGraph.releaseRef( ref );
	}
}
