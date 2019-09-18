package org.mastodon.mamut.feature;

import org.mastodon.collection.RefCollection;
import org.mastodon.feature.FeatureModel;
import org.mastodon.feature.FeatureProjectionSpec;
import org.mastodon.feature.FeatureSpec;
import org.mastodon.feature.Multiplicity;
import org.mastodon.feature.update.SizedDeque;
import org.mastodon.feature.update.UpdateStack;
import org.mastodon.revised.model.mamut.Spot;
import org.scijava.plugin.Plugin;

public class SpotUpdateStack extends UpdateStack< Spot >
{

	public static final String KEY = "Update stack " + Spot.class.getSimpleName();

	private static final String INFO = "Specialized features that has no projections, but stores the stack of spot changes "
			+ "since last feature computation. Is used by some feature computers that offers incremental feature "
			+ "computation.";

	public static final Spec SPEC = new Spec();

	public SpotUpdateStack( final RefCollection< Spot > pool )
	{
		super( pool );
	}

	/**
	 * For deserialization only.
	 */
	SpotUpdateStack( final RefCollection< Spot > pool, final SizedDeque< UpdateState< Spot > > stateStack )
	{
		super( pool, stateStack );
	}

	public static SpotUpdateStack getOrCreate( final FeatureModel featureModel, final RefCollection< Spot > pool )
	{
		final SpotUpdateStack retrieved = ( SpotUpdateStack ) featureModel.getFeature( SpotUpdateStack.SPEC );
		if ( null != retrieved )
			return retrieved;

		final SpotUpdateStack feature = new SpotUpdateStack( pool );
		featureModel.declareFeature( feature );
		return feature;
	}

	@Override
	public FeatureSpec< SpotUpdateStack, Spot > getSpec()
	{
		return SPEC;
	}

	@Plugin( type = FeatureSpec.class )
	public static class Spec extends FeatureSpec< SpotUpdateStack, Spot >
	{
		public Spec()
		{
			super(
					KEY,
					INFO,
					SpotUpdateStack.class,
					Spot.class,
					Multiplicity.SINGLE,
					new FeatureProjectionSpec[] {} );
		}
	}
}
