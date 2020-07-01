package org.mastodon.mamut.model;

import org.mastodon.graph.ref.AbstractListenableEdge;
import org.mastodon.pool.ByteMappedElement;

public class Link extends AbstractListenableEdge< Link, Spot, LinkPool, ByteMappedElement >
{
	/**
	 * Initialize a new {@link Link}.
	 *
	 * @return this {@link Link}.
	 */
	public Link init()
	{
		super.initDone();
		return this;
	}

	@Override
	public String toString()
	{
		return String.format( "Link( %d -> %d )", getSource().getInternalPoolIndex(), getTarget().getInternalPoolIndex() );
	}

	Link( final LinkPool pool )
	{
		super( pool );
	}

	protected void notifyEdgeAdded()
	{
		super.initDone();
	}
}
