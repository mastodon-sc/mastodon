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
package org.mastodon.mamut.io;

import mpicbg.spim.data.SpimDataException;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Cast;
import org.junit.Ignore;
import org.junit.Test;
import org.mastodon.feature.FeatureProjection;
import org.mastodon.feature.FeatureProjectionKey;
import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.ProjectModelTestUtils;
import org.mastodon.mamut.feature.FeatureComputerTestUtils;
import org.mastodon.mamut.feature.MamutFeatureComputerService;
import org.mastodon.mamut.feature.branch.BranchDisplacementDurationFeature;
import org.mastodon.mamut.feature.branch.exampleGraph.ExampleGraph1;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.branch.BranchSpot;
import org.scijava.Context;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * This unit test can be used to test if the ProjectLoader class can properly load and close a project file multiple times without causing memory leaks.
 */
public class ProjectLoaderTest
{
	@Ignore( "The run time of this test is too long for a unit test that is run on every build." )
	@Test
	public void testLoadAndCloseProjectGarbageCollection() throws IOException, SpimDataException
	{
		Model model = new Model();
		Img< FloatType > image = ArrayImgs.floats( 1, 1, 1 );
		File mastodonFile = File.createTempFile( "test", ".mastodon" );
		try (Context context = new Context())
		{
			ProjectModel appModel = ProjectModelTestUtils.wrapAsAppModel( image, model, context, mastodonFile );
			ProjectSaver.saveProject( mastodonFile, appModel );
		}
		for ( int i = 0; i < 100; i++ )
			loadAndCloseProjectModel( mastodonFile );
		assertTrue( true );
	}

	@Test
	public void testBranchFeaturesAfterSaveAndReload() throws IOException, SpimDataException
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
			FeatureProjection< BranchSpot > durationProjection = FeatureComputerTestUtils.getFeatureProjection( context, model,
					BranchDisplacementDurationFeature.SPEC, BranchDisplacementDurationFeature.DURATION_PROJECTION_SPEC );
			double durationBeforeSave = durationProjection.value( graph.branchSpotA );
			ProjectModel reloadedProjectModel = saveAndReloadProject( projectModel, mastodonFile, context );
			FeatureProjection< BranchSpot > reloadedDurationProjection = getDurationProjectionFromModel( reloadedProjectModel );
			BranchSpot branchSpot = reloadedProjectModel.getModel().getBranchGraph().vertices().iterator().next(); // NB: the model only has one branch spot
			double durationAfterSave = reloadedDurationProjection.value( branchSpot );
			assertEquals( durationBeforeSave, durationAfterSave, 0 );
		}
	}

	private void loadAndCloseProjectModel( final File mastodonFile ) throws SpimDataException, IOException
	{
		try (Context context = new Context())
		{
			ProjectModel projectModel = ProjectLoader.open( mastodonFile.getAbsolutePath(), context, false, true );
			projectModel.close();
		}
	}

	private static FeatureProjection< BranchSpot > getDurationProjectionFromModel( final ProjectModel reloadedProjectModel )
	{
		BranchDisplacementDurationFeature reloadedFeature = Cast
				.unchecked( reloadedProjectModel.getModel().getFeatureModel().getFeature( BranchDisplacementDurationFeature.SPEC ) );
		return reloadedFeature.project( FeatureProjectionKey.key( BranchDisplacementDurationFeature.DURATION_PROJECTION_SPEC ) );
	}

	private static ProjectModel saveAndReloadProject( final ProjectModel projectModel, final File mastodonFile, final Context context )
			throws IOException, SpimDataException
	{
		ProjectSaver.saveProject( mastodonFile, projectModel );
		return ProjectLoader.open( mastodonFile.getAbsolutePath(), context, false, true );
	}
}
