/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2025 Tobias Pietzsch, Jean-Yves Tinevez
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
package org.mastodon.mamut.views.bdv;

import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.mastodon.mamut.model.BoundingSphereRadiusStatistics;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.properties.PropertyChangeListener;
import org.mastodon.spatial.VertexPositionListener;
import org.mastodon.views.bdv.overlay.OverlayGraph;
import org.mastodon.views.bdv.overlay.shapes.BdvOverlayProperties;
import org.mastodon.views.bdv.overlay.shapes.ellipsoid.BdvEllipsoidOverlayProperties;

/**
 * Provides spot {@link BdvOverlayProperties properties} for BDV
 * {@link OverlayGraph}, specific to the Mamut app.
 *
 * @author Tobias Pietzsch
 */
public class MamutBdvOverlayProperties implements BdvEllipsoidOverlayProperties< Spot, Link >
{
	private final ModelGraph modelGraph;

	private final BoundingSphereRadiusStatistics radiusStats;

	public MamutBdvOverlayProperties(
			final ModelGraph modelGraph,
			final BoundingSphereRadiusStatistics radiusStats )
	{
		this.modelGraph = modelGraph;
		this.radiusStats = radiusStats;
	}

	@Override
	public void localize( final Spot v, final double[] position )
	{
		v.localize( position );
	}

	@Override
	public double getDoublePosition( final Spot v, final int d )
	{
		return v.getDoublePosition( d );
	}

	@Override
	public void setPosition( final Spot v, final double position, final int d )
	{
		v.setPosition( position, d );
	}

	@Override
	public void setPosition( final Spot v, final double[] position )
	{
		v.setPosition( position );
	}

	@Override
	public void getCovariance( final Spot v, final double[][] mat )
	{
		v.getCovariance( mat );
	}

	@Override
	public void setCovariance( final Spot v, final double[][] mat )
	{
		v.setCovariance( mat );
	}

	@Override
	public double getBoundingSphereRadiusSquared( final Spot v )
	{
		return v.getBoundingSphereRadiusSquared();
	}

	@Override
	public int getTimepoint( final Spot v )
	{
		return v.getTimepoint();
	}

	@Override
	public String getLabel( final Spot v )
	{
		return v.getLabel();
	}

	@Override
	public void setLabel( final Spot v, final String label )
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
	public Spot addVertex( final Spot ref )
	{
		return modelGraph.addVertex( ref );
	}

	@Override
	public Spot initVertex( final Spot spot, final int timepoint, final double[] position, final double radius )
	{
		return spot.init( timepoint, position, radius );
	}

	@Override
	public Spot initVertex( final Spot spot, final int timepoint, final double[] position, final double[][] covariance )
	{
		return spot.init( timepoint, position, covariance );
	}

	@Override
	public Link addEdge( final Spot source, final Spot target, final Link ref )
	{
		return modelGraph.addEdge( source, target, ref );
	}

	@Override
	public Link insertEdge( final Spot source, final int sourceOutIndex, final Spot target, final int targetInIndex,
			final Link ref )
	{
		return modelGraph.insertEdge( source, sourceOutIndex, target, targetInIndex, ref );
	}

	@Override
	public Link initEdge( final Link link )
	{
		return link.init();
	}

	@Override
	public void removeEdge( final Link edge )
	{
		modelGraph.remove( edge );
	}

	@Override
	public void removeVertex( final Spot vertex )
	{
		modelGraph.remove( vertex );
	}

	@Override
	public void notifyGraphChanged()
	{
		modelGraph.notifyGraphChanged();
	}

	@Override
	public ReentrantReadWriteLock getLock()
	{
		return modelGraph.getLock();
	}

	@Override
	public boolean addVertexLabelListener( final PropertyChangeListener< Spot > listener )
	{
		return modelGraph.addVertexLabelListener( listener );
	}

	@Override
	public boolean removeVertexLabelListener( final PropertyChangeListener< Spot > vertexLabelListener )
	{
		return modelGraph.removeVertexLabelListener( vertexLabelListener );
	}

	@Override
	public boolean addVertexPositionListener( final VertexPositionListener< Spot > listener )
	{
		return modelGraph.addVertexPositionListener( listener );
	}

	@Override
	public boolean removeVertexPositionListener( final VertexPositionListener< Spot > listener )
	{
		return modelGraph.removeVertexPositionListener( listener );
	}
}
