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
package org.mastodon.model.branch;

import org.mastodon.graph.Edge;
import org.mastodon.graph.GraphIdBimap;
import org.mastodon.graph.ReadOnlyGraph;
import org.mastodon.graph.Vertex;
import org.mastodon.graph.branch.BranchGraph;

/**
 * Base class for classes that adapt a model component of a core graph to a
 * branch graph.
 * 
 * @author Jean-Yves Tinevez
 */
public abstract class AbstractBranchGraphAdapter< 
	V extends Vertex< E >, 
	E extends Edge< V >, 
	BV extends Vertex< BE >, 
	BE extends Edge< BV > >
{

	protected final BranchGraph< BV, BE, V, E > branchGraph;

	protected final ReadOnlyGraph< V, E > graph;

	protected final GraphIdBimap< V, E > idmap;

	private final E eref;

	private final V vref;

	protected AbstractBranchGraphAdapter( 
			final BranchGraph< BV, BE, V, E > branchGraph,
			final ReadOnlyGraph< V, E > graph,
			final GraphIdBimap< V, E > idmap )
	{
		this.branchGraph = branchGraph;
		this.graph = graph;
		this.idmap = idmap;
		this.eref = graph.edgeRef();
		this.vref = graph.vertexRef();
	}
	

	protected final boolean isValid( final E e )
	{
		if ( e == null )
			return false;
		final int id = idmap.getEdgeId( e );
		return idmap.getEdgeIfExists( id, eref ) != null;
	}

	protected final boolean isValid( final V v )
	{
		if ( v == null )
			return false;
		final int id = idmap.getVertexId( v );
		return idmap.getVertexIfExists( id, vref ) != null;
	}
}
