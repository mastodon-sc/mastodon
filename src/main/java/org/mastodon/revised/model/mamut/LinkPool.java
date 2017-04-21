package org.mastodon.revised.model.mamut;

import org.mastodon.graph.ref.AbstractEdgePool;
import org.mastodon.graph.ref.AbstractListenableEdgePool;
import org.mastodon.pool.ByteMappedElement;
import org.mastodon.pool.ByteMappedElementArray;
import org.mastodon.pool.SingleArrayMemPool;

public class LinkPool extends AbstractListenableEdgePool< Link, Spot, ByteMappedElement >
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