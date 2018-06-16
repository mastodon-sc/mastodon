package org.mastodon.revised.mamut;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.jdom2.JDOMException;
import org.mastodon.revised.mamut.feature.MamutFeatureComputerService;
import org.mastodon.revised.model.feature.FeatureComputer;
import org.mastodon.revised.model.mamut.Model;
import org.mastodon.revised.model.mamut.ModelUtils;
import org.mastodon.revised.model.mamut.trackmate.MamutExporter;
import org.mastodon.revised.model.mamut.trackmate.TrackMateImporter;
import org.scijava.Context;
import org.scijava.prefs.PrefService;

import mpicbg.spim.data.SpimDataException;

public class MaMuTExportExample
{

	public static void main( final String[] args ) throws IOException, JDOMException, SpimDataException
	{
		/*
		 * 1. Load a regular Mastodon project.
		 */

		final String projectFolder = "samples/mamutproject";
		final String bdvFile = "samples/mamutproject/datasethdf5.xml";
		final MamutProject project = new MamutProject( new File( projectFolder ), new File( bdvFile ) );
//		final MamutProject project = new MamutProjectIO().load( "samples/mamutproject" );
		final Model model = new Model();
		model.loadRaw( project );

		/*
		 * 1.1. Compute features.
		 */

		// Tune context to use command-liner logger.
		final Context context = new Context(
				SysOutMastodonLogger.class,
				MamutFeatureComputerService.class,
				PrefService.class );
		final MamutFeatureComputerService featureComputerService = context.getService( MamutFeatureComputerService.class );
		final Set< FeatureComputer< Model > > featureComputers = new HashSet<>( featureComputerService.getFeatureComputers() );
		System.out.println( "Computing all features." );
		final boolean computed = featureComputerService.compute( model, model.getFeatureModel(), featureComputers );
		if (!computed)
		{
			System.err.println( "Error while calculating model features." );
			return;
		}

		/*
		 * 2. Export it to a MaMuT file.
		 *
		 * This will export also setup assignments and bookmarks, as well as
		 * feature values when possible. Of course, we loose the ellipsoid
		 * information, and the MaMuT spots have a radius equal to the mean of
		 * the ellipsoid semi-axes.
		 */

		final File target = new File( "samples/mamutExport.xml" );
		MamutExporter.export( target, model, project );

		/*
		 * 3. Re-import it using the TrackMate importer.
		 */

		final Model importedModel = new Model();
		new TrackMateImporter( target ).readModel( importedModel );
		System.out.println( ModelUtils.dump( importedModel ) );
	}

}
