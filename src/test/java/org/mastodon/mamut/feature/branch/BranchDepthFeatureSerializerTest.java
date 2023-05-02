package org.mastodon.mamut.feature.branch;

import org.junit.Test;
import org.mastodon.feature.Feature;
import org.mastodon.mamut.feature.FeatureComputerTestUtils;
import org.mastodon.mamut.feature.FeatureSerializerTestUtils;
import org.mastodon.mamut.feature.branch.exampleGraph.ExampleGraph2;
import org.mastodon.mamut.model.branch.BranchSpot;
import org.scijava.Context;

import java.io.IOException;
import java.util.Collection;

import static org.junit.Assert.assertTrue;

public class BranchDepthFeatureSerializerTest
{

	@Test
	public void testFeatureSerialization() throws IOException
	{
		try (Context context = new Context())
		{
			ExampleGraph2 exampleGraph2 = new ExampleGraph2();

			Feature< BranchSpot > feature = FeatureComputerTestUtils.getFeature( context, exampleGraph2.getModel(), BranchDepthFeature.SPEC );
			Feature< BranchSpot > featureReloaded = FeatureSerializerTestUtils.saveAndReload( context, exampleGraph2.getModel(), feature );

			Collection< BranchSpot > branchSpots = exampleGraph2.getModel().getBranchGraph().vertices();
			assertTrue( FeatureSerializerTestUtils.checkFeatureProjectionEquality( feature, featureReloaded, branchSpots ) );
		}
	}
}
