package org.mastodon.mamut.feature.branch;

import org.junit.Test;
import org.mastodon.feature.FeatureProjection;
import org.mastodon.mamut.feature.FeatureComputerTestUtils;
import org.mastodon.mamut.feature.branch.exampleGraph.ExampleGraph1;
import org.mastodon.mamut.feature.branch.exampleGraph.ExampleGraph2;
import org.mastodon.mamut.model.branch.BranchSpot;
import org.scijava.Context;

import static org.junit.Assert.assertEquals;

public class BranchNSpotsFeatureComputerTest
{

	@Test
	public void testComputeBranchNSpots()
	{
		try (Context context = new Context())
		{
			ExampleGraph1 exampleGraph1 = new ExampleGraph1();
			final FeatureProjection< BranchSpot > projection = FeatureComputerTestUtils.getFeatureProjection( context, exampleGraph1,
					BranchNSpotsFeature.SPEC, BranchNSpotsFeature.PROJECTION_SPEC );

			assertEquals( 5, projection.value( exampleGraph1.branchSpotA ), 0 );
		}
	}

	@Test
	public void testComputeBranchNSpots2()
	{
		try (Context context = new Context())
		{
			ExampleGraph2 exampleGraph2 = new ExampleGraph2();
			final FeatureProjection< BranchSpot > projection = FeatureComputerTestUtils.getFeatureProjection( context, exampleGraph2,
					BranchNSpotsFeature.SPEC, BranchNSpotsFeature.PROJECTION_SPEC );

			assertEquals( 3, projection.value( exampleGraph2.branchSpotA ), 0 );
			assertEquals( 2, projection.value( exampleGraph2.branchSpotB ), 0 );
			assertEquals( 2, projection.value( exampleGraph2.branchSpotC ), 0 );
			assertEquals( 3, projection.value( exampleGraph2.branchSpotD ), 0 );
			assertEquals( 2, projection.value( exampleGraph2.branchSpotE ), 0 );

		}
	}
}
