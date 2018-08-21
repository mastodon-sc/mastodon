package org.mastodon.revised.mamut;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.jdom2.JDOMException;
import org.mastodon.project.MamutProject;
import org.mastodon.project.MamutProjectIO;
import org.mastodon.revised.mamut.feature.MamutFeatureComputerService;
import org.mastodon.revised.model.feature.FeatureComputer;
import org.mastodon.revised.model.mamut.ModelUtils;
import org.mastodon.revised.model.mamut.Model;
import org.mastodon.revised.model.mamut.trackmate.MamutExporter;
import org.mastodon.revised.model.mamut.trackmate.TrackMateImporter;
import org.mastodon.revised.ui.ProgressListener;
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
		final Model model = new Model();
		model.loadRaw( project.openForReading() );

		/*
		 * 1.1. Compute features.
		 */

		final Context context = new Context( MamutFeatureComputerService.class );
		final MamutFeatureComputerService featureComputerService = context.getService( MamutFeatureComputerService.class );
		final Set< FeatureComputer< Model > > featureComputers = new HashSet<>( featureComputerService.getFeatureComputers() );
		final ProgressListener pl = new ProgressListener()
		{

			@Override
			public void showStatus( final String string )
			{
				System.out.println( " - " + string );
			}

			@Override
			public void showProgress( final int current, final int total )
			{}

			@Override
			public void clearStatus()
			{}
		};
		System.out.println( "Computing all features." );
		final boolean computed = featureComputerService.compute( model, model.getFeatureModel(), featureComputers, pl );
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
