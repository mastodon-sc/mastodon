package org.mastodon.mamut.feature;

import static org.mastodon.feature.FeatureProjectionKey.key;

import java.util.Collections;
import java.util.Set;

import org.mastodon.feature.Dimension;
import org.mastodon.feature.Feature;
import org.mastodon.feature.FeatureProjection;
import org.mastodon.feature.FeatureProjectionKey;
import org.mastodon.feature.FeatureProjectionSpec;
import org.mastodon.feature.FeatureProjections;
import org.mastodon.feature.FeatureSpec;
import org.mastodon.feature.IntFeatureProjection;
import org.mastodon.feature.Multiplicity;
import org.mastodon.mamut.model.Spot;
import org.mastodon.properties.IntPropertyMap;
import org.scijava.plugin.Plugin;

public class TrackSizeFeature implements Feature< Spot >
{

	public static final String KEY = "Track N spots";

	private static final String INFO_STRING = "Returns the number of spots in a track.";

	private static final FeatureProjectionSpec PROJECTION_SPEC = new FeatureProjectionSpec( KEY );

	public static final Spec SPEC = new Spec();

	final IntPropertyMap< Spot > map;

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
					Multiplicity.SINGLE,
					PROJECTION_SPEC );
		}
	}

	TrackSizeFeature( final IntPropertyMap< Spot > map )
	{
		this.map = map;
		this.projection = FeatureProjections.project( key( PROJECTION_SPEC ), map, Dimension.NONE_UNITS );
	}

	@Override
	public FeatureProjection< Spot > project( final FeatureProjectionKey key )
	{
		return projection.getKey().equals( key ) ? projection : null;
	}

	@Override
	public Set< FeatureProjection< Spot > > projections()
	{
		return Collections.singleton( projection );
	}

	@Override
	public Spec getSpec()
	{
		return SPEC;
	}
}
