package org.mastodon.mamut.feature;

import org.mastodon.feature.Feature;
import org.mastodon.feature.FeatureProjection;
import org.mastodon.feature.IntFeatureProjection;
import org.mastodon.revised.model.mamut.Spot;

public class SpotFrameFeature implements Feature< Spot >
{
	SpotFrameFeature()
	{}

	@Override
	public FeatureProjection< Spot > project( final String projectionKey )
	{
		return new MyProjection();
	}

	private static final class MyProjection implements IntFeatureProjection< Spot >
	{

		@Override
		public boolean isSet( final Spot obj )
		{
			return true;
		}

		@Override
		public double value( final Spot o )
		{
			return o.getTimepoint();
		}
	}
}
