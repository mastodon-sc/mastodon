package org.mastodon.ui.coloring.feature;

import java.util.Collection;
import java.util.DoubleSummaryStatistics;

import org.mastodon.feature.FeatureProjection;

/**
 * A {@link FeatureRangeCalculator} computing statistics over a
 * {@code Collection<O>} of objects of one specific target type {@code O}.
 *
 * @param <O>
 *            target type.
 */
public class DefaultFeatureRangeCalculator< O > implements FeatureRangeCalculator
{
	private final Collection< O > objs;

	private final Projections projections;

	public DefaultFeatureRangeCalculator( final Collection< O > objs, final Projections projections )
	{
		this.objs = objs;
		this.projections = projections;
	}

	@Override
	public double[] computeMinMax( final FeatureProjectionId id )
	{
		if ( objs.isEmpty() )
			return null;

		@SuppressWarnings( "unchecked" )
		final Class< O > target = ( Class< O > ) objs.iterator().next().getClass();

		final FeatureProjection< O > projection = projections.getFeatureProjection( id, target );
		if ( null == projection )
			return null;

		final DoubleSummaryStatistics stats = objs.stream()
				.filter( projection::isSet )
				.mapToDouble( projection::value )
				.summaryStatistics();
		return new double[] { stats.getMin(), stats.getMax() };
	}
}
