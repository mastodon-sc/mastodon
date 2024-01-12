/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2023 Tobias Pietzsch, Jean-Yves Tinevez
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
package org.mastodon.mamut.feature.branch;

import org.junit.Test;
import org.mastodon.feature.FeatureProjection;
import org.mastodon.mamut.feature.FeatureComputerTestUtils;
import org.mastodon.mamut.feature.branch.exampleGraph.ExampleGraph1;
import org.mastodon.mamut.feature.branch.exampleGraph.ExampleGraph2;
import org.mastodon.mamut.model.branch.BranchSpot;
import org.scijava.Context;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class BranchDisplacementDurationFeatureComputerTest
{
	@Test
	public void testComputeDuration1()
	{
		try (Context context = new Context())
		{
			ExampleGraph1 exampleGraph1 = new ExampleGraph1();

			FeatureProjection< BranchSpot > featureProjection = FeatureComputerTestUtils.getFeatureProjection( context,
					exampleGraph1.getModel(),
					BranchDisplacementDurationFeature.SPEC,
					BranchDisplacementDurationFeature.DURATION_PROJECTION_SPEC );

			assertEquals( 3, featureProjection.value( exampleGraph1.branchSpotA ), 0 );
		}
	}

	@Test
	public void testComputeDuration2()
	{
		try (Context context = new Context())
		{
			ExampleGraph2 exampleGraph2 = new ExampleGraph2();

			FeatureProjection< BranchSpot > featureProjection = FeatureComputerTestUtils.getFeatureProjection( context,
					exampleGraph2.getModel(),
					BranchDisplacementDurationFeature.SPEC,
					BranchDisplacementDurationFeature.DURATION_PROJECTION_SPEC );

			assertNotNull( featureProjection );
			assertEquals( 2, featureProjection.value( exampleGraph2.branchSpotA ), 0 );
			assertEquals( 2, featureProjection.value( exampleGraph2.branchSpotB ), 0 );
			assertEquals( 3, featureProjection.value( exampleGraph2.branchSpotC ), 0 );
			assertEquals( 3, featureProjection.value( exampleGraph2.branchSpotD ), 0 );
			assertEquals( 3, featureProjection.value( exampleGraph2.branchSpotE ), 0 );
		}
	}

	@Test
	public void testComputeDisplacement1()
	{
		try (Context context = new Context())
		{
			ExampleGraph1 exampleGraph1 = new ExampleGraph1();

			FeatureProjection< BranchSpot > featureProjection = FeatureComputerTestUtils.getFeatureProjection( context,
					exampleGraph1.getModel(),
					BranchDisplacementDurationFeature.SPEC,
					BranchDisplacementDurationFeature.DISPLACEMENT_PROJECTION_SPEC );

			assertEquals( Math.sqrt( 16 + 64 + 144 ), featureProjection.value( exampleGraph1.branchSpotA ), 0 );
		}
	}

	@Test
	public void testComputeDisplacement2()
	{
		try (Context context = new Context())
		{
			ExampleGraph2 exampleGraph2 = new ExampleGraph2();

			FeatureProjection< BranchSpot > featureProjection = FeatureComputerTestUtils.getFeatureProjection( context,
					exampleGraph2.getModel(),
					BranchDisplacementDurationFeature.SPEC,
					BranchDisplacementDurationFeature.DISPLACEMENT_PROJECTION_SPEC );

			assertEquals( Math.sqrt( 4 + 16 + 36 ), featureProjection.value( exampleGraph2.branchSpotA ), 0d );
			assertEquals( Math.sqrt( 4 + 16 + 36 ), featureProjection.value( exampleGraph2.branchSpotB ), 0d );
			assertEquals( Math.sqrt( 121 + 484 + 1089 ), featureProjection.value( exampleGraph2.branchSpotC ), 0d );
			assertEquals( Math.sqrt( 9 + 36 + 81 ), featureProjection.value( exampleGraph2.branchSpotD ), 0 );
			assertEquals( Math.sqrt( 36 + 144 + 324 ), featureProjection.value( exampleGraph2.branchSpotE ), 0 );
		}
	}
}
