package net.trackmate.model;

import net.trackmate.graph.PoolObjectSet;
import net.trackmate.graph.mempool.ByteMappedElement;

public class EdgeSet extends PoolObjectSet< Edge, ByteMappedElement >
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
