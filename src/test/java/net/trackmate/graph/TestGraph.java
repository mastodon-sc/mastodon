package net.trackmate.graph;

import net.trackmate.pool.ByteMappedElement;

public class TestGraph extends GraphImp< TestVertexPool, TestEdgePool, TestVertex, TestEdge, ByteMappedElement >
{
	public TestGraph( final int initialCapacity )
	{
		super( new TestEdgePool( initialCapacity, new TestVertexPool( initialCapacity ) ) );
	}

	public TestGraph()
	{
		this( 10 );
	}
}
