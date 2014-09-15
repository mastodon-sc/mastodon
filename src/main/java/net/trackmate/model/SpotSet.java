package net.trackmate.model;

import net.trackmate.graph.PoolObjectSet;
import net.trackmate.graph.mempool.ByteMappedElement;

public class SpotSet extends PoolObjectSet< Spot, ByteMappedElement >
{
	public SpotSet( final SpotCollection c )
	{
		super( c.spotPool );
	}

	public SpotSet( final SpotCollection c, final int initialCapacity )
	{
		super( c.spotPool, initialCapacity );
	}
}
