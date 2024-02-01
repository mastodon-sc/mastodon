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
package org.mastodon.model.branch;

import org.mastodon.adapter.RefBimap;
import org.mastodon.graph.Edge;
import org.mastodon.graph.ReadOnlyGraph;
import org.mastodon.graph.branch.BranchGraph;

/**
 * Maps a graph vertices to the edges in the branch graph they are linked to.
 * 
 * @author Jean-Yves Tinevez
 *
 * @param <E>
 *            the type of edges in the core graph.
 * @param <BE>
 *            the type of edges in the branch graph.
 */
public class BranchGraphEdgeBimap< E extends Edge< ? >, BE extends Edge< ? > > implements RefBimap< E, BE >
{

	private final BranchGraph< ?, BE, ?, E > branchGraph;

	private final ReadOnlyGraph< ?, E > graph;

	public BranchGraphEdgeBimap( final BranchGraph< ?, BE, ?, E > branchGraph, final ReadOnlyGraph< ?, E > graph )
	{
		this.branchGraph = branchGraph;
		this.graph = graph;
	}

	@Override
	public E getLeft( final BE right )
	{
		return right == null ? null : branchGraph.getLinkedEdge( right, reusableLeftRef( right ) );
	}

	@Override
	public BE getRight( final E left, final BE ref )
	{
		return left == null ? null : branchGraph.getBranchEdge( left, ref );
	}

	@Override
	public E reusableLeftRef( final BE ref )
	{
		return graph.edgeRef();
	}

	@Override
	public BE reusableRightRef()
	{
		return branchGraph.edgeRef();
	}

	@Override
	public void releaseRef( final BE ref )
	{
		branchGraph.releaseRef( ref );
	}
}
