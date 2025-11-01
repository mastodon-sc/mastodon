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
package org.mastodon.mamut.model.branch;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import org.junit.Ignore;
import org.junit.Test;
import org.mastodon.feature.FeatureProjection;
import org.mastodon.mamut.MamutAppModel;
import org.mastodon.mamut.ProjectModelTestUtils;
import org.mastodon.mamut.feature.FeatureComputerTestUtils;
import org.mastodon.mamut.feature.MamutFeatureComputerService;
import org.mastodon.mamut.feature.branch.BranchDisplacementDurationFeature;
import org.mastodon.mamut.feature.branch.exampleGraph.ExampleGraph1;
import org.mastodon.mamut.model.Model;
import org.scijava.Context;

import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.real.FloatType;

public class BranchGraphSynchronizerTest
{

	@Test
	public void testKeepBranchFeaturesAfterSyncWithoutChanges() throws IOException
	{
		final ExampleGraph1 graph = new ExampleGraph1();
		final Model model = graph.getModel();
		try (Context context = new Context())
		{
			final File mastodonFile = File.createTempFile( "test", ".mastodon" );
			final Img< FloatType > image = ArrayImgs.floats( 1, 1, 1 );
			final MamutAppModel projectModel = ProjectModelTestUtils.wrapAsAppModel( image, model, context, mastodonFile );
			final MamutFeatureComputerService computerService = MamutFeatureComputerService.newInstance( context );
			computerService.setModel( model );
			final FeatureProjection< BranchSpot > durationProjection =
					FeatureComputerTestUtils.getFeatureProjection( context, model, BranchDisplacementDurationFeature.SPEC,
							BranchDisplacementDurationFeature.DURATION_PROJECTION_SPEC );
			final BranchGraphSynchronizer branchGraphSynchronizer = projectModel.dataModel().branchGraphSync();
			final double durationBeforeSync = durationProjection.value( graph.branchSpotA );
			branchGraphSynchronizer.sync();
			final double durationAfterSync = durationProjection.value( graph.branchSpotA );
			assertEquals( durationBeforeSync, durationAfterSync, 0 );
		}
	}

	@Ignore( "This is a known issue. The test is ignored until the issue is fixed." )
	@Test
	public void testKeepBranchFeaturesAfterSyncWithChanges() throws IOException
	{
		final ExampleGraph1 graph = new ExampleGraph1();
		final Model model = graph.getModel();
		try (Context context = new Context())
		{
			final File mastodonFile = File.createTempFile( "test", ".mastodon" );
			final Img< FloatType > image = ArrayImgs.floats( 1, 1, 1 );
			final MamutAppModel projectModel = ProjectModelTestUtils.wrapAsAppModel( image, model, context, mastodonFile );
			final MamutFeatureComputerService computerService = MamutFeatureComputerService.newInstance( context );
			computerService.setModel( model );
			final FeatureProjection< BranchSpot > durationProjection =
					FeatureComputerTestUtils.getFeatureProjection( context, model, BranchDisplacementDurationFeature.SPEC,
							BranchDisplacementDurationFeature.DURATION_PROJECTION_SPEC );
			final BranchGraphSynchronizer branchGraphSynchronizer = projectModel.dataModel().branchGraphSync();
			final double durationBeforeSync = durationProjection.value( graph.branchSpotA );
			model.getGraph().addVertex().init( 0, new double[] { 0, 0, 0 }, 1 );
			model.getGraph().notifyGraphChanged();
			branchGraphSynchronizer.sync();
			final double durationAfterSync = durationProjection.value( graph.branchSpotA );
			assertEquals( durationBeforeSync, durationAfterSync, 0 );
		}
	}
}
