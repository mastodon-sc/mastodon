package org.mastodon.mamut.model.branch;

import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.real.FloatType;
import org.junit.Ignore;
import org.junit.Test;
import org.mastodon.feature.FeatureProjection;
import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.ProjectModelTestUtils;
import org.mastodon.mamut.feature.FeatureComputerTestUtils;
import org.mastodon.mamut.feature.MamutFeatureComputerService;
import org.mastodon.mamut.feature.branch.BranchDisplacementDurationFeature;
import org.mastodon.mamut.feature.branch.exampleGraph.ExampleGraph1;
import org.mastodon.mamut.model.Model;
import org.scijava.Context;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class BranchGraphSynchronizerTest
{

	@Test
	public void testKeepBranchFeaturesAfterSyncWithoutChanges() throws IOException
	{
		ExampleGraph1 graph = new ExampleGraph1();
		Model model = graph.getModel();
		try (Context context = new Context())
		{
			File mastodonFile = File.createTempFile( "test", ".mastodon" );
			Img< FloatType > image = ArrayImgs.floats( 1, 1, 1 );
			ProjectModel projectModel = ProjectModelTestUtils.wrapAsAppModel( image, model, context, mastodonFile );
			final MamutFeatureComputerService computerService = MamutFeatureComputerService.newInstance( context );
			computerService.setModel( model );
			FeatureProjection< BranchSpot > durationProjection =
					FeatureComputerTestUtils.getFeatureProjection( context, model, BranchDisplacementDurationFeature.SPEC,
							BranchDisplacementDurationFeature.DURATION_PROJECTION_SPEC );
			BranchGraphSynchronizer branchGraphSynchronizer = projectModel.getBranchGraphSync();
			double durationBeforeSync = durationProjection.value( graph.branchSpotA );
			branchGraphSynchronizer.sync();
			double durationAfterSync = durationProjection.value( graph.branchSpotA );
			assertEquals( durationBeforeSync, durationAfterSync, 0 );
		}
	}

	@Ignore( "This is a known issue. The test is ignored until the issue is fixed." )
	@Test
	public void testKeepBranchFeaturesAfterSyncWithChanges() throws IOException
	{
		ExampleGraph1 graph = new ExampleGraph1();
		Model model = graph.getModel();
		try (Context context = new Context())
		{
			File mastodonFile = File.createTempFile( "test", ".mastodon" );
			Img< FloatType > image = ArrayImgs.floats( 1, 1, 1 );
			ProjectModel projectModel = ProjectModelTestUtils.wrapAsAppModel( image, model, context, mastodonFile );
			final MamutFeatureComputerService computerService = MamutFeatureComputerService.newInstance( context );
			computerService.setModel( model );
			FeatureProjection< BranchSpot > durationProjection =
					FeatureComputerTestUtils.getFeatureProjection( context, model, BranchDisplacementDurationFeature.SPEC,
							BranchDisplacementDurationFeature.DURATION_PROJECTION_SPEC );
			BranchGraphSynchronizer branchGraphSynchronizer = projectModel.getBranchGraphSync();
			double durationBeforeSync = durationProjection.value( graph.branchSpotA );
			model.getGraph().addVertex().init( 0, new double[] { 0, 0, 0 }, 1 );
			model.getGraph().notifyGraphChanged();
			branchGraphSynchronizer.sync();
			double durationAfterSync = durationProjection.value( graph.branchSpotA );
			assertEquals( durationBeforeSync, durationAfterSync, 0 );
		}
	}
}
