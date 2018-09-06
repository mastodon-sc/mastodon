package org.mastodon.mamut.feature;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mastodon.RefPool;
import org.mastodon.collection.RefDoubleMap;
import org.mastodon.collection.ref.RefDoubleHashMap;
import org.mastodon.feature.Feature;
import org.mastodon.feature.FeatureProjection;
import org.mastodon.feature.FeatureProjectionSpec;
import org.mastodon.feature.FeatureProjections;
import org.mastodon.feature.FeatureSpec;
import org.mastodon.revised.model.mamut.Spot;
import org.scijava.plugin.Plugin;

public class SpotGaussFilteredIntensityFeature implements Feature< Spot >
{

	public static final String KEY = "Spot gaussian-filtered intensity";

	private static final String MEAN_PROJECTIONS_KEY = "Mean";

	private static final String STD_PROJECTIONS_KEY = "Std";

	@Plugin( type = FeatureSpec.class )
	public static class SpotGaussFilteredIntensityFeatureSpec extends FeatureSpec< SpotGaussFilteredIntensityFeature, Spot >
	{
		public SpotGaussFilteredIntensityFeatureSpec()
		{
			super(
					KEY,
					SpotGaussFilteredIntensityFeature.class,
					Spot.class,
					FeatureProjectionSpec.onSources( MEAN_PROJECTIONS_KEY ),
					FeatureProjectionSpec.onSources( STD_PROJECTIONS_KEY ) );
		}
	}

	final List< RefDoubleMap< Spot > > means;

	final List< RefDoubleMap< Spot > > stds;

	private final Map< String, FeatureProjection< Spot > > projectionMap;

	SpotGaussFilteredIntensityFeature( final int nSources, final RefPool< Spot > pool )
	{
		// Just used to generate projection names.
		final FeatureProjectionSpec meanProjections = FeatureProjectionSpec.onSources( MEAN_PROJECTIONS_KEY );
		final FeatureProjectionSpec stdProjections = FeatureProjectionSpec.onSources( MEAN_PROJECTIONS_KEY );

		this.means = new ArrayList<>( nSources );
		this.stds = new ArrayList<>( nSources );
		this.projectionMap = new HashMap<>( 2 * nSources );
		for ( int iSource = 0; iSource < nSources; iSource++ )
		{
			final RefDoubleHashMap< Spot > m = new RefDoubleHashMap<>( pool, Double.NaN );
			means.add( m );
			projectionMap.put( meanProjections.projectionKey( iSource ), FeatureProjections.project( m ) );

			final RefDoubleHashMap< Spot > s = new RefDoubleHashMap<>( pool, Double.NaN );
			stds.add( s );
			projectionMap.put( stdProjections.projectionKey( iSource ), FeatureProjections.project( s ) );
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
