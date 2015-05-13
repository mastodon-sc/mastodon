package net.trackmate.model;

import net.trackmate.graph.PoolObjectSet;

public class EdgeSet extends PoolObjectSet< Edge >
{
	public EdgeSet( final SpotCollection c )
	{
		super( c.edgePool );
	}

	public EdgeSet( final SpotCollection c, final int initialCapacity )
	{
		super( c.edgePool, initialCapacity );
	}
}
