package net.trackmate.trackscheme;

import static net.trackmate.graph.mempool.ByteUtils.BOOLEAN_SIZE;
import static net.trackmate.graph.mempool.ByteUtils.INDEX_SIZE;
import net.trackmate.graph.AbstractEdge;
import net.trackmate.graph.AbstractEdgePool;
import net.trackmate.graph.mempool.ByteMappedElement;

public class TrackSchemeEdge extends AbstractEdge< TrackSchemeEdge, TrackSchemeVertex, ByteMappedElement >
{
	protected static final int ORIG_EDGE_INDEX_OFFSET = AbstractEdge.SIZE_IN_BYTES;

	protected static final int SELECTED_OFFSET = ORIG_EDGE_INDEX_OFFSET + INDEX_SIZE;

	protected static final int SCREENEDGE_INDEX_OFFSET = SELECTED_OFFSET + BOOLEAN_SIZE;

	protected static final int SIZE_IN_BYTES = SCREENEDGE_INDEX_OFFSET + INDEX_SIZE;

	@Override
	public String toString()
	{
		return String.format( "Edge( %s -> %s )", getSource().getLabel(), getTarget().getLabel() );
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

	public boolean isSelected()
	{
		return access.getBoolean( SELECTED_OFFSET );
	}

	public void setSelected( final boolean selected )
	{
		access.putBoolean( selected, SELECTED_OFFSET );
	}

	public int getScreenVertexIndex()
	{
		return access.getIndex( SCREENEDGE_INDEX_OFFSET );
	}

	public void setScreenEdgeIndex( final int screenVertexIndex )
	{
		access.putIndex( screenVertexIndex, SCREENEDGE_INDEX_OFFSET );
	}
}
