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

public class TrackSizeFeature implements Feature< Spot >
{

	public static final String KEY = "Track N spots";

	private static final String INFO_STRING = "Returns the number of spots in a track.";

	RefIntMap< Spot > map;

	private final IntFeatureProjection< Spot > projection;

	@Plugin( type = FeatureSpec.class )
	public static class Spec extends FeatureSpec< TrackSizeFeature, Spot >
	{
		public Spec()
		{
			super(
					KEY,
					INFO_STRING,
					TrackSizeFeature.class,
					Spot.class,
					FeatureProjectionSpec.standard( KEY, Dimension.NONE ) );
		}
	}

	TrackSizeFeature( final RefIntMap< Spot > map )
	{
		this.map = map;
		this.projection = FeatureProjections.project( map, Dimension.NONE_UNITS );
	}

	@Override
	public FeatureProjection< Spot > project( final String projectionKey )
	{
		return projectionKey.equals( KEY ) ? projection : null;
	}

	@Override
	public String[] projectionKeys()
	{
		return new String[] { KEY };
	}
}
