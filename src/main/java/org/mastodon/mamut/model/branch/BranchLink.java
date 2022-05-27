package org.mastodon.mamut.model.branch;

import org.mastodon.graph.ref.AbstractListenableEdge;
import org.mastodon.pool.ByteMappedElement;

public class BranchLink extends AbstractListenableEdge< BranchLink, BranchSpot, BranchLinkPool, ByteMappedElement >
{

	BranchLink( final BranchLinkPool pool )
	{
		super( pool );
	}

	public BranchLink init()
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