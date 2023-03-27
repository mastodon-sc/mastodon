/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2022 Tobias Pietzsch, Jean-Yves Tinevez
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package org.mastodon.views.trackscheme;

import org.mastodon.graph.GraphIdBimap;
import org.mastodon.graph.ref.AbstractVertex;
import org.mastodon.model.HasLabel;
import org.mastodon.pool.ByteMappedElement;
import org.mastodon.pool.PoolObject;
import org.mastodon.spatial.HasTimepoint;
import org.mastodon.views.trackscheme.TrackSchemeGraph.TrackSchemeVertexPool;

/**
 * The vertex class for TrackScheme.
 *
 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
 */
public class TrackSchemeVertex
		extends AbstractVertex< TrackSchemeVertex, TrackSchemeEdge, TrackSchemeVertexPool, ByteMappedElement >
		implements HasLabel, HasTimepoint
{
	final ModelGraphWrapper< ?, ? >.ModelVertexWrapper modelVertex;

	@Override
	protected void setToUninitializedState()
	{
		super.setToUninitializedState();
		setScreenVertexIndex( -1 );
	}

	TrackSchemeVertex initModelId( final int modelVertexId )
	{
		setModelVertexId( modelVertexId );
		setLayoutX( 0 );
		setLayoutTimestamp( -1 );
		setLayoutInEdgeIndex( 0 );
		setGhost( false );
		updateTimepointFromModel();
		return this;
	}

	/**
	 * Gets the ID of the associated model vertex, as defined by a
	 * {@link GraphIdBimap}. For {@link PoolObject} model vertices, the ID will
	 * be the internal pool index of the model vertex.
	 *
	 * @return the ID of the associated model vertex.
	 */
	public int getModelVertexId()
	{
		return pool.origVertexIndex.get( this );
	}

	protected void setModelVertexId( final int id )
	{
		pool.origVertexIndex.setQuiet( this, id );
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

	TrackSchemeVertex( final TrackSchemeVertexPool pool )
	{
		super( pool );
		modelVertex = pool.modelGraphWrapper.createVertexWrapper( this );
	}

	/**
	 * Gets the label of associated model vertex.
	 *
	 * @return the label.
	 *
	 */
	@Override
	public String getLabel()
	{
		return modelVertex.getLabel();
	}

	/**
	 * Sets the label of associated model vertex.
	 *
	 * @param label
	 *            the label to set.
	 */
	@Override
	public void setLabel( final String label )
	{
		modelVertex.setLabel( label );
	}

	@Override
	public int getTimepoint()
	{
		return pool.timepoint.get( this );
	}

	protected void setTimepoint( final int timepoint )
	{
		pool.timepoint.setQuiet( this, timepoint );
	}

	protected void updateTimepointFromModel()
	{
		setFirstTimepoint( modelVertex.getFirstTimepoint() );
		setTimepoint( modelVertex.getTimepoint() );
	}

	public int getFirstTimepoint()
	{
		return pool.firstTimepoint.get( this );
	}

	protected void setFirstTimepoint( final int timepoint )
	{
		pool.firstTimepoint.setQuiet( this, timepoint );
	}

	/**
	 * Internal pool index of last {@link ScreenVertex} that was created for
	 * this vertex. Used for lookup when creating {@link ScreenEdge}s.
	 *
	 * @return internal pool index of associated {@link ScreenVertex}.
	 */
	public int getScreenVertexIndex()
	{
		return pool.screenVertexIndex.get( this );
	}

	protected void setScreenVertexIndex( final int screenVertexIndex )
	{
		pool.screenVertexIndex.setQuiet( this, screenVertexIndex );
	}

	public double getLayoutX()
	{
		return pool.layoutX.get( this );
	}

	protected void setLayoutX( final double x )
	{
		pool.layoutX.setQuiet( this, x );
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
		return pool.layoutTimeStamp.get( this );
	}

	public void setLayoutTimestamp( final int timestamp )
	{
		pool.layoutTimeStamp.setQuiet( this, timestamp );
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
		return pool.layoutInEdgeIndex.get( this );
	}

	protected void setLayoutInEdgeIndex( final int index )
	{
		pool.layoutInEdgeIndex.setQuiet( this, index );
	}

	/**
	 * A vertex is set to <em>ghost</em> if it is hit during a partial layout
	 * and is marked with a timestamp &lt; the current mark.
	 *
	 * @return whether this vertex is a ghost
	 */
	protected boolean isGhost()
	{
		return pool.ghost.get( this );
	}

	protected void setGhost( final boolean ghost )
	{
		pool.ghost.setQuiet( this, ghost );
	}

	public String getRootLabel()
	{
		return modelVertex.getRootLabel();
	}
}
