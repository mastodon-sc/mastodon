package net.trackmate.revised.trackscheme;

import static net.trackmate.graph.mempool.ByteUtils.INDEX_SIZE;
import net.trackmate.graph.AbstractEdge;
import net.trackmate.graph.AbstractEdgePool;
import net.trackmate.graph.mempool.ByteMappedElement;
import net.trackmate.revised.trackscheme.ModelGraphProperties.ModelEdgeProperties;

public class TrackSchemeEdge extends AbstractEdge< TrackSchemeEdge, TrackSchemeVertex, ByteMappedElement >
{
	protected static final int ORIG_EDGE_INDEX_OFFSET = AbstractEdge.SIZE_IN_BYTES;

	protected static final int SCREENEDGE_INDEX_OFFSET = ORIG_EDGE_INDEX_OFFSET + INDEX_SIZE;

	protected static final int SIZE_IN_BYTES = SCREENEDGE_INDEX_OFFSET + INDEX_SIZE;

	private final ModelEdgeProperties props;

	@Override
	public String toString()
	{
		return String.format( "Edge( %s -> %s )", getSource().getLabel(), getTarget().getLabel() );
	}

	TrackSchemeEdge(
			final AbstractEdgePool< TrackSchemeEdge, TrackSchemeVertex, ByteMappedElement > pool,
			final ModelEdgeProperties props )
	{
		super( pool );
		this.props = props;
	}

	@Override
	protected void setToUninitializedState()
	{
		super.setToUninitializedState();
		setScreenEdgeIndex( -1 );
	}

	/**
	 * TODO: fix javadoc, see TrackSchemeVertex.getModelVertexId()
	 *
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

	/**
	 * TODO javadoc
	 *
	 * backed by selection state of model edge
	 * @return
	 */
	public boolean isSelected()
	{
		return props.isSelected( getModelEdgeId() );
	}

	public int getScreenEdgeIndex()
	{
		return access.getIndex( SCREENEDGE_INDEX_OFFSET );
	}

	public void setScreenEdgeIndex( final int screenVertexIndex )
	{
		access.putIndex( screenVertexIndex, SCREENEDGE_INDEX_OFFSET );
	}
}
