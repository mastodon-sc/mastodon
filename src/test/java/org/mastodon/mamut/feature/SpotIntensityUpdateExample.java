package org.mastodon.mamut.feature;

import java.io.IOException;
import java.util.Map;

import net.imglib2.util.StopWatch;

import org.jdom2.JDOMException;
import org.mastodon.feature.Feature;
import org.mastodon.feature.FeatureModel;
import org.mastodon.feature.FeatureProjection;
import org.mastodon.feature.FeatureSpec;
import org.mastodon.project.MamutProject;
import org.mastodon.project.MamutProjectIO;
import org.mastodon.revised.mamut.WindowManager;
import org.mastodon.revised.model.mamut.Model;
import org.mastodon.revised.model.mamut.Spot;
import org.scijava.Context;

import mpicbg.spim.data.SpimDataException;

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
		final StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		final Map< FeatureSpec< ?, ? >, Feature< ? > > features = featureComputerService.compute(
				SpotGaussFilteredIntensityFeature.SPEC );
		stopWatch.stop();
		System.out.println( String.format( "Done in %.1s.", stopWatch.nanoTime() / 1e9 ) );

		/*
		 * 1.1b. Pass them to the feature model.
		 */

		featureModel.clear();
		features.values().forEach( featureModel::declareFeature );

		@SuppressWarnings( "unchecked" )
		final FeatureProjection< Spot > proj1 = ( FeatureProjection< Spot > ) model.getFeatureModel()
				.getFeature( SpotGaussFilteredIntensityFeature.SPEC ).project( SpotGaussFilteredIntensityFeature.MEAN_PROJECTION_SPEC, 0 );
		@SuppressWarnings( "unchecked" )
		final FeatureProjection< Spot > proj2 = ( FeatureProjection< Spot > ) model.getFeatureModel()
				.getFeature( SpotGaussFilteredIntensityFeature.SPEC ).project( SpotGaussFilteredIntensityFeature.STD_PROJECTION_SPEC, 0 );

		System.out.println();
		System.out.println( "Spot " + spot.getLabel() + " intensity was " + proj1.value( spot ) + " ± " + proj2.value( spot ) );
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
				SpotGaussFilteredIntensityFeature.SPEC );

		featureModel.clear();
		features2.values().forEach( featureModel::declareFeature );

		System.out.println( "Spot " + spot.getLabel() + " intensity is now " + proj1.value( spot ) + " ± " + proj2.value( spot ) );
		System.out.println();

		/*
		 * 4. Trigger full recalculation to compare.
		 */

		System.out.println( "Full recalculation..." );
		featureComputerService.setModel( model );
		final Map< FeatureSpec< ?, ? >, Feature< ? > > features3 = featureComputerService.compute(
				SpotGaussFilteredIntensityFeature.SPEC );

		featureModel.clear();
		features3.values().forEach( featureModel::declareFeature );

		System.out.println( "Spot " + spot.getLabel() + " intensity is to be compared with " + proj1.value( spot ) + " ± " + proj2.value( spot ) );
		System.out.println();

		/*
		 * 4. Re-calculate without changes.
		 */

		System.out.println( "Update without changes..." );
		final Map< FeatureSpec< ?, ? >, Feature< ? > > features4 = featureComputerService.compute(
				SpotGaussFilteredIntensityFeature.SPEC );

		featureModel.clear();
		features4.values().forEach( featureModel::declareFeature );
	}
}
