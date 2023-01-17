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
import org.mastodon.model.FocusListener;
import org.mastodon.model.FocusModel;
import org.scijava.listeners.Listeners;

public class BranchGraphFocusAdapter<
		V extends Vertex< E >,
		E extends Edge< V >,
		BV extends Vertex< BE >,
		BE extends Edge< BV > >
		extends AbstractBranchGraphAdapter< V, E, BV, BE >
		implements FocusModel< BV, BE >
{

	private final FocusModel< V, E > focus;

	public BranchGraphFocusAdapter(
			final BranchGraph< BV, BE, V, E > branchGraph,
			final ReadOnlyGraph< V, E > graph,
			final GraphIdBimap< V, E > idmap,
			final FocusModel< V, E > focus )
	{
		super( branchGraph, graph, idmap );
		this.focus = focus;
	}

	@Override
	public void focusVertex( final BV vertex )
	{
		if ( null == vertex )
			focus.focusVertex( null );
		else
		{
			final V vRef = graph.vertexRef();
			final V v = branchGraph.getLastLinkedVertex( vertex, vRef );
			focus.focusVertex( v );
			graph.releaseRef( vRef );
		}
	}

	@Override
	public BV getFocusedVertex( final BV ref )
	{
		final V vref = graph.vertexRef();
		final V focused = focus.getFocusedVertex( vref );
		if ( focused == null )
		{
			graph.releaseRef( vref );
			return null;
		}

		final BV bv = branchGraph.getBranchVertex( focused, ref );
		graph.releaseRef( vref );
		return bv;
	}

	@Override
	public Listeners< FocusListener > listeners()
	{
		return focus.listeners();
	}
}
