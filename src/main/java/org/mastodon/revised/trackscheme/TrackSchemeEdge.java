package org.mastodon.revised.trackscheme;

import static org.mastodon.pool.ByteUtils.INDEX_SIZE;

import org.mastodon.graph.GraphIdBimap;
import org.mastodon.graph.ref.AbstractEdge;
import org.mastodon.pool.ByteMappedElement;
import org.mastodon.pool.PoolObject;
import org.mastodon.revised.trackscheme.TrackSchemeGraph.TrackSchemeEdgePool;

public class TrackSchemeEdge extends AbstractEdge< TrackSchemeEdge, TrackSchemeVertex, TrackSchemeEdgePool, ByteMappedElement >
{
	protected static final int ORIG_EDGE_INDEX_OFFSET = AbstractEdge.SIZE_IN_BYTES;
	protected static final int SCREENEDGE_INDEX_OFFSET = ORIG_EDGE_INDEX_OFFSET + INDEX_SIZE;
	protected static final int SIZE_IN_BYTES = SCREENEDGE_INDEX_OFFSET + INDEX_SIZE;

	ModelGraphWrapper< ?, ? >.ModelEdgeWrapper modelEdge;

	@Override
	public String toString()
	{
		return String.format( "Edge( %s -> %s )", getSource().getLabel(), getTarget().getLabel() );
	}

	TrackSchemeEdge( final TrackSchemeEdgePool pool )
	{
		super( pool );
	}

	@Override
	protected void setToUninitializedState()
	{
		super.setToUninitializedState();
		setScreenEdgeIndex( -1 );
	}

	public TrackSchemeEdge init( final int modelEdgeId )
	{
		setModelEdgeId( modelEdgeId );
		return this;
	}

	/**
	 * Gets the ID of the associated model edge, as defined by a
	 * {@link GraphIdBimap}. For {@link PoolObject} model edges, the ID will be
	 * the internal pool index of the model edge.
	 *
	 * @return the ID of the associated model edge.
	 */
	public int getModelEdgeId()
	{
		return access.getIndex( ORIG_EDGE_INDEX_OFFSET );
	}

	protected void setModelEdgeId( final int id )
	{
		access.putIndex( id, ORIG_EDGE_INDEX_OFFSET );
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
