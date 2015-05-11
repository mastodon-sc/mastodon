package net.trackmate.trackscheme;

import static net.trackmate.graph.mempool.ByteUtils.INDEX_SIZE;
import net.trackmate.graph.AbstractEdge;
import net.trackmate.graph.AbstractEdgePool;
import net.trackmate.graph.mempool.ByteMappedElement;

public class TrackSchemeEdge extends AbstractEdge< TrackSchemeEdge, TrackSchemeVertex, ByteMappedElement >
{
	protected static final int ORIG_EDGE_INDEX_OFFSET = AbstractEdge.SIZE_IN_BYTES;

	protected static final int SIZE_IN_BYTES = ORIG_EDGE_INDEX_OFFSET + INDEX_SIZE;

	@Override
	public String toString()
	{
		return String.format( "Edge( %d -> %d )", getSource().getModelVertexId(), getTarget().getModelVertexId() );
	}

	TrackSchemeEdge( final AbstractEdgePool< TrackSchemeEdge, TrackSchemeVertex, ByteMappedElement > pool )
	{
		super( pool );
	}

	/**
	 * Get the internal pool index of the associated model vertex.
	 *
	 * @return the internal pool index of the associated
	 *         {@link TrackSchemeVertex}.
	 */
	public int getModelEdgeId()
	{
		return access.getIndex( ORIG_EDGE_INDEX_OFFSET );
	}

	protected void setModelEdgeId( final int id )
	{
		access.putIndex( id, ORIG_EDGE_INDEX_OFFSET );
	}
}
