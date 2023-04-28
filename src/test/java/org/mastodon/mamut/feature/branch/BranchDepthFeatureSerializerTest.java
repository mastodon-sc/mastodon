package org.mastodon.mamut.feature.branch;

import org.junit.Test;
import org.mastodon.feature.Feature;
import org.mastodon.mamut.feature.FeatureComputerTestUtils;
import org.mastodon.mamut.feature.FeatureSerializerTestUtils;
import org.mastodon.mamut.feature.branch.exampleGraph.ExampleGraph2;
import org.mastodon.mamut.model.branch.BranchSpot;
import org.scijava.Context;

import java.io.IOException;

import static org.junit.Assert.assertTrue;

public class BranchDepthFeatureSerializerTest
{

	@Test
	public void testFeatureSerialization() throws IOException
	{
		try (Context context = new Context())
		{
			ExampleGraph2 exampleGraph2 = new ExampleGraph2();
			Feature< BranchSpot > branchDepthFeature =
					FeatureComputerTestUtils.getFeature( context, exampleGraph2.getModel(), BranchDepthFeature.SPEC );

			BranchDepthFeature branchDepthFeatureReloaded = ( BranchDepthFeature ) FeatureSerializerTestUtils.saveAndReload( context,
					exampleGraph2.getModel(), branchDepthFeature );

			// check that the feature has correct values after saving and reloading
			assertTrue( FeatureSerializerTestUtils.checkFeatureProjectionEquality( branchDepthFeature, branchDepthFeatureReloaded,
					exampleGraph2.branchSpotA ) );
			assertTrue( FeatureSerializerTestUtils.checkFeatureProjectionEquality( branchDepthFeature, branchDepthFeatureReloaded,
					exampleGraph2.branchSpotB ) );
			assertTrue( FeatureSerializerTestUtils.checkFeatureProjectionEquality( branchDepthFeature, branchDepthFeatureReloaded,
					exampleGraph2.branchSpotC ) );
			assertTrue( FeatureSerializerTestUtils.checkFeatureProjectionEquality( branchDepthFeature, branchDepthFeatureReloaded,
					exampleGraph2.branchSpotD ) );
			assertTrue( FeatureSerializerTestUtils.checkFeatureProjectionEquality( branchDepthFeature, branchDepthFeatureReloaded,
					exampleGraph2.branchSpotE ) );
		}
	}
}
