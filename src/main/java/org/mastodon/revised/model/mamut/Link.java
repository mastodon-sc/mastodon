package org.mastodon.revised.model.mamut;

import org.mastodon.graph.ref.AbstractEdge;
import org.mastodon.graph.ref.AbstractEdgePool;
import org.mastodon.graph.ref.AbstractListenableEdge;
import org.mastodon.pool.ByteMappedElement;

public class Link extends AbstractListenableEdge< Link, Spot, ByteMappedElement >
{
	protected static final int SIZE_IN_BYTES = AbstractEdge.SIZE_IN_BYTES;

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

	Link( final AbstractEdgePool< Link, Spot, ByteMappedElement > pool )
	{
		super( pool );
	}

	protected void notifyEdgeAdded()
	{
		super.initDone();
	}
}
