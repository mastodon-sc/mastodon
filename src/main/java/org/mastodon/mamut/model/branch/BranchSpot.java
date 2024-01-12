/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2023 Tobias Pietzsch, Jean-Yves Tinevez
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
package org.mastodon.mamut.model.branch;

import org.mastodon.RefPool;
import org.mastodon.graph.ref.AbstractListenableVertex;
import org.mastodon.mamut.model.Spot;
import org.mastodon.model.HasLabel;
import org.mastodon.pool.ByteMappedElement;
import org.mastodon.pool.attributes.IntAttributeValue;
import org.mastodon.spatial.HasTimepoint;

import net.imglib2.RealLocalizable;

public class BranchSpot extends AbstractListenableVertex< BranchSpot, BranchLink, BranchSpotPool, ByteMappedElement >
		implements HasTimepoint, HasLabel, RealLocalizable
{

	private final RefPool< Spot > vertexBimap;

	private final IntAttributeValue firstSpotId;

	private final IntAttributeValue lastSpotId;

	protected BranchSpot( final BranchSpotPool vertexPool, final RefPool< Spot > vertexBimap )
	{
		super( vertexPool );
		this.vertexBimap = vertexBimap;
		this.firstSpotId = pool.firstSpotId.createAttributeValue( this );
		this.lastSpotId = pool.lastSpotId.createAttributeValue( this );
	}

	@Override
	public String toString()
	{
		final Spot ref = vertexBimap.createRef();
		final String str = vertexBimap.getObject( getLastLinkedVertexId(), ref ).toString();
		vertexBimap.releaseRef( ref );
		return "bv(" + getInternalPoolIndex() + ") -> " + str;
	}

	protected int getFirstLinkedVertexId()
	{
		return firstSpotId.get();
	}

	protected int getLastLinkedVertexId()
	{
		return lastSpotId.get();
	}

	protected void setLinkedVertexId( final int firstSpotId, final int lastSpotId )
	{
		this.firstSpotId.set( firstSpotId );
		this.lastSpotId.set( lastSpotId );
	}

	public BranchSpot init( final Spot branchStart, final Spot branchEnd )
	{
		setLinkedVertexId( vertexBimap.getId( branchStart ), vertexBimap.getId( branchEnd ) );
		initDone();
		return this;
	}

	@Override
	protected void setToUninitializedState() throws IllegalStateException
	{
		super.setToUninitializedState();
		setLinkedVertexId( -1, -1 );
	}

	@Override
	public int numDimensions()
	{
		final Spot ref = vertexBimap.createRef();
		final int n = vertexBimap.getObject( getLastLinkedVertexId(), ref ).numDimensions();
		vertexBimap.releaseRef( ref );
		return n;
	}

	@Override
	public void localize( final float[] position )
	{
		final Spot ref = vertexBimap.createRef();
		vertexBimap.getObject( getLastLinkedVertexId(), ref ).localize( position );
		vertexBimap.releaseRef( ref );
	}

	@Override
	public void localize( final double[] position )
	{
		final Spot ref = vertexBimap.createRef();
		vertexBimap.getObject( getLastLinkedVertexId(), ref ).localize( position );
		vertexBimap.releaseRef( ref );
	}

	@Override
	public float getFloatPosition( final int d )
	{
		final Spot ref = vertexBimap.createRef();
		final float x = vertexBimap.getObject( getLastLinkedVertexId(), ref ).getFloatPosition( d );
		vertexBimap.releaseRef( ref );
		return x;
	}

	@Override
	public double getDoublePosition( final int d )
	{
		final Spot ref = vertexBimap.createRef();
		final double x = vertexBimap.getObject( getLastLinkedVertexId(), ref ).getDoublePosition( d );
		vertexBimap.releaseRef( ref );
		return x;
	}

	public int getFirstTimePoint()
	{
		final Spot ref = vertexBimap.createRef();
		final int t = vertexBimap.getObject( getFirstLinkedVertexId(), ref ).getTimepoint();
		vertexBimap.releaseRef( ref );
		return t;
	}

	@Override
	public int getTimepoint()
	{
		final Spot ref = vertexBimap.createRef();
		final int t = vertexBimap.getObject( getLastLinkedVertexId(), ref ).getTimepoint();
		vertexBimap.releaseRef( ref );
		return t;
	}

	@Override
	public String getLabel()
	{
		final Spot ref = vertexBimap.createRef();
		final String label = vertexBimap.getObject( getLastLinkedVertexId(), ref ).getLabel();
		vertexBimap.releaseRef( ref );
		return label;
	}

	@Override
	public void setLabel( final String label )
	{
		final Spot ref = vertexBimap.createRef();
		vertexBimap.getObject( getLastLinkedVertexId(), ref ).setLabel( label );
		vertexBimap.releaseRef( ref );
	}

	public String getFirstLabel()
	{
		final Spot ref = vertexBimap.createRef();
		final String label = vertexBimap.getObject( getFirstLinkedVertexId(), ref ).getLabel();
		vertexBimap.releaseRef( ref );
		return label;
	}
}
