package net.trackmate.model;

import net.trackmate.graph.PoolObjectSet;

public class LinkSet extends PoolObjectSet< Link >
{
	public LinkSet( final SpotCollection c )
	{
		super( c.linkPool );
	}

	public LinkSet( final SpotCollection c, final int initialCapacity )
	{
		super( c.linkPool, initialCapacity );
	}
}
