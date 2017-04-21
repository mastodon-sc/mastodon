package org.mastodon.revised.model.mamut;

import org.mastodon.graph.GraphIdBimap;
import org.mastodon.io.properties.StringPropertyMapSerializer;
import org.mastodon.pool.ByteMappedElement;
import org.mastodon.revised.model.AbstractModelGraph;

public class ModelGraph extends AbstractModelGraph< ModelGraph, SpotPool, LinkPool, Spot, Link, ByteMappedElement >
{
	public ModelGraph()
	{
		this( 1000 );
	}

	public ModelGraph( final int initialCapacity )
	{
		super( new LinkPool( initialCapacity, new SpotPool( initialCapacity ) ) );

		vertexPropertySerializers.put( "label", new StringPropertyMapSerializer<>( vertexPool.label ) );
	}

	SpotPool getVertexPool()
	{
		return vertexPool;
	}

	LinkPool getEdgePool()
	{
		return edgePool;
	}

	GraphIdBimap< Spot, Link > idmap()
	{
		return idmap;
	}
}
