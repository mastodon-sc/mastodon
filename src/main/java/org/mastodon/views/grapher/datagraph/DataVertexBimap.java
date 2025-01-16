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
package org.mastodon.views.grapher.datagraph;

import org.mastodon.adapter.RefBimap;
import org.mastodon.graph.Edge;
import org.mastodon.graph.GraphIdBimap;
import org.mastodon.graph.Vertex;
import org.mastodon.model.HasLabel;
import org.mastodon.spatial.HasTimepoint;

public class DataVertexBimap< V extends Vertex< E > & HasTimepoint & HasLabel, E extends Edge< V > >
		implements RefBimap< V, DataVertex >
{
	private final GraphIdBimap< V, E > idmap;

	private final DataGraph< V, E > dataGraph;

	public DataVertexBimap( final DataGraph< V, E > dataGraph )
	{
		this.idmap = dataGraph.getGraphIdBimap();
		this.dataGraph = dataGraph;
	}

	@Override
	public V getLeft( final DataVertex right )
	{
		return right == null ? null : idmap.getVertex( right.getModelVertexId(), reusableLeftRef( right ) );
	}

	@Override
	public DataVertex getRight( final V left, final DataVertex ref )
	{
		return left == null ? null : dataGraph.getDataVertexForModelId( idmap.getVertexId( left ), ref );
	}

	@SuppressWarnings( "unchecked" )
	@Override
	public V reusableLeftRef( final DataVertex ref )
	{
		return ( V ) ref.modelVertex.getReusableRef();
	}

	@Override
	public DataVertex reusableRightRef()
	{
		return dataGraph.vertexRef();
	}

	@Override
	public void releaseRef( final DataVertex ref )
	{
		dataGraph.releaseRef( ref );
	}
}
