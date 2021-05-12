/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2021 Tobias Pietzsch, Jean-Yves Tinevez
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
package org.mastodon.mamut.feature;

import static org.mastodon.feature.FeatureProjectionKey.key;

import java.io.IOException;
import java.util.Map;

import org.jdom2.JDOMException;
import org.mastodon.feature.Feature;
import org.mastodon.feature.FeatureModel;
import org.mastodon.feature.FeatureProjection;
import org.mastodon.feature.FeatureSpec;
import org.mastodon.mamut.WindowManager;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.project.MamutProject;
import org.mastodon.mamut.project.MamutProjectIO;
import org.scijava.Context;

import mpicbg.spim.data.SpimDataException;
import net.imglib2.util.StopWatch;

public class SpotIntensityUpdateExample
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

		// Keep a spot for later
		final Spot spot = model.getGraph().vertices().iterator().next();

		/*
		 * 1.1a. Compute spot intensity feature for all.
		 */

		final Context context = windowManager.getContext();
		final MamutFeatureComputerService featureComputerService = context.getService( MamutFeatureComputerService.class );
		featureComputerService.setModel( model );
		featureComputerService.setSharedBdvData( windowManager.getAppModel().getSharedBdvData() );
		System.out.println( "Computing spot intensity..." );
		final StopWatch stopWatch = StopWatch.createAndStart();
		final Map< FeatureSpec< ?, ? >, Feature< ? > > features = featureComputerService.compute(
				SpotCenterIntensityFeature.SPEC );
		stopWatch.stop();
		System.out.println( String.format( "Done in %.1s.", stopWatch.nanoTime() / 1e9 ) );

		/*
		 * 1.1b. Pass them to the feature model.
		 */

		featureModel.clear();
		features.values().forEach( featureModel::declareFeature );

		@SuppressWarnings( "unchecked" )
		final FeatureProjection< Spot > proj1 = ( FeatureProjection< Spot > ) model.getFeatureModel()
				.getFeature( SpotCenterIntensityFeature.SPEC ).project( key( SpotCenterIntensityFeature.PROJECTION_SPEC, 0 ) );

		System.out.println();
		System.out.println( "Spot " + spot.getLabel() + " center intensity was " + proj1.value( spot ) );
		System.out.println();

		/*
		 * 2. Modify a spot.
		 */

		spot.move( 10., 0 );
		System.out.println( "Moved spot " + spot.getLabel() );

		/*
		 * 3. Re-calculate.
		 */

		System.out.println( "Re-computing spot intensity..." );
		final Map< FeatureSpec< ?, ? >, Feature< ? > > features2 = featureComputerService.compute(
				SpotCenterIntensityFeature.SPEC );

		featureModel.clear();
		features2.values().forEach( featureModel::declareFeature );

		System.out.println( "Spot " + spot.getLabel() + " center intensity is now " + proj1.value( spot ) );
		System.out.println();

		/*
		 * 4. Trigger full recalculation to compare.
		 */

		System.out.println( "Full recalculation..." );
		featureComputerService.setModel( model );
		final Map< FeatureSpec< ?, ? >, Feature< ? > > features3 = featureComputerService.compute(
				SpotCenterIntensityFeature.SPEC );

		featureModel.clear();
		features3.values().forEach( featureModel::declareFeature );

		System.out.println( "Spot " + spot.getLabel() + " center intensity is to be compared with " + proj1.value( spot ) );
		System.out.println();

		/*
		 * 4. Re-calculate without changes.
		 */

		System.out.println( "Update without changes..." );
		final Map< FeatureSpec< ?, ? >, Feature< ? > > features4 = featureComputerService.compute(
				SpotCenterIntensityFeature.SPEC );

		featureModel.clear();
		features4.values().forEach( featureModel::declareFeature );
	}
}
