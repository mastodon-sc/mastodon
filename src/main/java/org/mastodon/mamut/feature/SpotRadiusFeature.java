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
import org.mastodon.mamut.model.Spot;
import org.mastodon.properties.DoublePropertyMap;
import org.scijava.plugin.Plugin;

public class SpotRadiusFeature implements Feature< Spot >
{

	private static final String KEY = "Spot radius";

	private static final String HELP_STRING = "Computes the spot equivalent radius. "
			+ "This is the radius of the sphere that would have the same volume "
			+ "that of the spot.";

	private static final FeatureProjectionSpec PROJECTION_SPEC = new FeatureProjectionSpec( KEY, Dimension.LENGTH );

	public static final Spec SPEC = new Spec();

	final DoublePropertyMap< Spot > map;

	private final FeatureProjection< Spot > projection;

	SpotRadiusFeature( final DoublePropertyMap< Spot > map, final String units )
	{
		this.map = map;
		this.projection = FeatureProjections.project( key( PROJECTION_SPEC ), map, units );
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

	public double get(final Spot spot)
	{
		return map.getDouble( spot );
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

	@Override
	public void remove( final Spot spot )
	{
		map.remove( spot );
	}
}
