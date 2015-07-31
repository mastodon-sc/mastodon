package net.trackmate.model;

import net.trackmate.graph.PoolObjectSet;

public class LinkSet extends PoolObjectSet< Link >
{
	public LinkSet( final ModelGraph c )
	{
		super( c.getLinkPool() );
	}

	public LinkSet( final ModelGraph c, final int initialCapacity )
	{
		super( c.getLinkPool(), initialCapacity );
	}
}
