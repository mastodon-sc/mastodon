package org.mastodon.mamut.feature;

import org.mastodon.feature.Dimension;
import org.mastodon.feature.Feature;
import org.mastodon.feature.FeatureProjection;
import org.mastodon.feature.FeatureProjectionSpec;
import org.mastodon.feature.FeatureSpec;
import org.mastodon.feature.IntFeatureProjection;
import org.mastodon.feature.Multiplicity;
import org.mastodon.revised.model.mamut.Spot;
import org.scijava.plugin.Plugin;

public class SpotFrameFeature implements Feature< Spot >
{

	private static final String KEY = "Spot frame";

	private static final String HELP_STRING = "Exposes the spot frame.";

	SpotFrameFeature()
	{}

	@Plugin( type = FeatureSpec.class )
	public static class Spec extends FeatureSpec< SpotFrameFeature, Spot >
	{
		public Spec()
		{
			super(
					KEY,
					HELP_STRING,
					SpotFrameFeature.class,
					Spot.class,
					Multiplicity.SINGLE,
					new FeatureProjectionSpec( KEY, Dimension.NONE ) );
		}
	}

	@Override
	public FeatureProjection< Spot > project( final String projectionKey )
	{
		return new MyProjection();
	}

	@Override
	public String[] projectionKeys()
	{
		return new String[] { KEY };
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

		@Override
		public String units()
		{
			return "";
		}
	}
}
