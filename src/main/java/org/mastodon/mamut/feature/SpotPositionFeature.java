package org.mastodon.mamut.feature;

import static org.mastodon.feature.FeatureProjectionKey.key;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.mastodon.feature.Dimension;
import org.mastodon.feature.Feature;
import org.mastodon.feature.FeatureProjection;
import org.mastodon.feature.FeatureProjectionKey;
import org.mastodon.feature.FeatureProjectionSpec;
import org.mastodon.feature.FeatureSpec;
import org.mastodon.feature.Multiplicity;
import org.mastodon.revised.model.mamut.Spot;
import org.scijava.plugin.Plugin;

public class SpotPositionFeature implements Feature< Spot >
{

	private static final String KEY = "Spot position";

	private static final String X_KEY = "X";

	private static final String Y_KEY = "Y";

	private static final String Z_KEY = "Z";

	private static final String HELP_STRING = "Exposes the spot X,Y, Z position.";

	private static final List< FeatureProjectionSpec > PROJECTION_SPECS = Arrays.asList( new FeatureProjectionSpec[] {
			new FeatureProjectionSpec( X_KEY, Dimension.POSITION ),
			new FeatureProjectionSpec( Y_KEY, Dimension.POSITION ),
			new FeatureProjectionSpec( Z_KEY, Dimension.POSITION ) } );

	public static final Spec SPEC = new Spec();

	private final FeatureProjection< Spot > projectionX;

	private final FeatureProjection< Spot > projectionY;

	private final FeatureProjection< Spot > projectionZ;

	SpotPositionFeature( final String spaceUnits )
	{
		this.projectionX = new MyProjection( 0, spaceUnits );
		this.projectionY = new MyProjection( 1, spaceUnits );
		this.projectionZ = new MyProjection( 2, spaceUnits );
	}

	@Plugin( type = FeatureSpec.class )
	public static class Spec extends FeatureSpec< SpotPositionFeature, Spot >
	{
		public Spec()
		{
			super(
					KEY,
					HELP_STRING,
					SpotPositionFeature.class,
					Spot.class,
					Multiplicity.SINGLE,
					PROJECTION_SPECS.get( 0 ),
					PROJECTION_SPECS.get( 1 ),
					PROJECTION_SPECS.get( 2 ) );
		}
	}

	@Override
	public FeatureProjection< Spot > project( final FeatureProjectionKey key )
	{
		if ( key.equals( key( PROJECTION_SPECS.get( 0 ) ) ) )
			return projectionX;
		else if ( key.equals( key( PROJECTION_SPECS.get( 1 ) ) ) )
			return projectionY;
		else if ( key.equals( key( PROJECTION_SPECS.get( 2 ) ) ) )
			return projectionZ;
		return null;
	}

	@Override
	public Set< FeatureProjection< Spot > > projections()
	{
		final Set< FeatureProjection< Spot > > set = new HashSet<>();
		set.add( projectionX );
		set.add( projectionY );
		set.add( projectionZ );
		return Collections.unmodifiableSet( set );
	}

	@Override
	public Spec getSpec()
	{
		return SPEC;
	}

	private static final class MyProjection implements FeatureProjection< Spot >
	{

		private final int dim;

		private final String spaceUnits;

		public MyProjection( final int dim, final String spaceUnits )
		{
			this.dim = dim;
			this.spaceUnits = spaceUnits;
		}

		@Override
		public FeatureProjectionKey getKey()
		{
			return key( PROJECTION_SPECS.get( dim ) );
		}

		@Override
		public boolean isSet( final Spot obj )
		{
			return true;
		}

		@Override
		public double value( final Spot o )
		{
			return o.getDoublePosition( dim );
		}

		@Override
		public String units()
		{
			return spaceUnits;
		}
	}
}
