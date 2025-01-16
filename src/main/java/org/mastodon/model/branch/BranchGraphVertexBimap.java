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

import org.mastodon.adapter.RefBimap;
import org.mastodon.graph.ReadOnlyGraph;
import org.mastodon.graph.Vertex;
import org.mastodon.graph.branch.BranchGraph;

/**
 * Maps a graph vertices to the vertices in the branch graph they are linked to.
 * 
 * @author Jean-Yves Tinevez
 *
 * @param <V>
 *            the type of vertices in the core graph.
 * @param <BV>
 *            the type of vertices in the branch graph.
 */
public class BranchGraphVertexBimap< V extends Vertex< ? >, BV extends Vertex< ? > > implements RefBimap< V, BV >
{

	private final BranchGraph< BV, ?, V, ? > branchGraph;

	private final ReadOnlyGraph< V, ? > graph;

	public BranchGraphVertexBimap( final BranchGraph< BV, ?, V, ? > branchGraph, final ReadOnlyGraph< V, ? > graph )
	{
		this.branchGraph = branchGraph;
		this.graph = graph;
	}

	@Override
	public V getLeft( final BV right )
	{
		return right == null ? null : branchGraph.getFirstLinkedVertex( right, reusableLeftRef( right ) );
	}

	@Override
	public BV getRight( final V left, final BV ref )
	{
		return left == null ? null : branchGraph.getBranchVertex( left, ref );
	}

	@Override
	public V reusableLeftRef( final BV ref )
	{
		return graph.vertexRef();
	}

	@Override
	public BV reusableRightRef()
	{
		return branchGraph.vertexRef();
	}

	@Override
	public void releaseRef( final BV ref )
	{
		branchGraph.releaseRef( ref );
	}
}
