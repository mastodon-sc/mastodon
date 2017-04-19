package org.mastodon.revised.model.mamut;

import org.mastodon.graph.ref.AbstractEdgePool;
import org.mastodon.graph.ref.AbstractListenableEdge;
import org.mastodon.graph.ref.AbstractListenableEdgePool;
import org.mastodon.pool.ByteMappedElement;
import org.mastodon.pool.ByteMappedElementArray;
import org.mastodon.pool.SingleArrayMemPool;
import org.mastodon.revised.model.mamut.Link.LinkPool;
import org.mastodon.revised.model.mamut.Spot.SpotPool;

public class Link extends AbstractListenableEdge< Link, Spot, LinkPool, ByteMappedElement >
{
	static class LinkPool extends AbstractListenableEdgePool< Link, Spot, ByteMappedElement >
	{
		LinkPool( final int initialCapacity, final SpotPool vertexPool )
		{
			super( initialCapacity, AbstractEdgePool.layout, Link.class, SingleArrayMemPool.factory( ByteMappedElementArray.factory ), vertexPool );
		}

		@Override
		protected Link createEmptyRef()
		{
			return new Link( this );
		}
	}

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
