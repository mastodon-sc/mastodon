package org.mastodon.mamut.model.branch;

import org.mastodon.graph.ref.AbstractListenableEdgePool;
import org.mastodon.pool.ByteMappedElement;
import org.mastodon.pool.ByteMappedElementArray;
import org.mastodon.pool.SingleArrayMemPool;

public class BranchLinkPool extends AbstractListenableEdgePool< BranchLink, BranchSpot, ByteMappedElement >
{

	BranchLinkPool( final int initialCapacity, final BranchSpotPool vertexPool )
	{
		super( initialCapacity, AbstractListenableEdgePool.layout, BranchLink.class, SingleArrayMemPool.factory( ByteMappedElementArray.factory ), vertexPool );
	}

	@Override
	protected BranchLink createEmptyRef()
	{
		return new BranchLink( this );
	}
}
