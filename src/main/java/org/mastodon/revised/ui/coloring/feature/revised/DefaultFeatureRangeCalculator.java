package org.mastodon.revised.ui.coloring.feature.revised;

import java.util.Collection;
import java.util.DoubleSummaryStatistics;
import org.mastodon.feature.FeatureProjection;

public class DefaultFeatureRangeCalculator< O > implements FeatureRangeCalculator
{
	private final Projections< O > projections;

	private final Collection< O > objs;

	public DefaultFeatureRangeCalculator( final Collection< O > objs, final Projections< O > projections )
	{
		this.objs = objs;
		this.projections = projections;
	}

	@Override
	public double[] computeMinMax( final FeatureProjectionId id )
	{
		if ( objs.isEmpty() )
			return null;

		final FeatureProjection< O > projection = projections.getFeatureProjection( id );
		if ( null == projection )
			return null;

		final DoubleSummaryStatistics stats = objs.stream()
				.filter( projection::isSet )
				.mapToDouble( projection::value )
				.summaryStatistics();
		return new double[] { stats.getMin(), stats.getMax() };
	}
}
