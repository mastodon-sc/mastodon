package net.trackmate.model;

import net.trackmate.graph.PoolObjectSet;

public class SpotSet< V extends AbstractSpot< V >> extends PoolObjectSet< V >
{
	public SpotSet( final ModelGraph< V > c )
	{
		super( c.getVertexPool() );
	}

	public SpotSet( final ModelGraph< V > c, final int initialCapacity )
	{
		super( c.getVertexPool(), initialCapacity );
	}
}
