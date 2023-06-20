/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2022 Tobias Pietzsch, Jean-Yves Tinevez
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
package org.mastodon.mamut.importer;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.jdom2.JDOMException;
import org.mastodon.feature.Feature;
import org.mastodon.feature.FeatureModel;
import org.mastodon.feature.FeatureProjection;
import org.mastodon.feature.FeatureSpec;
import org.mastodon.mamut.WindowManager;
import org.mastodon.mamut.feature.MamutFeatureComputerService;
import org.mastodon.mamut.feature.TrackSizeFeature;
import org.mastodon.mamut.importer.trackmate.MamutExporter;
import org.mastodon.mamut.importer.trackmate.TrackMateImporter;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.ModelUtils;
import org.mastodon.mamut.project.MamutProject;
import org.mastodon.mamut.project.MamutProjectIO;
import org.scijava.Context;

import mpicbg.spim.data.SpimDataException;

public class MaMuTExportExample
{

	public static void main( final String[] args ) throws IOException, JDOMException, SpimDataException
	{
		/*
		 * 1. Load a regular Mastodon project.
		 */

		final MamutProject project = new MamutProjectIO().load( "samples/mamutproject.mastodon" );
		final WindowManager windowManager = new WindowManager( new Context() );
		windowManager.getProjectManager().open( project );
		final Model model = windowManager.getAppModel().getModel();
		final FeatureModel featureModel = model.getFeatureModel();

		/*
		 * 1.1a. Compute all features.
		 */

		final Context context = windowManager.getContext();
		final MamutFeatureComputerService featureComputerService =
				MamutFeatureComputerService.newInstance( context );
		final Collection< FeatureSpec< ?, ? > > featureKeys = featureComputerService.getFeatureSpecs();
		featureComputerService.setModel( model );
		featureComputerService.setSharedBdvData( windowManager.getAppModel().getSharedBdvData() );
		System.out.println( "Computing all discovered features: " + featureKeys );
		final Map< FeatureSpec< ?, ? >, Feature< ? > > features = featureComputerService.compute( featureKeys );
		System.out.println( "Done." );

		for ( final FeatureSpec< ?, ? > fs : features.keySet() )
		{
			System.out.println( " - " + fs.getKey() );
			final Feature< ? > feature = features.get( fs );
			if ( null == feature.projections() )
				continue;
			for ( final FeatureProjection< ? > projection : feature.projections() )
				System.out.println( "   - " + projection.getKey() );
		}

		/*
		 * 1.1b. Pass them to the feature model.
		 */

		featureModel.clear();
		for ( final FeatureSpec< ?, ? > spec : features.keySet() )
			featureModel.declareFeature( features.get( spec ) );

		System.out.println();
		System.out.println( "Model BEFORE serialization:" );
		System.out.println( ModelUtils.dump( model, 10 ) );
		System.out.println();

		/*
		 * 2. Export it to a MaMuT file.
		 *
		 * This will export also setup assignments and bookmarks, as well as
		 * feature values when possible. Of course, we loose the ellipsoid
		 * information, and the MaMuT spots have a radius equal to the mean of
		 * the ellipsoid semi-axes.
		 */

		final File targetFile = new File( "samples/mamutExport.xml" );
		MamutExporter.export( targetFile, model, project );

		/*
		 * 3. Re-import it using the TrackMate importer.
		 */

		Model importedModel = new Model( model.getSpaceUnits(), model.getTimeUnits() );
		new TrackMateImporter( targetFile ).readModel( importedModel );
		System.out.println();
		System.out.println( "Model AFTER de-serialization:" );
		System.out.println( ModelUtils.dump( importedModel, 10 ) );

		/*
		 * Test for name clash: recompute a feature that we already imported,
		 * and try to re-export both.
		 */

		featureComputerService.setModel( importedModel );
		featureComputerService.setSharedBdvData( windowManager.getAppModel().getSharedBdvData() );
		System.out.println( "Computing feature: " + TrackSizeFeature.SPEC );
		final Map< FeatureSpec< ?, ? >, Feature< ? > > features2 =
				featureComputerService.compute( Collections.singleton( TrackSizeFeature.SPEC ) );
		System.out.println( "Done." );
		for ( final FeatureSpec< ?, ? > spec : features2.keySet() )
			importedModel.getFeatureModel().declareFeature( features.get( spec ) );

		System.out.println();
		System.out.println( "Model BEFORE serialization:" );
		System.out.println( ModelUtils.dump( importedModel, 10 ) );
		System.out.println();

		MamutExporter.export( targetFile, importedModel, project );
		importedModel = new Model( model.getSpaceUnits(), model.getTimeUnits() );
		new TrackMateImporter( targetFile ).readModel( importedModel );
		System.out.println();
		System.out.println( "Model AFTER de-serialization EXTRA:" );
		System.out.println( ModelUtils.dump( importedModel, 10 ) );
	}
}
