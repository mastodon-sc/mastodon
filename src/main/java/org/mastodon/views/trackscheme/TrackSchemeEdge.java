/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2024 Tobias Pietzsch, Jean-Yves Tinevez
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
import org.mastodon.graph.ref.AbstractEdge;
import org.mastodon.pool.ByteMappedElement;
import org.mastodon.pool.PoolObject;
import org.mastodon.views.trackscheme.TrackSchemeGraph.TrackSchemeEdgePool;

public class TrackSchemeEdge
		extends AbstractEdge< TrackSchemeEdge, TrackSchemeVertex, TrackSchemeEdgePool, ByteMappedElement >
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
