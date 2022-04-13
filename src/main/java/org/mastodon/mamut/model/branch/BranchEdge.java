package org.mastodon.mamut.model.branch;

import org.mastodon.graph.ref.AbstractListenableEdge;
import org.mastodon.pool.ByteMappedElement;

public class BranchEdge extends AbstractListenableEdge< BranchEdge, BranchVertex, BranchEdgePool, ByteMappedElement >
{

	BranchEdge( final BranchEdgePool pool )
	{
		super( pool );
	}

	public BranchEdge init()
	{
		super.initDone();
		return this;
	}

	@Override
	public String toString()
	{
		return "be(" + getInternalPoolIndex() + ")";
	}
}