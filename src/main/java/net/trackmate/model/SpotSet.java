package net.trackmate.model;

import net.trackmate.graph.PoolObjectSet;

public class SpotSet extends PoolObjectSet< Spot >
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
