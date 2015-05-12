package net.trackmate.trackscheme;

import static net.trackmate.graph.mempool.ByteUtils.BOOLEAN_SIZE;
import static net.trackmate.graph.mempool.ByteUtils.DOUBLE_SIZE;
import static net.trackmate.graph.mempool.ByteUtils.INDEX_SIZE;
import static net.trackmate.graph.mempool.ByteUtils.INT_SIZE;
import net.trackmate.graph.AbstractVertex;
import net.trackmate.graph.AbstractVertexPool;
import net.trackmate.graph.mempool.ByteMappedElement;

public class TrackSchemeVertex extends AbstractVertex< TrackSchemeVertex, TrackSchemeEdge, ByteMappedElement >
{
	protected static final int ORIG_VERTEX_INDEX_OFFSET = AbstractVertex.SIZE_IN_BYTES;
	protected static final int X_OFFSET = ORIG_VERTEX_INDEX_OFFSET + INDEX_SIZE;
	protected static final int TIMEPOINT_OFFSET = X_OFFSET + DOUBLE_SIZE;
	protected static final int SCREENVERTEX_INDEX_OFFSET = TIMEPOINT_OFFSET + INT_SIZE;
	protected static final int SELECTED_OFFSET = SCREENVERTEX_INDEX_OFFSET + INDEX_SIZE;
	protected static final int SIZE_IN_BYTES = SELECTED_OFFSET + BOOLEAN_SIZE;

	private final Labels labels;

	@Override
	protected void setToUninitializedState()
	{
		super.setToUninitializedState();
		setScreenVertexIndex( -1 );
	}

	// TODO: remove
	public TrackSchemeVertex init( final String label, final int timepoint, final boolean isSelected )
	{
		return init( 0, label, timepoint, isSelected );
	}

	public TrackSchemeVertex init( final int modelVertexId, final String label, final int timepoint, final boolean isSelected )
	{
		setModelVertexId( modelVertexId );
		setLabel( label );
		setLayoutX( 0 );
		setTimePoint( timepoint );
		setSelected( isSelected );
		return this;
	}

	/**
	 * Get the internal pool index of the associated model vertex.
	 *
	 * @return the internal pool index of the associated
	 *         {@link TrackSchemeVertex}.
	 */
	public int getModelVertexId()
	{
		return access.getIndex( ORIG_VERTEX_INDEX_OFFSET );
	}

	protected void setModelVertexId( final int id )
	{
		access.putIndex( id, ORIG_VERTEX_INDEX_OFFSET );
	}

	@Override
	public String toString()
	{
		return String.format( "Spot( ID=%d, LABEL=%s, X=%.2f, TIMEPOINT=%d, SELECTED=%s )", getModelVertexId(), getLabel(), getLayoutX(), getTimePoint(), isSelected() ? "true" : "false" );
	}

	TrackSchemeVertex( final AbstractVertexPool< TrackSchemeVertex, ?, ByteMappedElement > pool, final Labels labels )
	{
		super( pool );
		this.labels = labels;
	}

	public String getLabel()
	{
		return labels.getLabel( getInternalPoolIndex() );
	}

	protected void setLabel( final String label )
	{
		labels.putLabel( label, getInternalPoolIndex() );
	}

	public int getTimePoint()
	{
		return access.getInt( TIMEPOINT_OFFSET );
	}

	protected void setTimePoint( final int timepoint )
	{
		access.putInt( timepoint, TIMEPOINT_OFFSET );
	}

	public int getScreenVertexIndex()
	{
		return access.getIndex( SCREENVERTEX_INDEX_OFFSET );
	}

	public void setScreenVertexIndex( final int screenVertexIndex )
	{
		access.putIndex( screenVertexIndex, SCREENVERTEX_INDEX_OFFSET );
	}

	public double getLayoutX()
	{
		return access.getDouble( X_OFFSET );
	}

	public void setLayoutX( final double x )
	{
		access.putDouble( x, X_OFFSET );
	}

	public boolean isSelected()
	{
		return access.getBoolean( SELECTED_OFFSET );
	}

	public void setSelected( final boolean selected )
	{
		access.putBoolean( selected, SELECTED_OFFSET );
	}
}
