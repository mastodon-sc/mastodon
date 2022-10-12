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
	public BranchSpot initVertex( final BranchSpot v, final int timepoint, final double[] position, final double radius )
	{
		throw new UnsupportedOperationException( "Cannot modify a branch graph." );
	}

	@Override
	public BranchSpot initVertex( final BranchSpot v, final int timepoint, final double[] position, final double[][] covariance )
	{
		throw new UnsupportedOperationException( "Cannot modify a branch graph." );
	}

	@Override
	public BranchLink insertEdge( final BranchSpot source, final int sourceOutIndex, final BranchSpot target, final int targetInIndex, final BranchLink ref )
	{
		throw new UnsupportedOperationException( "Cannot modify a branch graph." );
	}

	@Override
	public BranchLink initEdge( final BranchLink e )
	{
		throw new UnsupportedOperationException( "Cannot modify a branch graph." );
	}
}
