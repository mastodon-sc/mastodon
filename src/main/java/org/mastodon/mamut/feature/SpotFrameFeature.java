package org.mastodon.mamut.feature;

import static org.mastodon.feature.FeatureProjectionKey.key;

import java.util.Collections;
import java.util.Set;

import org.mastodon.feature.Feature;
import org.mastodon.feature.FeatureProjection;
import org.mastodon.feature.FeatureProjectionKey;
import org.mastodon.feature.FeatureProjectionSpec;
import org.mastodon.feature.FeatureSpec;
import org.mastodon.feature.IntFeatureProjection;
import org.mastodon.feature.Multiplicity;
import org.mastodon.mamut.model.Spot;
import org.scijava.plugin.Plugin;

public class SpotFrameFeature implements Feature< Spot >
{

	private static final String KEY = "Spot frame";

	private static final String HELP_STRING = "Exposes the spot frame.";

	private static final FeatureProjectionSpec PROJECTION_SPEC = new FeatureProjectionSpec( KEY );

	public static final Spec SPEC = new Spec();

	private final IntFeatureProjection< Spot > projection;

	SpotFrameFeature()
	{
		this.projection = new MyProjection();
	}

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
					PROJECTION_SPEC );
		}
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
	public void invalidate( final Spot spot )
	{}

	private static final class MyProjection implements IntFeatureProjection< Spot >
	{

		@Override
		public FeatureProjectionKey getKey()
		{
			return key( PROJECTION_SPEC );
		}

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
