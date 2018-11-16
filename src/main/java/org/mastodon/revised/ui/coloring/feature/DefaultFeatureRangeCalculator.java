package org.mastodon.revised.ui.coloring.feature;

import java.util.Collection;
import java.util.DoubleSummaryStatistics;

import org.mastodon.feature.FeatureModel;
import org.mastodon.feature.FeatureModelUtil;
import org.mastodon.feature.FeatureProjection;
import org.mastodon.feature.FeatureSpec;

public class DefaultFeatureRangeCalculator< O > implements FeatureRangeCalculator
{

	private final FeatureModel featureModel;

	private final Collection< O > objs;

	public DefaultFeatureRangeCalculator( final Collection< O > objs, final FeatureModel featureModel )
	{
		this.objs = objs;
		this.featureModel = featureModel;
	}

	@Override
	public double[] computeMinMax( final FeatureSpec< ?, ? > featureSpec, final String projectionKey )
	{
		final FeatureProjection< ? > projection = FeatureModelUtil.getFeatureProjection( featureModel, featureSpec, FeatureModelUtil.parseProjectionName( projectionKey ) );
		if ( null == projection )
			return null;

		if ( objs.isEmpty() )
			return null;

		if ( !featureSpec.getTargetClass().isAssignableFrom( objs.iterator().next().getClass() ) )
			return null;

		@SuppressWarnings( "unchecked" )
		final FeatureProjection< O > fp = ( FeatureProjection< O > ) projection;
		final DoubleSummaryStatistics stats = objs.stream()
				.filter( e -> fp.isSet( e ) )
				.mapToDouble( e -> fp.value( e ) )
				.summaryStatistics();
		return new double[] { stats.getMin(), stats.getMax() };
	}
}
