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
package org.mastodon.mamut.model;

import org.mastodon.mamut.model.branch.BranchLink;
import org.mastodon.mamut.model.branch.BranchSpot;
import org.mastodon.mamut.model.branch.ModelBranchGraph;
import org.mastodon.views.bdv.overlay.OverlayGraph;
import org.mastodon.views.bdv.overlay.wrap.OverlayProperties;

/**
 * Provides branch vertex {@link OverlayProperties properties} for BDV
 * {@link OverlayGraph}.
 *
 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
 * @author Jean-Yves Tinevez
 */
public class BranchGraphModelOverlayProperties implements OverlayProperties< BranchSpot, BranchLink >
{
	private final ModelBranchGraph branchGraph;

	private final BoundingSphereRadiusStatistics radiusStats;

	private final ModelGraph graph;

	public BranchGraphModelOverlayProperties(
			final ModelBranchGraph branchGraph,
			final ModelGraph graph,
			final BoundingSphereRadiusStatistics radiusStats )
	{
		this.branchGraph = branchGraph;
		this.graph = graph;
		this.radiusStats = radiusStats;
	}

	@Override
	public void localize( final BranchSpot v, final double[] position )
	{
		v.localize( position );
	}

	@Override
	public double getDoublePosition( final BranchSpot v, final int d )
	{
		return v.getDoublePosition( d );
	}

	@Override
	public void getCovariance( final BranchSpot v, final double[][] mat )
	{
		final Spot ref = graph.vertexRef();
		branchGraph.getLastLinkedVertex( v, ref ).getCovariance( mat );
		graph.releaseRef( ref );
	}

	@Override
	public double getBoundingSphereRadiusSquared( final BranchSpot v )
	{
		final Spot ref = graph.vertexRef();
		final double r2 = branchGraph.getLastLinkedVertex( v, ref ).getBoundingSphereRadiusSquared();
		graph.releaseRef( ref );
		return r2;
	}

	@Override
	public int getTimepoint( final BranchSpot v )
	{
		return v.getTimepoint();
	}

	@Override
	public String getLabel( final BranchSpot v )
	{
		return v.getLabel();
	}

	@Override
	public void setLabel( final BranchSpot v, final String label )
	{
		v.setLabel( label );
	}

	@Override
	public double getMaxBoundingSphereRadiusSquared( final int timepoint )
	{
		radiusStats.readLock().lock();
		try
		{
			return radiusStats.getMaxBoundingSphereRadiusSquared( timepoint );
		}
		finally
		{
			radiusStats.readLock().unlock();
		}
	}

	@Override
	public void setPosition( final BranchSpot v, final double position, final int d )
	{}

	@Override
	public void setPosition( final BranchSpot v, final double[] position )
	{}

	@Override
	public void setCovariance( final BranchSpot v, final double[][] mat )
	{}

	@Override
	public BranchLink addEdge( final BranchSpot source, final BranchSpot target, final BranchLink ref )
	{
		throw new UnsupportedOperationException( "Cannot modify a branch graph." );
	}

	@Override
	public void removeEdge( final BranchLink e )
	{
		throw new UnsupportedOperationException( "Cannot modify a branch graph." );
	}

	@Override
	public void removeVertex( final BranchSpot v )
	{
		throw new UnsupportedOperationException( "Cannot modify a branch graph." );
	}

	@Override
	public void notifyGraphChanged()
	{}

	@Override
	public BranchSpot addVertex( final BranchSpot ref )
	{
		throw new UnsupportedOperationException( "Cannot modify a branch graph." );
	}

	@Override
	public BranchSpot initVertex( final BranchSpot v, final int timepoint, final double[] position,
			final double radius )
	{
		throw new UnsupportedOperationException( "Cannot modify a branch graph." );
	}

	@Override
	public BranchSpot initVertex( final BranchSpot v, final int timepoint, final double[] position,
			final double[][] covariance )
	{
		throw new UnsupportedOperationException( "Cannot modify a branch graph." );
	}

	@Override
	public BranchLink insertEdge( final BranchSpot source, final int sourceOutIndex, final BranchSpot target,
			final int targetInIndex, final BranchLink ref )
	{
		throw new UnsupportedOperationException( "Cannot modify a branch graph." );
	}

	@Override
	public BranchLink initEdge( final BranchLink e )
	{
		throw new UnsupportedOperationException( "Cannot modify a branch graph." );
	}
}
