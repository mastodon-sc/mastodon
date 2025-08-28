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
package org.mastodon.model.branch;

import org.mastodon.graph.Edge;
import org.mastodon.graph.GraphIdBimap;
import org.mastodon.graph.ReadOnlyGraph;
import org.mastodon.graph.Vertex;
import org.mastodon.graph.branch.BranchGraph;
import org.mastodon.model.HighlightListener;
import org.mastodon.model.HighlightModel;
import org.mastodon.spatial.HasTimepoint;
import org.scijava.listeners.Listeners;

public class BranchGraphHighlightAdapter< V extends Vertex< E > & HasTimepoint, E extends Edge< V >, BV extends Vertex< BE >, BE extends Edge< BV > >
		extends AbstractBranchGraphAdapter< V, E, BV, BE >
		implements HighlightModel< BV, BE >
{

	private final HighlightModel< V, E > highlight;

	public BranchGraphHighlightAdapter(
			final BranchGraph< BV, BE, V, E > branchGraph,
			final ReadOnlyGraph< V, E > graph,
			final GraphIdBimap< V, E > idmap,
			final HighlightModel< V, E > highlight )
	{
		super( branchGraph, graph, idmap );
		this.highlight = highlight;
	}

	@Override
	public void highlightVertex( final BV branchVertex )
	{
		if ( null == branchVertex )
		{
			highlight.highlightVertex( null );
			return;
		}

		// Highlight the last spot in the branch
		final V spotRef = graph.vertexRef();
		highlight.highlightVertex( branchGraph.getLastLinkedVertex( branchVertex, spotRef ) );
		graph.releaseRef( spotRef );
	}

	@Override
	public void highlightEdge( final BE edge )
	{
		if ( null == edge )
		{
			highlight.highlightVertex( null );
			return;
		}

		final E eRef = graph.edgeRef();
		highlight.highlightEdge( branchGraph.getLinkedEdge( edge, eRef ) );
		graph.releaseRef( eRef );
	}

	@Override
	public BV getHighlightedVertex( final BV ref )
	{
		final V vRef = graph.vertexRef();
		final E eRef = graph.edgeRef();
		try
		{
			final V highlighted = highlight.getHighlightedVertex( vRef );
			if ( null != highlighted )
				return branchGraph.getBranchVertex( highlighted, ref );

			final E highlightedEdge = highlight.getHighlightedEdge( eRef );
			if ( null != highlightedEdge )
				return branchGraph.getBranchVertex( highlightedEdge, ref );

			return null;

		}
		finally
		{
			graph.releaseRef( vRef );
			graph.releaseRef( eRef );
		}
	}

	@Override
	public BE getHighlightedEdge( final BE ref )
	{
		final E eRef = graph.edgeRef();
		try
		{
			final E highlightedEdge = highlight.getHighlightedEdge( eRef );
			if ( highlightedEdge == null )
				return null;

			return branchGraph.getBranchEdge( highlightedEdge, ref );
		}
		finally
		{
			graph.releaseRef( eRef );
		}
	}

	@Override
	public void clearHighlight()
	{
		highlight.clearHighlight();
	}

	@Override
	public Listeners< HighlightListener > listeners()
	{
		return highlight.listeners();
	}
}
