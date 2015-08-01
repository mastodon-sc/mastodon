package net.trackmate.model;

import net.trackmate.graph.IntPoolObjectMap;

public class IntSpotMap extends IntPoolObjectMap< SpotCovariance >
{
	public IntSpotMap( final ModelGraph graph )
	{
		super( graph.getVertexPool() );
	}

	public IntSpotMap( final ModelGraph graph, final int initialCapacity )
	{
		super( graph.getVertexPool(), initialCapacity );
	}
}
