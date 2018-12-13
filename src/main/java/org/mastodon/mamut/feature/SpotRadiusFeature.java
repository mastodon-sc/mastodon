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
import org.mastodon.feature.Multiplicity;
import org.mastodon.properties.DoublePropertyMap;
import org.mastodon.revised.model.mamut.Spot;
import org.scijava.plugin.Plugin;

public class SpotRadiusFeature implements Feature< Spot >
{

	private static final String KEY = "Spot radius";

	private static final String HELP_STRING = "Approximate the spot radius, using the geometric mean "
			+ "of the spot ellipsoid radiuses. This approxima tion is such that the sphere with the "
			+ "reported radius and the ellipsoid have the same volume.";

	private static final FeatureProjectionSpec PROJECTION_SPEC = new FeatureProjectionSpec( KEY, Dimension.LENGTH );

	public static final Spec SPEC = new Spec();

	private final FeatureProjection< Spot > projection;

	final DoublePropertyMap< Spot > map;

	SpotRadiusFeature( final DoublePropertyMap< Spot > map, final String spaceUnits )
	{
		this.map = map;
		this.projection = FeatureProjections.project( key( PROJECTION_SPEC ), map, spaceUnits );
	}

	@Plugin( type = FeatureSpec.class )
	public static class Spec extends FeatureSpec< SpotRadiusFeature, Spot >
	{
		public Spec()
		{
			super(
					KEY,
					HELP_STRING,
					SpotRadiusFeature.class,
					Spot.class,
					Multiplicity.SINGLE,
					PROJECTION_SPEC );
		}
	}

	@Override
	public FeatureProjection< Spot > project( final FeatureProjectionKey key )
	{
		return ( key.equals( key( PROJECTION_SPEC ) ) ) ? projection : null;
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
