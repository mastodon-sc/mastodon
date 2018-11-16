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
import org.mastodon.revised.model.mamut.Link;
import org.scijava.plugin.Plugin;

public class LinkDisplacementFeature implements Feature< Link >
{

	private static final String KEY = "Link displacement";

	private static final String HELP_STRING = "Computes the link displacement in physical units "
			+ "as the distance between the source spot and the target spot.";

	private static final FeatureProjectionSpec PROJECTION_SPEC = new FeatureProjectionSpec( KEY, Dimension.VELOCITY );

	public static final Spec SPEC = new Spec();

	final DoublePropertyMap< Link > map;

	private final FeatureProjection< Link > projection;

	@Plugin( type = FeatureSpec.class )
	public static class Spec extends FeatureSpec< LinkDisplacementFeature, Link >
	{
		public Spec()
		{
			super(
					KEY,
					HELP_STRING,
					LinkDisplacementFeature.class,
					Link.class,
					Multiplicity.SINGLE,
					PROJECTION_SPEC );
		}
	}

	LinkDisplacementFeature( final DoublePropertyMap< Link > map, final String units )
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
	public Set< FeatureProjectionKey > projectionKeys()
	{
		return Collections.singleton( projection.getKey() );
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
}
