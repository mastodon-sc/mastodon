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
package org.mastodon.views.grapher.datagraph;

import org.mastodon.graph.GraphIdBimap;
import org.mastodon.graph.ref.AbstractVertex;
import org.mastodon.model.HasLabel;
import org.mastodon.pool.ByteMappedElement;
import org.mastodon.pool.PoolObject;
import org.mastodon.spatial.HasTimepoint;
import org.mastodon.views.grapher.datagraph.DataGraph.DataVertexPool;

import net.imglib2.RealLocalizable;

public class DataVertex extends AbstractVertex< DataVertex, DataEdge, DataVertexPool, ByteMappedElement >
		implements HasLabel, HasTimepoint, RealLocalizable
{
	final ModelGraphWrapper< ?, ? >.ModelVertexWrapper modelVertex;

	@Override
	protected void setToUninitializedState()
	{
		super.setToUninitializedState();
		setScreenVertexIndex( -1 );
	}

	DataVertex initModelId( final int modelVertexId, final int timepoint )
	{
		setModelVertexId( modelVertexId );
		setLayoutX( Double.NaN );
		setLayoutY( Double.NaN );
		setTimepoint( timepoint );
		setLayoutInEdgeIndex( 0 );
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
		return String.format( "DataVertex( ID=%d, LABEL=%s, X=%.2f, Y=%.2f )",
				getModelVertexId(),
				getLabel(),
				getLayoutX(),
				getLayoutY() );
	}

	DataVertex( final DataVertexPool pool )
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

	public double getLayoutX()
	{
		return pool.layoutX.get( this );
	}

	protected void setLayoutX( final double x )
	{
		pool.layoutX.setQuiet( this, x );
	}

	public double getLayoutY()
	{
		return pool.layoutY.get( this );
	}

	protected void setLayoutY( final double y )
	{
		pool.layoutY.setQuiet( this, y );
	}

	@Override
	public int getTimepoint()
	{
		return pool.modelTimepoint.get( this );
	}

	public void setTimepoint( final int timepoint )
	{
		pool.modelTimepoint.setQuiet( this, timepoint );
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

	protected void setLayoutInEdgeIndex( final int index )
	{
		pool.layoutInEdgeIndex.setQuiet( this, index );
	}

	@Override
	public int numDimensions()
	{
		return 2;
	}

	@Override
	public double getDoublePosition( final int d )
	{
		return ( d == 0 ) ? pool.layoutX.get( this ) : pool.layoutY.get( this );
	}
}
