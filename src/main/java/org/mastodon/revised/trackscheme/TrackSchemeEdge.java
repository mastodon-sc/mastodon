package org.mastodon.revised.trackscheme;

import org.mastodon.graph.GraphIdBimap;
import org.mastodon.graph.ref.AbstractEdge;
import org.mastodon.pool.ByteMappedElement;
import org.mastodon.pool.PoolObject;
import org.mastodon.revised.trackscheme.TrackSchemeGraph.TrackSchemeEdgePool;

public class TrackSchemeEdge extends AbstractEdge< TrackSchemeEdge, TrackSchemeVertex, TrackSchemeEdgePool, ByteMappedElement >
{
	final ModelGraphWrapper< ?, ? >.ModelEdgeWrapper modelEdge;

	@Override
	public String toString()
	{
		return String.format( "Edge( %s -> %s )", getSource().getLabel(), getTarget().getLabel() );
	}

	TrackSchemeEdge( final TrackSchemeEdgePool pool )
	{
		super( pool );
		modelEdge = pool.modelGraphWrapper.createEdgeWrapper( this );
	}

	/**
	 * Initialize a new {@link TrackSchemeEdge}
	 * <p>
	 * Fake {@code init()} constructor. Although it does nothing, it is good
	 * practice to call it when adding a new {@code TrackSchemeEdge}, because
	 * eventually it might do something...
	 * </p>
	 *
	 * @return this {@link TrackSchemeEdge}
	 */
	public TrackSchemeEdge init()
	{
		return this;
	}

	@Override
	protected void setToUninitializedState()
	{
		super.setToUninitializedState();
		setScreenEdgeIndex( -1 );
	}

	TrackSchemeEdge initModelId( final int modelEdgeId )
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
		return pool.origEdgeIndex.get( this );
	}

	protected void setModelEdgeId( final int id )
	{
		pool.origEdgeIndex.setQuiet( this, id );
	}

	public int getScreenEdgeIndex()
	{
		return pool.screenEdgeIndex.get( this );
	}

	public void setScreenEdgeIndex( final int screenEdgeIndex )
	{
		pool.screenEdgeIndex.setQuiet( this, screenEdgeIndex );
	}
}
