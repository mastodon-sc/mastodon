package org.mastodon.mamut.model;

import org.mastodon.views.bvv.wrap.BvvModelGraphProperties;
import org.mastodon.mamut.model.BoundingSphereRadiusStatistics;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;

public class MamutModelGraphPropertiesBvv implements BvvModelGraphProperties< Spot, Link >
{
	private final ModelGraph modelGraph;

	private final BoundingSphereRadiusStatistics radiusStats;

	public MamutModelGraphPropertiesBvv(
			final ModelGraph modelGraph,
			final BoundingSphereRadiusStatistics radiusStats )
	{
		this.modelGraph = modelGraph;
		this.radiusStats = radiusStats;
	}

	@Override
	public double getDoublePosition( final Spot spot, final int d )
	{
		return spot.getDoublePosition( d );
	}

	@Override
	public void getCovariance( final Spot spot, final double[][] mat )
	{
		spot.getCovariance( mat );
	}

	@Override
	public int getTimepoint( final Spot spot )
	{
		return spot.getTimepoint();
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
	public Link initEdge( final Link link )
	{
		return link.init();
	}

	@Override
	public void removeEdge( final Link link )
	{
		modelGraph.remove( link );
	}

	@Override
	public void removeVertex( final Spot spot )
	{
		modelGraph.remove( spot );
	}

	@Override
	public void notifyGraphChanged()
	{
		modelGraph.notifyGraphChanged();
	}
}
