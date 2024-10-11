package org.mastodon.views.trackscheme;

import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.real.FloatType;
import org.junit.Test;
import org.mastodon.feature.FeatureProjection;
import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.ProjectModelTestUtils;
import org.mastodon.mamut.feature.FeatureComputerTestUtils;
import org.mastodon.mamut.feature.MamutFeatureComputerService;
import org.mastodon.mamut.feature.branch.BranchDisplacementDurationFeature;
import org.mastodon.mamut.feature.branch.exampleGraph.ExampleGraph1;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.branch.BranchSpot;
import org.mastodon.mamut.views.trackscheme.MamutBranchViewTrackScheme;
import org.mastodon.mamut.views.trackscheme.MamutBranchViewTrackSchemeHierarchy;
import org.scijava.Context;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class MamutBranchViewTrackSchemeTest
{
	@Test
	public void testBranchFeaturesAfterOpeningBranchView() throws IOException, InterruptedException
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
			double duration = durationProjection.value( graph.branchSpotA );
			new MamutBranchViewTrackScheme( projectModel );
			Thread.sleep( 1_000 );
			double durationAfterCreatingTrackSchemeBranch = durationProjection.value( graph.branchSpotA );
			assertEquals( duration, durationAfterCreatingTrackSchemeBranch, 0 );
			new MamutBranchViewTrackSchemeHierarchy( projectModel );
			Thread.sleep( 1_000 );
			double durationAfterCreatingTrackSchemeHierarchyBranch = durationProjection.value( graph.branchSpotA );
			assertEquals( duration, durationAfterCreatingTrackSchemeHierarchyBranch, 0 );
		}
	}
}
