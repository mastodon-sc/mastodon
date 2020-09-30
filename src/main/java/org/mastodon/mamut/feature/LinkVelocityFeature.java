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
import org.mastodon.mamut.model.Link;
import org.mastodon.properties.DoublePropertyMap;
import org.scijava.plugin.Plugin;

public class LinkVelocityFeature implements Feature< Link >
{

	private static final String KEY = "Link velocity";

	private static final String HELP_STRING = "Computes the link velocity as the distance between "
			+ "the source and target spots divided by their frame difference. Units are in physical distance per frame.";

	private static final FeatureProjectionSpec PROJECTION_SPEC = new FeatureProjectionSpec( KEY, Dimension.VELOCITY );

	public static final Spec SPEC = new Spec();

	final DoublePropertyMap< Link > map;

	private final FeatureProjection< Link > projection;

	@Plugin( type = FeatureSpec.class )
	public static class Spec extends FeatureSpec< LinkVelocityFeature, Link >
	{
		public Spec()
		{
			super(
					KEY,
					HELP_STRING,
					LinkVelocityFeature.class,
					Link.class,
					Multiplicity.SINGLE,
					PROJECTION_SPEC );
		}
	}

	LinkVelocityFeature( final DoublePropertyMap< Link > map, final String units )
	{
		this.map = map;
		this.projection = FeatureProjections.project( key( PROJECTION_SPEC ), map, units );
	}

	@Override
	public FeatureProjection< Link > project( final FeatureProjectionKey key )
	{
		return projection.getKey().equals( key ) ? projection : null;
	}

	@Override
	public Set< FeatureProjection< Link > > projections()
	{
		return Collections.singleton( projection );
	}

	@Override
	public Spec getSpec()
	{
		return SPEC;
	}

	@Override
	public void invalidate( final Link link )
	{
		map.remove( link );
	}
}
