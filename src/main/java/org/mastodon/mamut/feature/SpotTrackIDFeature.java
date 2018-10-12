package org.mastodon.mamut.feature;

import org.mastodon.collection.RefIntMap;
import org.mastodon.feature.Dimension;
import org.mastodon.feature.Feature;
import org.mastodon.feature.FeatureProjection;
import org.mastodon.feature.FeatureProjectionSpec;
import org.mastodon.feature.FeatureProjections;
import org.mastodon.feature.FeatureSpec;
import org.mastodon.feature.IntFeatureProjection;
import org.mastodon.revised.model.mamut.Spot;
import org.scijava.plugin.Plugin;

public class SpotTrackIDFeature implements Feature< Spot >
{

	public static final String KEY = "Spot track ID";

	private static final String HELP_STRING = "Returns the ID of the track each spot belongs to.";

	RefIntMap< Spot > map;

	private final IntFeatureProjection< Spot > projection;

	@Plugin( type = FeatureSpec.class )
	public static class Spec extends FeatureSpec< SpotTrackIDFeature, Spot >
	{
		public Spec()
		{
			super(
					KEY,
					HELP_STRING,
					SpotTrackIDFeature.class,
					Spot.class,
					FeatureProjectionSpec.standard( KEY, Dimension.NONE ) );
		}
	}

	SpotTrackIDFeature( final RefIntMap< Spot > map )
	{
		this.map = map;
		this.projection = FeatureProjections.project( map, Dimension.NONE_UNITS );
	}

	@Override
	public FeatureProjection< Spot > project( final String projectionKey )
	{
		return KEY.equals( projectionKey ) ? projection : null;
	}

	@Override
	public String[] projectionKeys()
	{
		return new String[] { KEY };
	}
}
