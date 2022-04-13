package org.mastodon.mamut.model.branch;

import org.mastodon.graph.ref.AbstractListenableEdgePool;
import org.mastodon.pool.ByteMappedElement;
import org.mastodon.pool.ByteMappedElementArray;
import org.mastodon.pool.SingleArrayMemPool;

public class BranchEdgePool extends AbstractListenableEdgePool< BranchEdge, BranchVertex, ByteMappedElement >
{

	BranchEdgePool( final int initialCapacity, final BranchVertexPool vertexPool )
	{
		super( initialCapacity, AbstractListenableEdgePool.layout, BranchEdge.class, SingleArrayMemPool.factory( ByteMappedElementArray.factory ), vertexPool );
	}

	@Override
	protected BranchEdge createEmptyRef()
	{
		return new BranchEdge( this );
	}
}
