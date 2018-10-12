package org.mastodon.revised.mamut;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jdom2.JDOMException;
import org.mastodon.feature.Feature;
import org.mastodon.feature.FeatureModel;
import org.mastodon.feature.FeatureSpec;
import org.mastodon.feature.FeatureSpecsService;
import org.mastodon.mamut.feature.MamutFeatureComputerService;
import org.mastodon.revised.model.mamut.Link;
import org.mastodon.revised.model.mamut.Model;
import org.mastodon.revised.model.mamut.ModelUtils;
import org.mastodon.revised.model.mamut.Spot;
import org.mastodon.revised.model.mamut.trackmate.MamutExporter;
import org.mastodon.revised.model.mamut.trackmate.TrackMateImporter;
import org.scijava.Context;

import mpicbg.spim.data.SpimDataException;

public class MaMuTExportExample
{

	public static void main( final String[] args ) throws IOException, JDOMException, SpimDataException
	{
		/*
		 * 1. Load a regular Mastodon project.
		 */

		final MamutProject project = new MamutProjectIO().load( "samples/mamutproject" );
		final WindowManager windowManager = new WindowManager( new Context() );
		windowManager.projectManager.open( project );
		final Model model = windowManager.getAppModel().getModel();
		final FeatureModel featureModel = model.getFeatureModel();

		/*
		 * 1.1a. Compute all features.
		 */

		final Context context = windowManager.getContext();
		final FeatureSpecsService specsService = context.getService( FeatureSpecsService.class );
		final Collection< String > featureKeys = new ArrayList<>();
		featureKeys.addAll(
				Stream.concat(
						specsService.getSpecs( Spot.class ).stream(),
						specsService.getSpecs( Link.class ).stream() )
						.map( FeatureSpec::getKey )
						.collect( Collectors.toList() ) );

		final MamutFeatureComputerService featureComputerService = context.getService( MamutFeatureComputerService.class );
		featureComputerService.setModel( model );
		featureComputerService.setSharedBdvData( windowManager.getAppModel().getSharedBdvData() );
		System.out.println( "Computing all discovered features: " + featureKeys );
		final Map< FeatureSpec< ?, ? >, Feature< ? > > features = featureComputerService.compute( featureKeys );
		System.out.println( "Done." );


		for ( final FeatureSpec< ?, ? > fs : features.keySet() )
		{
			System.out.println( " - " + fs.getKey() );
			final Feature< ? > feature = features.get( fs );
			for ( final String projectionKey : feature.projectionKeys() )
				System.out.println( "   - " + projectionKey  );
		}


		/*
		 * 1.1b. Pass them to the feature model.
		 */

		featureModel.clear();
		for ( final FeatureSpec< ?, ? > spec: features.keySet() )
			featureModel.declareFeature( spec, features.get( spec ) );

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

		final Model importedModel = new Model(model.getSpaceUnits(), model.getTimeUnits());
		new TrackMateImporter( targetFile ).readModel( importedModel );
		System.out.println();
		System.out.println( "Model AFTER de-serialization:" );
		System.out.println( ModelUtils.dump( importedModel, 10 ) );
	}
}
