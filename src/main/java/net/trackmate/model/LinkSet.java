package net.trackmate.model;

import net.trackmate.graph.PoolObjectSet;

public class LinkSet< V extends Spot< V >> extends PoolObjectSet< Link< V > >
{
	public LinkSet( final ModelGraph< V > c )
	{
		super( c.getLinkPool() );
	}

	public LinkSet( final ModelGraph< V > c, final int initialCapacity )
	{
		super( c.getLinkPool(), initialCapacity );
	}
}
