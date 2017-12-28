package org.mastodon.revised.mamut;

import java.io.File;
import java.io.IOException;

import org.jdom2.JDOMException;
import org.mastodon.revised.bdv.SharedBigDataViewerData;
import org.mastodon.revised.bdv.overlay.ui.RenderSettingsManager;
import org.mastodon.revised.model.mamut.Model;
import org.mastodon.revised.model.mamut.trackmate.MamutExporter;
import org.mastodon.revised.model.mamut.trackmate.TrackMateImporter;
import org.mastodon.revised.trackscheme.display.style.TrackSchemeStyleManager;
import org.scijava.ui.behaviour.KeyPressedManager;
import org.scijava.ui.behaviour.io.InputTriggerConfig;

import bdv.spimdata.SpimDataMinimal;
import bdv.spimdata.XmlIoSpimDataMinimal;
import bdv.viewer.ViewerOptions;
import mpicbg.spim.data.SpimDataException;

public class MaMuTExportExample
{

	public static void main( final String[] args ) throws IOException, JDOMException, SpimDataException
	{
		/*
		 * 1. Load a regular Mastodon project.
		 */

		final String bdvFile = "samples/datasethdf5.xml";
		final String modelFile = "samples/model_revised.raw";
		final MamutProject project = new MamutProject( new File( "." ), new File( bdvFile ), new File( modelFile ) );
		final Model model = new Model();
		model.loadRaw( project.getRawModelFile() );

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

		final Model trackMateModel = TrackMateImporter.importModel( target ).model;

		/*
		 * 4. Display the re-imported model.
		 */

		final InputTriggerConfig keyconf =Mastodon.getInputTriggerConfig();
		final KeyPressedManager keyPressedManager = new KeyPressedManager();
		final TrackSchemeStyleManager trackSchemeStyleManager = new TrackSchemeStyleManager( false );
		final RenderSettingsManager renderSettingsManager = new RenderSettingsManager( false );
		final ViewerOptions options = ViewerOptions.options()
				.inputTriggerConfig( keyconf )
				.shareKeyPressedEvents( keyPressedManager );
		final SpimDataMinimal spimData = new XmlIoSpimDataMinimal().load( project.getDatasetXmlFile().getAbsolutePath() );
		final SharedBigDataViewerData sharedBdvData = new SharedBigDataViewerData(
				project.getDatasetXmlFile().getAbsolutePath(),
				spimData,
				options,
				() -> System.out.println( "repaint.") );

		final MamutAppModel appModel = new MamutAppModel( trackMateModel, sharedBdvData, keyconf, keyPressedManager, trackSchemeStyleManager, renderSettingsManager );
		new MamutViewBdv( appModel );
		new MamutViewTrackScheme( appModel );


	}

}
