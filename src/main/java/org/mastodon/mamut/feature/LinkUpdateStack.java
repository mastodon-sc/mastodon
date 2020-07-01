package org.mastodon.mamut.feature;

import org.mastodon.collection.RefCollection;
import org.mastodon.feature.FeatureModel;
import org.mastodon.feature.FeatureProjectionSpec;
import org.mastodon.feature.FeatureSpec;
import org.mastodon.feature.Multiplicity;
import org.mastodon.feature.update.SizedDeque;
import org.mastodon.feature.update.UpdateStack;
import org.mastodon.mamut.model.Link;
import org.scijava.plugin.Plugin;

public class LinkUpdateStack extends UpdateStack< Link >
{

	public static final String KEY = "Update stack " + Link.class.getSimpleName();

	private static final String INFO = "Specialized features that has no projections, but stores the stack of link changes "
			+ "since last feature computation. Is used by some feature computers that offers incremental feature "
			+ "computation.";

	public static final Spec SPEC = new Spec();

	public LinkUpdateStack( final RefCollection< Link > pool )
	{
		super( pool );
	}

	/**
	 * For deserialization only.
	 */
	LinkUpdateStack( final RefCollection< Link > pool, final SizedDeque< UpdateState< Link > > stateStack )
	{
		super( pool, stateStack );
	}

	public static LinkUpdateStack getOrCreate( final FeatureModel featureModel, final RefCollection< Link > pool )
	{
		final LinkUpdateStack retrieved = ( LinkUpdateStack ) featureModel.getFeature( LinkUpdateStack.SPEC );
		if ( null != retrieved )
			return retrieved;

		final LinkUpdateStack feature = new LinkUpdateStack( pool );
		featureModel.declareFeature( feature );
		return feature;
	}

	@Override
	public FeatureSpec< LinkUpdateStack, Link > getSpec()
	{
		return SPEC;
	}

	@Plugin( type = FeatureSpec.class )
	public static class Spec extends FeatureSpec< LinkUpdateStack, Link >
	{
		public Spec()
		{
			super(
					KEY,
					INFO,
					LinkUpdateStack.class,
					Link.class,
					Multiplicity.SINGLE,
					new FeatureProjectionSpec[] {} );
		}
	}
}
