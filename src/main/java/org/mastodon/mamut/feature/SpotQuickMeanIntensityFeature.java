package org.mastodon.mamut.feature;

import static org.mastodon.feature.FeatureProjectionKey.key;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
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

public class SpotQuickMeanIntensityFeature implements Feature< Spot >
{

	public static final String KEY = "Spot quick mean";

	private static final String HELP_STRING =
			"Computes the mean intensity of spots using the highest resolution level to speedup calculation."
					+ "It is recommended to use the 'Spot intensity' feature when the best accuracy is required.";

	public static final FeatureProjectionSpec PROJECTION_SPEC = new FeatureProjectionSpec( "Mean", Dimension.INTENSITY );

	public static final Spec SPEC = new Spec();

	@Plugin( type = FeatureSpec.class )
	public static class Spec extends FeatureSpec< SpotQuickMeanIntensityFeature, Spot >
	{
		public Spec()
		{
			super(
					KEY,
					HELP_STRING,
					SpotQuickMeanIntensityFeature.class,
					Spot.class,
					Multiplicity.ON_SOURCES,
					PROJECTION_SPEC );
		}
	}

	private final Map< FeatureProjectionKey, FeatureProjection< Spot > > projectionMap;

	final List< DoublePropertyMap< Spot > > means;

	SpotQuickMeanIntensityFeature( final List< DoublePropertyMap< Spot > > means )
	{
		this.means = means;
		this.projectionMap = new LinkedHashMap<>( 6 * means.size() );
		for ( int iSource = 0; iSource < means.size(); iSource++ )
		{
			final FeatureProjectionKey meankey = key( PROJECTION_SPEC, iSource );
			projectionMap.put( meankey, FeatureProjections.project( meankey, means.get( iSource ), Dimension.COUNTS_UNITS ) );
		}
	}

	public double getMean( final Spot spot, final int source )
	{
		return means.get( source ).getDouble( spot );
	}

	@Override
	public FeatureProjection< Spot > project( final FeatureProjectionKey key )
	{
		return projectionMap.get( key );
	}

	@Override
	public Set< FeatureProjection< Spot > > projections()
	{
		return new LinkedHashSet<>( projectionMap.values() );
	}

	@Override
	public Spec getSpec()
	{
		return SPEC;
	}

	@Override
	public void invalidate( final Spot spot )
	{
		means.forEach( m -> m.remove( spot ) );
	}
}
