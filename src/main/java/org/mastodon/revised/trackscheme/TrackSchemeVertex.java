package org.mastodon.revised.trackscheme;

import static org.mastodon.pool.ByteUtils.BOOLEAN_SIZE;
import static org.mastodon.pool.ByteUtils.DOUBLE_SIZE;
import static org.mastodon.pool.ByteUtils.INDEX_SIZE;
import static org.mastodon.pool.ByteUtils.INT_SIZE;

import org.mastodon.graph.GraphIdBimap;
import org.mastodon.graph.ref.AbstractVertex;
import org.mastodon.graph.ref.AbstractVertexPool;
import org.mastodon.pool.ByteMappedElement;
import org.mastodon.pool.PoolObject;
import org.mastodon.revised.trackscheme.ModelGraphProperties.ModelVertexProperties;

/**
 * TODO javadoc
 *
 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
 */
public class TrackSchemeVertex extends AbstractVertex< TrackSchemeVertex, TrackSchemeEdge, ByteMappedElement >
{
	protected static final int ORIG_VERTEX_INDEX_OFFSET = AbstractVertex.SIZE_IN_BYTES;
	protected static final int LAYOUT_TIMESTAMP_OFFSET = ORIG_VERTEX_INDEX_OFFSET + INDEX_SIZE;
	protected static final int LAYOUT_IN_EDGE_INDEX_OFFSET = LAYOUT_TIMESTAMP_OFFSET + INT_SIZE;
	protected static final int X_OFFSET = LAYOUT_IN_EDGE_INDEX_OFFSET + INDEX_SIZE;
	protected static final int TIMEPOINT_OFFSET = X_OFFSET + DOUBLE_SIZE;
	protected static final int SCREENVERTEX_INDEX_OFFSET = TIMEPOINT_OFFSET + INT_SIZE;
	protected static final int GHOST_OFFSET = SCREENVERTEX_INDEX_OFFSET + INDEX_SIZE;
	protected static final int SIZE_IN_BYTES = GHOST_OFFSET + BOOLEAN_SIZE;

	private final ModelVertexProperties props;

	@Override
	protected void setToUninitializedState()
	{
		super.setToUninitializedState();
		setScreenVertexIndex( -1 );
	}

	public TrackSchemeVertex init( final int modelVertexId, final int timepoint )
	{
		setModelVertexId( modelVertexId );
		setLayoutX( 0 );
		setTimepoint( timepoint );
		setLayoutTimestamp( -1 );
		setLayoutInEdgeIndex( 0 );
		setGhost( false );
		return this;
	}

	/**
	 * Get the ID of the associated model vertex, as defined by a
	 * {@link GraphIdBimap}. For {@link PoolObject} model vertices, the ID will
	 * be the internal pool index of the model vertex.
	 *
	 * @return the ID of the associated model vertex.
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
		return String.format( "TrackSchemeVertex( ID=%d, LABEL=%s, X=%.2f, TIMEPOINT=%d )",
				getModelVertexId(),
				getLabel(),
				getLayoutX(),
				getTimepoint() );
	}

	TrackSchemeVertex(
			final AbstractVertexPool< TrackSchemeVertex, ?, ByteMappedElement > pool,
			final ModelVertexProperties props )
	{
		super( pool );
		this.props = props;
	}

	/**
	 * Get label of associated model vertex
	 */
	public String getLabel()
	{
		return props.getLabel( getModelVertexId() );
	}

	/**
	 * Set label of associated model vertex
	 */
	public void setLabel( final String label )
	{
		props.setLabel( getModelVertexId(), label );
	}

	/**
	 * TODO javadoc
	 *
	 * backed by the state of the SelectionModel for the associated ModelGraph vertex.
	 * @return
	 */
	public boolean isSelected()
	{
		return props.isSelected( getModelVertexId() );
	}

	public int getTimepoint()
	{
		return access.getInt( TIMEPOINT_OFFSET );
	}

	protected void setTimepoint( final int timepoint )
	{
		access.putInt( timepoint, TIMEPOINT_OFFSET );
	}

	/**
	 * Internal pool index of last {@link ScreenVertex} that was created for
	 * this vertex. Used for lookup when creating {@link ScreenEdge}s.
	 *
	 * @return internal pool index of associated {@link ScreenVertex}.
	 */
	public int getScreenVertexIndex()
	{
		return access.getIndex( SCREENVERTEX_INDEX_OFFSET );
	}

	protected void setScreenVertexIndex( final int screenVertexIndex )
	{
		access.putIndex( screenVertexIndex, SCREENVERTEX_INDEX_OFFSET );
	}

	public double getLayoutX()
	{
		return access.getDouble( X_OFFSET );
	}

	protected void setLayoutX( final double x )
	{
		access.putDouble( x, X_OFFSET );
	}

	/**
	 * Layout timestamp is set when this vertex is layouted (assigned a
	 * {@link #getLayoutX() coordinate}). It is also used to mark active
	 * vertices before a partial layout.
	 *
	 * @return layout timestamp.
	 */
	public int getLayoutTimestamp()
	{
		return access.getInt( LAYOUT_TIMESTAMP_OFFSET );
	}

	public void setLayoutTimestamp( final int timestamp )
	{
		access.putInt( timestamp, LAYOUT_TIMESTAMP_OFFSET );
	}

	/**
	 * internal pool index of first (and only) edge through which this vertex
	 * was touched in last layout.
	 * <p>
	 * TODO: REMOVE? This is not be needed because VertexOrder is built directly
	 * during layout now.
	 *
	 * @return internal pool index of first (and only) edge through which this
	 *         vertex was touched in last layout.
	 */
	protected int getLayoutInEdgeIndex()
	{
		return access.getIndex( LAYOUT_IN_EDGE_INDEX_OFFSET );
	}

	protected void setLayoutInEdgeIndex( final int index )
	{
		access.putIndex( index, LAYOUT_IN_EDGE_INDEX_OFFSET );
	}

	/**
	 * A vertex is set to <em>ghost</em> if it is hit during a partial layout
	 * and is marked with a timestamp &lt; the current mark.
	 *
	 * @return whether this vertex is a ghost
	 */
	protected boolean isGhost()
	{
		return access.getBoolean( GHOST_OFFSET );
	}

	protected void setGhost( final boolean ghost )
	{
		access.putBoolean( ghost, GHOST_OFFSET );
	}
}
