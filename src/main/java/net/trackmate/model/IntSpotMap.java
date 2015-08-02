package net.trackmate.model;

import net.trackmate.graph.IntPoolObjectMap;

public class IntSpotMap< V extends AbstractSpot< V >> extends IntPoolObjectMap< V >
{
	public IntSpotMap( final ModelGraph< V > graph )
	{
		super( graph.getVertexPool() );
	}

	public IntSpotMap( final ModelGraph< V > graph, final int initialCapacity )
	{
		super( graph.getVertexPool(), initialCapacity );
	}
}
