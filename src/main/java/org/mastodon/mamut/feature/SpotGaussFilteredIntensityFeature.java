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
import org.mastodon.feature.FeatureSpec;
import org.mastodon.feature.util.FeatureProjections;
import org.mastodon.revised.model.mamut.Spot;
import org.scijava.plugin.Plugin;

public class SpotGaussFilteredIntensityFeature implements Feature< Spot >
{

	public static final String KEY = "Spot gaussian-filtered intensity";

	/**
	 * Let's try to do some kind of a dynamic spec.
	 */
	@Plugin( type = FeatureSpec.class )
	public static class SpotGaussFilteredIntensityFeatureSpec extends FeatureSpec< SpotGaussFilteredIntensityFeature, Spot >
	{
		private String[] projections;

		public SpotGaussFilteredIntensityFeatureSpec()
		{
			super( KEY, SpotGaussFilteredIntensityFeature.class, Spot.class );
			// Will be updated later.
			this.projections = new String[] {};
		}

		@Override
		public String[] getProjections()
		{
			return projections;
		}

		void setProjections( final String[] projections )
		{
			this.projections = projections;
		}
	}

	final List< RefDoubleMap< Spot > > means;

	final List< RefDoubleMap< Spot > > stds;

	private final Map< String, FeatureProjection< Spot > > projectionMap;

	@Override
	public FeatureProjection< Spot > project( final String projectionKey )
	{
		return projectionMap.get( projectionKey );
	}

	SpotGaussFilteredIntensityFeature( final int nSources, final RefPool< Spot > pool )
	{
		this.means = new ArrayList<>(nSources);
		this.stds = new ArrayList<>(nSources);
		this.projectionMap = new HashMap<>( 2 * nSources );
		for ( int iSource = 0; iSource < nSources; iSource++ )
		{
			final RefDoubleHashMap< Spot > m = new RefDoubleHashMap<>( pool, Double.NaN );
			means.add( m );
			projectionMap.put( meanProjectionName( iSource ), FeatureProjections.project( m ) );

			final RefDoubleHashMap< Spot > s = new RefDoubleHashMap<>( pool, Double.NaN );
			stds.add( s );
			projectionMap.put( stdProjectionName( iSource ), FeatureProjections.project( s ) );
		}
	}

	static String meanProjectionName( final int iSource )
	{
		return "Mean ch " + iSource;
	}

	static String stdProjectionName( final int iSource )
	{
		return "Std ch " + iSource;
	}
}
