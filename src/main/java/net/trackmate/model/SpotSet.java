package net.trackmate.model;

import net.trackmate.graph.PoolObjectSet;

public class SpotSet extends PoolObjectSet< SpotCovariance >
{
	public SpotSet( final ModelGraph c )
	{
		super( c.getVertexPool() );
	}

	public SpotSet( final ModelGraph c, final int initialCapacity )
	{
		super( c.getVertexPool(), initialCapacity );
	}
}
