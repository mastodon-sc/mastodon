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

public class SpotIntensityFeature implements Feature< Spot >
{
	
	public static final String KEY = "Spot intensity";

	private static final String HELP_STRING =
			"Computes spot intensity features like mean, median, etc for all the channels of the source image. "
			+ "All the pixels within the spot ellipsoid are taken into account.";

	public static final FeatureProjectionSpec MEAN_PROJECTION_SPEC = new FeatureProjectionSpec( "Mean", Dimension.INTENSITY );
	public static final FeatureProjectionSpec STD_PROJECTION_SPEC = new FeatureProjectionSpec( "Std", Dimension.INTENSITY );
	public static final FeatureProjectionSpec MIN_PROJECTION_SPEC = new FeatureProjectionSpec( "Min", Dimension.INTENSITY );
	public static final FeatureProjectionSpec MAX_PROJECTION_SPEC = new FeatureProjectionSpec( "Max", Dimension.INTENSITY );
	public static final FeatureProjectionSpec MEDIAN_PROJECTION_SPEC = new FeatureProjectionSpec( "Median", Dimension.INTENSITY );
	public static final FeatureProjectionSpec SUM_PROJECTION_SPEC = new FeatureProjectionSpec( "Sum", Dimension.INTENSITY );

	public static final Spec SPEC = new Spec();

	@Plugin( type = FeatureSpec.class )
	public static class Spec extends FeatureSpec< SpotIntensityFeature, Spot >
	{
		public Spec()
		{
			super(
					KEY,
					HELP_STRING,
					SpotIntensityFeature.class,
					Spot.class,
					Multiplicity.ON_SOURCES,
					MEAN_PROJECTION_SPEC,
					STD_PROJECTION_SPEC,
					MIN_PROJECTION_SPEC,
					MAX_PROJECTION_SPEC,
					MEDIAN_PROJECTION_SPEC,
					SUM_PROJECTION_SPEC );
		}
	}

	private final Map< FeatureProjectionKey, FeatureProjection< Spot > > projectionMap;

	final List< DoublePropertyMap< Spot > > means;
	final List< DoublePropertyMap< Spot > > stds;
	final List< DoublePropertyMap< Spot > > mins;
	final List< DoublePropertyMap< Spot > > maxs;
	final List< DoublePropertyMap< Spot > > medians;
	final List< DoublePropertyMap< Spot > > sums;
	
	SpotIntensityFeature(
			final List< DoublePropertyMap< Spot > > means,
			final List< DoublePropertyMap< Spot > > stds,
			final List< DoublePropertyMap< Spot > > mins,
			final List< DoublePropertyMap< Spot > > maxs,
			final List< DoublePropertyMap< Spot > > medians,
			final List< DoublePropertyMap< Spot > > sums )
	{
		this.means = means;
		this.stds = stds;
		this.mins = mins;
		this.maxs = maxs;
		this.medians = medians;
		this.sums = sums;
		this.projectionMap = new LinkedHashMap<>( 6 * means.size() );
		for ( int iSource = 0; iSource < means.size(); iSource++ )
		{
			final FeatureProjectionKey meankey = key( MEAN_PROJECTION_SPEC, iSource );
			projectionMap.put( meankey, FeatureProjections.project( meankey, means.get( iSource ), Dimension.COUNTS_UNITS ) );

			final FeatureProjectionKey stdkey = key( STD_PROJECTION_SPEC, iSource );
			projectionMap.put( stdkey, FeatureProjections.project( stdkey, stds.get( iSource ), Dimension.COUNTS_UNITS ) );

			final FeatureProjectionKey minkey = key( MIN_PROJECTION_SPEC, iSource );
			projectionMap.put( minkey, FeatureProjections.project( minkey, mins.get( iSource ), Dimension.COUNTS_UNITS ) );

			final FeatureProjectionKey maxkey = key( MAX_PROJECTION_SPEC, iSource );
			projectionMap.put( maxkey, FeatureProjections.project( maxkey, maxs.get( iSource ), Dimension.COUNTS_UNITS ) );

			final FeatureProjectionKey mediankey = key( MEDIAN_PROJECTION_SPEC, iSource );
			projectionMap.put( mediankey, FeatureProjections.project( mediankey, medians.get( iSource ), Dimension.COUNTS_UNITS ) );

			final FeatureProjectionKey sumkey = key( SUM_PROJECTION_SPEC, iSource );
			projectionMap.put( sumkey, FeatureProjections.project( sumkey, sums.get( iSource ), Dimension.COUNTS_UNITS ) );
		}
	}

	public double getMean( final Spot spot, final int source )
	{
		return means.get( source ).getDouble( spot );
	}

	public double getStd( final Spot spot, final int source )
	{
		return stds.get( source ).getDouble( spot );
	}

	public double getMin( final Spot spot, final int source )
	{
		return mins.get( source ).getDouble( spot );
	}

	public double getMax( final Spot spot, final int source )
	{
		return maxs.get( source ).getDouble( spot );
	}

	public double getMedian( final Spot spot, final int source )
	{
		return medians.get( source ).getDouble( spot );
	}

	public double getSum( final Spot spot, final int source )
	{
		return sums.get( source ).getDouble( spot );
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
		stds.forEach( m -> m.remove( spot ) );
		mins.forEach( m -> m.remove( spot ) );
		maxs.forEach( m -> m.remove( spot ) );
		medians.forEach( m -> m.remove( spot ) );
		sums.forEach( m -> m.remove( spot ) );
	}
}
