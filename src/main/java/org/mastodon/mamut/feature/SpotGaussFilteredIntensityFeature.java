package org.mastodon.mamut.feature;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mastodon.RefPool;
import org.mastodon.feature.Dimension;
import org.mastodon.feature.Feature;
import org.mastodon.feature.FeatureProjection;
import org.mastodon.feature.FeatureProjectionSpec;
import org.mastodon.feature.FeatureProjections;
import org.mastodon.feature.FeatureSpec;
import org.mastodon.properties.DoublePropertyMap;
import org.mastodon.revised.model.mamut.Spot;
import org.scijava.plugin.Plugin;

public class SpotGaussFilteredIntensityFeature implements Feature< Spot >
{
	public static final String KEY = "Spot gaussian-filtered intensity";

	public static final Spec SPEC = new Spec();

	private static final String HELP_STRING =
			"Computes the average intensity and its standard deviation inside spots "
					+ "over all sources of the dataset. "
					+ "The average is calculated by a weighted mean over the pixels of the spot, "
					+ "weighted by a gaussian centered in the spot and with a sigma value equal "
					+ "to the minimal radius of the ellipsoid divided by "
					+ SpotGaussFilteredIntensityFeatureComputer.SIGMA_FACTOR + ".";

	private static final String MEAN_PROJECTIONS_KEY = "Mean";

	private static final String STD_PROJECTIONS_KEY = "Std";

	@Plugin( type = FeatureSpec.class )
	public static class Spec extends FeatureSpec< SpotGaussFilteredIntensityFeature, Spot >
	{
		public Spec()
		{
			super(
					KEY,
					HELP_STRING,
					SpotGaussFilteredIntensityFeature.class,
					Spot.class,
					FeatureProjectionSpec.onSources( MEAN_PROJECTIONS_KEY, Dimension.INTENSITY ),
					FeatureProjectionSpec.onSources( STD_PROJECTIONS_KEY, Dimension.INTENSITY ) );
		}
	}

	final List< DoublePropertyMap< Spot > > means;

	final List< DoublePropertyMap< Spot > > stds;

	private final Map< String, FeatureProjection< Spot > > projectionMap;

	SpotGaussFilteredIntensityFeature( final int nSources, final RefPool< Spot > pool )
	{
		// Just used to generate projection names.
		final FeatureProjectionSpec meanProjections = FeatureProjectionSpec.onSources( MEAN_PROJECTIONS_KEY, Dimension.INTENSITY );
		final FeatureProjectionSpec stdProjections = FeatureProjectionSpec.onSources( STD_PROJECTIONS_KEY, Dimension.INTENSITY );

		this.means = new ArrayList<>( nSources );
		this.stds = new ArrayList<>( nSources );
		this.projectionMap = new HashMap<>( 2 * nSources );
		for ( int iSource = 0; iSource < nSources; iSource++ )
		{
			/*
			 * We use property maps, so that they are automatically cleaned if
			 * an object is removed from the graph.
			 */
			final DoublePropertyMap< Spot > m = new DoublePropertyMap<>( pool, Double.NaN );
			means.add( m );
			projectionMap.put( meanProjections.projectionKey( iSource ), FeatureProjections.project( m, Dimension.COUNTS_UNITS ) );

			final DoublePropertyMap< Spot > s = new DoublePropertyMap<>( pool, Double.NaN );
			stds.add( s );
			projectionMap.put( stdProjections.projectionKey( iSource ), FeatureProjections.project( s, Dimension.COUNTS_UNITS ) );
		}
	}

	@Override
	public FeatureProjection< Spot > project( final String projectionKey )
	{
		return projectionMap.get( projectionKey );
	}

	@Override
	public String[] projectionKeys()
	{
		return projectionMap.keySet().toArray( new String[] {} );
	}
}
