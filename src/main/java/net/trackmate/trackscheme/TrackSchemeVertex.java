package net.trackmate.trackscheme;

import static net.trackmate.graph.mempool.ByteUtils.BOOLEAN_SIZE;
import static net.trackmate.graph.mempool.ByteUtils.DOUBLE_SIZE;
import static net.trackmate.graph.mempool.ByteUtils.INT_SIZE;
import net.trackmate.graph.AbstractIdVertex;
import net.trackmate.graph.AbstractIdVertexPool;
import net.trackmate.graph.mempool.ByteMappedElement;

public class TrackSchemeVertex extends AbstractIdVertex< TrackSchemeVertex, TrackSchemeEdge, ByteMappedElement >
	implements TrackSchemeVertexI< TrackSchemeVertex, TrackSchemeEdge >
{
	protected static final int X_OFFSET = AbstractIdVertex.SIZE_IN_BYTES;
	protected static final int TIMEPOINT_OFFSET = X_OFFSET + DOUBLE_SIZE;
	protected static final int SCREENVERTEX_INDEX_OFFSET = TIMEPOINT_OFFSET + INT_SIZE;
	protected static final int SELECTED_OFFSET = SCREENVERTEX_INDEX_OFFSET + INT_SIZE;
	protected static final int SIZE_IN_BYTES = SELECTED_OFFSET + BOOLEAN_SIZE;

	private final Labels labels;

	@Override
	protected void setToUninitializedState()
	{
		super.setToUninitializedState();
	}

	public TrackSchemeVertex init( final String label, final int timepoint, final boolean isSelected )
	{
		setLabel( label );
		setLayoutX( 0 );
		setTimePoint( timepoint );
		setSelected( isSelected );
		return this;
	}

	@Override
	public int getId()
	{
		return super.getId();
	}

	@Override
	public String toString()
	{
		return String.format( "Spot( ID=%d, LABEL=%s, X=%.2f, TIMEPOINT=%d, SELECTED=%s )", getId(), getLabel(), getLayoutX(), getTimePoint(), isSelected() ? "true" : "false" );
	}

	TrackSchemeVertex( final AbstractIdVertexPool< TrackSchemeVertex, ?, ByteMappedElement > pool, final Labels labels )
	{
		super( pool );
		this.labels = labels;
	}

	@Override
	public String getLabel()
	{
		return labels.getLabel( getInternalPoolIndex() );
	}

	protected void setLabel( final String label )
	{
		labels.putLabel( label, getInternalPoolIndex() );
	}

	@Override
	public int getTimePoint()
	{
		return access.getInt( TIMEPOINT_OFFSET );
	}

	protected void setTimePoint( final int timepoint )
	{
		access.putInt( timepoint, TIMEPOINT_OFFSET );
	}

	@Override
	public int getScreenVertexIndex()
	{
		return access.getInt( SCREENVERTEX_INDEX_OFFSET );
	}

	@Override
	public void setScreenVertexIndex( final int screenVertexIndex )
	{
		access.putInt( screenVertexIndex, SCREENVERTEX_INDEX_OFFSET );
	}

	@Override
	public double getLayoutX()
	{
		return access.getDouble( X_OFFSET );
	}

	@Override
	public void setLayoutX( final double x )
	{
		access.putDouble( x, X_OFFSET );
	}

	@Override
	public boolean isSelected()
	{
		return access.getBoolean( SELECTED_OFFSET );
	}

	@Override
	public void setSelected( final boolean selected )
	{
		access.putBoolean( selected, SELECTED_OFFSET );
	}
}
