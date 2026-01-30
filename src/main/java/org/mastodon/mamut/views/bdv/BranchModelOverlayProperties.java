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

import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.mastodon.mamut.model.BoundingSphereRadiusStatistics;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.model.branch.BranchLink;
import org.mastodon.mamut.model.branch.BranchSpot;
import org.mastodon.mamut.model.branch.ModelBranchGraph;
import org.mastodon.properties.PropertyChangeListener;
import org.mastodon.spatial.VertexPositionListener;
import org.mastodon.views.bdv.overlay.OverlayGraph;
import org.mastodon.views.bdv.overlay.shapes.BdvOverlayProperties;
import org.mastodon.views.bdv.overlay.shapes.ellipsoid.BdvEllipsoidOverlayProperties;

/**
 * Provides branch vertex {@link BdvOverlayProperties properties} for BDV
 * {@link OverlayGraph}.
 *
 * @author Tobias Pietzsch
 * @author Jean-Yves Tinevez
 */
public class BranchModelOverlayProperties implements BdvEllipsoidOverlayProperties< BranchSpot, BranchLink >
{
	private final ModelBranchGraph branchGraph;

	private final BoundingSphereRadiusStatistics radiusStats;

	private final ModelGraph graph;

	/** Keeps track of forwarded listeners, so that we can remove them later. */
	private final Map< PropertyChangeListener< BranchSpot >, PropertyChangeListener< Spot > > forwardPropertyChangeListenerMap;

	/** Keeps track of position listeners, so that we can remove them later. */
	private final Map< VertexPositionListener< BranchSpot >, VertexPositionListener< Spot > > forwardPositionListenerMap;

	public BranchModelOverlayProperties(
			final ModelBranchGraph branchGraph,
			final ModelGraph graph,
			final BoundingSphereRadiusStatistics radiusStats )
	{
		this.branchGraph = branchGraph;
		this.graph = graph;
		this.radiusStats = radiusStats;
		this.forwardPropertyChangeListenerMap = new WeakHashMap<>();
		this.forwardPositionListenerMap = new WeakHashMap<>();
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

	@Override
	public ReentrantReadWriteLock getLock()
	{
		return branchGraph.getLock();
	}

	@Override
	public boolean addVertexLabelListener( final PropertyChangeListener< BranchSpot > listener )
	{
		return graph.addVertexLabelListener( forwardListener( listener ) );
	}

	@Override
	public boolean removeVertexLabelListener( final PropertyChangeListener< BranchSpot > vertexLabelListener )
	{
		final PropertyChangeListener< Spot > listener = forwardPropertyChangeListenerMap.remove( vertexLabelListener );
		if ( listener != null )
			return graph.removeVertexLabelListener( listener );
		return false;
	}

	private PropertyChangeListener< Spot > forwardListener( final PropertyChangeListener< BranchSpot > listener )
	{
		final PropertyChangeListener< Spot > l = new PropertyChangeListener< Spot >()
		{
			@Override
			public void propertyChanged( final Spot vertex )
			{
				final BranchSpot ref = branchGraph.vertexRef();
				final BranchSpot branchSpot = branchGraph.getBranchVertex( vertex, ref );
				listener.propertyChanged( branchSpot );
				branchGraph.releaseRef( ref );
			}
		};
		forwardPropertyChangeListenerMap.put( listener, l );
		return l;
	}

	@Override
	public boolean addVertexPositionListener( final VertexPositionListener< BranchSpot > listener )
	{
		return graph.addVertexPositionListener( forwardListener( listener ) );
	}

	private VertexPositionListener< Spot > forwardListener( final VertexPositionListener< BranchSpot > listener )
	{
		final VertexPositionListener< Spot > l = new VertexPositionListener< Spot >()
		{
			@Override
			public void vertexPositionChanged( final Spot vertex )
			{
				final BranchSpot ref = branchGraph.vertexRef();
				final BranchSpot branchSpot = branchGraph.getBranchVertex( vertex, ref );
				listener.vertexPositionChanged( branchSpot );
				branchGraph.releaseRef( ref );
			}
		};
		forwardPositionListenerMap.put( listener, l );
		return l;
	}


	@Override
	public boolean removeVertexPositionListener( final VertexPositionListener< BranchSpot > listener )
	{
		final VertexPositionListener< Spot > l = forwardPositionListenerMap.remove( listener );
		if ( l != null )
			return graph.removeVertexPositionListener( l );
		return false;
	}
}
