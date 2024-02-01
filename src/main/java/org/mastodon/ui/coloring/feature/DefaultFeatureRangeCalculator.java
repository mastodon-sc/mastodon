/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2024 Tobias Pietzsch, Jean-Yves Tinevez
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
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
