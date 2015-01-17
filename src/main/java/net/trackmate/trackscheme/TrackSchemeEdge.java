package net.trackmate.trackscheme;

import net.trackmate.graph.AbstractEdge;
import net.trackmate.graph.AbstractEdgePool;
import net.trackmate.graph.mempool.ByteMappedElement;

public class TrackSchemeEdge extends AbstractEdge< TrackSchemeEdge, TrackSchemeVertex, ByteMappedElement >
{
	protected static final int SIZE_IN_BYTES = AbstractEdge.SIZE_IN_BYTES;

	@Override
	public String toString()
	{
		return String.format( "Edge( %d -> %d )", getSource().getId(), getTarget().getId() );
	}

	TrackSchemeEdge( final AbstractEdgePool< TrackSchemeEdge, TrackSchemeVertex, ByteMappedElement > pool )
	{
		super( pool );
	}
}
