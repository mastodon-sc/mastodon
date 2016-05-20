package net.trackmate.revised.model.mamut;

import net.trackmate.graph.ref.AbstractEdge;
import net.trackmate.graph.ref.AbstractEdgePool;
import net.trackmate.graph.ref.AbstractListenableEdge;
import net.trackmate.pool.ByteMappedElement;

public class Link extends AbstractListenableEdge< Link, Spot, ByteMappedElement >
{
	protected static final int SIZE_IN_BYTES = AbstractEdge.SIZE_IN_BYTES;

	@Override
	public String toString()
	{
		return String.format( "Link( %d -> %d )", getSource().getInternalPoolIndex(), getTarget().getInternalPoolIndex() );
	}

	Link( final AbstractEdgePool< Link, Spot, ByteMappedElement > pool )
	{
		super( pool );
	}

	protected void notifyEdgeAdded()
	{
		super.initDone();
	}
}
