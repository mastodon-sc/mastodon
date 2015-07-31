package net.trackmate.model;

import net.trackmate.graph.AbstractEdge;
import net.trackmate.graph.AbstractEdgePool;
import net.trackmate.graph.mempool.ByteMappedElement;

public class Link extends AbstractEdge< Link, Spot, ByteMappedElement >
{
	protected static final int SIZE_IN_BYTES = AbstractEdge.SIZE_IN_BYTES;

	@Override
	public String toString()
	{
		return String.format( "Edge( %d -> %d )", getSource().getInternalPoolIndex(), getTarget().getInternalPoolIndex() );
	}

	Link( final AbstractEdgePool< Link, Spot, ByteMappedElement > pool )
	{
		super( pool );
	}
}
