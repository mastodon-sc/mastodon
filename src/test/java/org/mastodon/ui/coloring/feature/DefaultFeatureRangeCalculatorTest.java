/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2025 Tobias Pietzsch, Jean-Yves Tinevez
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

import org.junit.Test;
import org.mastodon.feature.Dimension;
import org.mastodon.feature.Feature;
import org.mastodon.feature.FeatureProjection;
import org.mastodon.feature.FeatureProjectionKey;
import org.mastodon.feature.FeatureProjectionSpec;
import org.mastodon.feature.FeatureProjections;
import org.mastodon.feature.FeatureSpec;
import org.mastodon.feature.Multiplicity;
import org.mastodon.mamut.feature.branch.exampleGraph.ExampleGraph2;
import org.mastodon.mamut.model.Spot;
import org.mastodon.properties.DoublePropertyMap;
import org.scijava.plugin.Plugin;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.Assert.assertEquals;
import static org.mastodon.feature.FeatureProjectionKey.key;

/**
 * Tests for {@link DefaultFeatureRangeCalculator}.
 * The test uses a simple feature that stores a double value for each spot. The feature is declared in the feature model of an example graph.
 * The test checks that the calculator computes the correct min and max values for the feature, ignoring NaN values.
 */
public class DefaultFeatureRangeCalculatorTest
{
	private static final String KEY = "TestDouble";

	@Test
	public void testComputeMinMax()
	{
		ExampleGraph2 exampleGraph2 = new ExampleGraph2();
		double minValue = 0;
		double maxValue = 100;
		TestDoubleFeature testDoubleFeature = new TestDoubleFeature(
				new DoublePropertyMap<>( exampleGraph2.getModel().getGraph().vertices().getRefPool(), Double.NaN ) );
		exampleGraph2.getModel().getFeatureModel().declareFeature( testDoubleFeature );
		testDoubleFeature.doubleValues.set( exampleGraph2.spot0, Double.NaN );
		testDoubleFeature.doubleValues.set( exampleGraph2.spot1, minValue );
		testDoubleFeature.doubleValues.set( exampleGraph2.spot2, maxValue );
		testDoubleFeature.doubleValues.set( exampleGraph2.spot3, ThreadLocalRandom.current().nextDouble( minValue, maxValue ) );
		final Projections projections = new ProjectionsFromFeatureModel( exampleGraph2.getModel().getFeatureModel() );
		FeatureProjectionId projectionId = new FeatureProjectionId( KEY, KEY, TargetType.VERTEX );
		DefaultFeatureRangeCalculator< Spot > calculator =
				new DefaultFeatureRangeCalculator<>( exampleGraph2.getModel().getGraph().vertices(), projections );
		double[] minMax = calculator.computeMinMax( projectionId );

		assertEquals( minValue, minMax[ 0 ], 0 );
		assertEquals( maxValue, minMax[ 1 ], 0 );
	}

	public static class TestDoubleFeature implements Feature< Spot >
	{
		public static final FeatureProjectionSpec PROJECTION_SPEC = new FeatureProjectionSpec( KEY );

		public final DoublePropertyMap< Spot > doubleValues;

		public final FeatureProjection< Spot > projection;

		public static final Spec SPEC = new TestDoubleFeature.Spec();

		@Plugin( type = FeatureSpec.class )
		public static class Spec extends FeatureSpec< TestDoubleFeature, Spot >
		{
			public Spec()
			{
				super(
						KEY,
						null,
						TestDoubleFeature.class,
						Spot.class,
						Multiplicity.SINGLE,
						PROJECTION_SPEC );
			}
		}

		public TestDoubleFeature( final DoublePropertyMap< Spot > map )
		{
			this.doubleValues = map;
			this.projection = FeatureProjections.project( key( PROJECTION_SPEC ), map, Dimension.NONE_UNITS );
		}

		@Override
		public FeatureProjection< Spot > project( final FeatureProjectionKey key )
		{
			return projection.getKey().equals( key ) ? projection : null;
		}

		@Override
		public Set< FeatureProjection< Spot > > projections()
		{
			return Collections.singleton( projection );
		}

		@Override
		public FeatureSpec< ? extends Feature< Spot >, Spot > getSpec()
		{
			return SPEC;
		}

		@Override
		public void invalidate( final Spot obj )
		{
			// Do nothing
		}
	}
}
