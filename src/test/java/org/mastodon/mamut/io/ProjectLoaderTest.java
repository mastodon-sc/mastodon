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
package org.mastodon.mamut.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.Ignore;
import org.junit.Test;
import org.mastodon.feature.FeatureProjection;
import org.mastodon.feature.FeatureProjectionKey;
import org.mastodon.mamut.MamutAppModel;
import org.mastodon.mamut.ProjectModelTestUtils;
import org.mastodon.mamut.feature.FeatureComputerTestUtils;
import org.mastodon.mamut.feature.MamutFeatureComputerService;
import org.mastodon.mamut.feature.branch.BranchDisplacementDurationFeature;
import org.mastodon.mamut.feature.branch.exampleGraph.ExampleGraph1;
import org.mastodon.mamut.feature.branch.exampleGraph.ExampleGraph2;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.branch.BranchSpot;
import org.scijava.Context;

import mpicbg.spim.data.SpimDataException;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Cast;

/**
 * This unit test can be used to test if the ProjectLoader class can properly load and close a project file multiple times without causing memory leaks.
 */
public class ProjectLoaderTest
{
	@Ignore( "The run time of this test is too long for a unit test that is run on every build." )
	@Test
	public void testLoadAndCloseProjectGarbageCollection() throws IOException, SpimDataException
	{
		final Model model = new Model();
		final Img< FloatType > image = ArrayImgs.floats( 1, 1, 1 );
		final File mastodonFile = File.createTempFile( "test", ".mastodon" );
		try (Context context = new Context())
		{
			final MamutAppModel appModel = ProjectModelTestUtils.wrapAsAppModel( image, model, context, mastodonFile );
			ProjectSaver.saveProject( mastodonFile, appModel );
		}
		for ( int i = 0; i < 100; i++ )
			loadAndCloseProjectModel( mastodonFile );
		assertTrue( true );
	}

	@Test
	public void testBranchFeaturesAfterSaveAndReload() throws IOException, SpimDataException
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
			final FeatureProjection< BranchSpot > durationProjection = FeatureComputerTestUtils.getFeatureProjection( context, model,
					BranchDisplacementDurationFeature.SPEC, BranchDisplacementDurationFeature.DURATION_PROJECTION_SPEC );
			final double durationBeforeSave = durationProjection.value( graph.branchSpotA );
			final MamutAppModel reloadedProjectModel = saveAndReloadProject( projectModel, mastodonFile, context );
			final FeatureProjection< BranchSpot > reloadedDurationProjection = getDurationProjectionFromModel( reloadedProjectModel );
			final BranchSpot branchSpot = reloadedProjectModel.dataModel().branchModel().getGraph().vertices().iterator().next();
			// NB: the model only has one branch
																															// spot
			final double durationAfterSave = reloadedDurationProjection.value( branchSpot );
			assertEquals( durationBeforeSave, durationAfterSave, 0 );
		}
	}

	@Test
	public void testOpenProjectAfterDeletingPoints() throws IOException, SpimDataException
	{
		final ExampleGraph2 exampleGraph2 = new ExampleGraph2();
		final Model model = exampleGraph2.getModel();
		try (Context context = new Context())
		{
			final File mastodonFile = File.createTempFile( "test", ".mastodon" );
			final Img< FloatType > image = ArrayImgs.floats( 1, 1, 1 );
			final MamutAppModel projectModel = ProjectModelTestUtils.wrapAsAppModel( image, model, context, mastodonFile );
			final MamutFeatureComputerService computerService = MamutFeatureComputerService.newInstance( context );
			computerService.setModel( model );
			FeatureComputerTestUtils.getFeatureProjection( context, model, BranchDisplacementDurationFeature.SPEC, // NB: Implicitly creates and computes the feature
					BranchDisplacementDurationFeature.DURATION_PROJECTION_SPEC );
			final ModelGraph graph = projectModel.dataModel().getGraph();
			graph.remove( exampleGraph2.spot7 );
			graph.notifyGraphChanged();
			final MamutAppModel reloadedProjectModel = saveAndReloadProject( projectModel, mastodonFile, context );
			assertNotNull( reloadedProjectModel );
		}
	}

	private void loadAndCloseProjectModel( final File mastodonFile ) throws SpimDataException, IOException
	{
		try (Context context = new Context())
		{
			final MamutAppModel projectModel = ProjectLoader2.open( mastodonFile.getAbsolutePath(), context, false, true );
			projectModel.close();
		}
	}

	private static FeatureProjection< BranchSpot > getDurationProjectionFromModel( final MamutAppModel reloadedProjectModel )
	{
		final BranchDisplacementDurationFeature reloadedFeature = Cast
				.unchecked( reloadedProjectModel.dataModel().getFeatureModel().getFeature( BranchDisplacementDurationFeature.SPEC ) );
		return reloadedFeature.project( FeatureProjectionKey.key( BranchDisplacementDurationFeature.DURATION_PROJECTION_SPEC ) );
	}

	private static MamutAppModel saveAndReloadProject( final MamutAppModel projectModel, final File mastodonFile, final Context context )
			throws IOException, SpimDataException
	{
		ProjectSaver.saveProject( mastodonFile, projectModel );
		return ProjectLoader2.open( mastodonFile.getAbsolutePath(), context, false, true );
	}
}
