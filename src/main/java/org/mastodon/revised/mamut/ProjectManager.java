package org.mastodon.revised.mamut;

import bdv.spimdata.SpimDataMinimal;
import bdv.spimdata.XmlIoSpimDataMinimal;
import bdv.viewer.ViewerOptions;
import java.awt.Component;
import java.awt.Dialog;
import java.io.File;
import java.io.IOException;
import javax.swing.JFrame;
import mpicbg.spim.data.SpimDataException;
import org.mastodon.revised.bdv.SharedBigDataViewerData;
import org.mastodon.revised.mamut.feature.MamutFeatureComputerService;
import org.mastodon.revised.model.mamut.Model;
import org.mastodon.revised.ui.util.FileChooser;
import org.mastodon.revised.ui.util.XmlFileFilter;
import org.mastodon.revised.util.ToggleDialogAction;
import org.scijava.Context;
import org.scijava.ui.behaviour.KeyPressedManager;
import org.scijava.ui.behaviour.io.InputTriggerConfig;

public class ProjectManager
{
	private WindowManager windowManager;

	public ProjectManager( WindowManager windowManager )
	{
		this.windowManager = windowManager;
	}

	private File proposedProjectFile;


	public void open( final MamutProject project ) throws IOException, SpimDataException
	{
		/*
		 * Load Model
		 */
		final Model model = new Model();
		if ( project.getRawModelFile() != null )
			model.loadRaw( project.getRawModelFile() );

		/*
		 * Load SpimData
		 */
		final String spimDataXmlFilename = project.getDatasetXmlFile().getAbsolutePath();
		final SpimDataMinimal spimData = new XmlIoSpimDataMinimal().load( spimDataXmlFilename );

//		this.project = project;

		final InputTriggerConfig keyconf = windowManager.getKeyConfig();
		final KeyPressedManager keyPressedManager = windowManager.getKeyPressedManager();
		final ViewerOptions options = ViewerOptions.options()
				.inputTriggerConfig( keyconf )
				.shareKeyPressedEvents( keyPressedManager );
		final SharedBigDataViewerData sharedBdvData = new SharedBigDataViewerData(
				spimDataXmlFilename,
				spimData,
				options,
				() -> windowManager.forEachBdvView( bdv -> bdv.requestRepaint() ) );
		final MamutAppModel appModel = new MamutAppModel( model, sharedBdvData, keyconf, keyPressedManager );

		windowManager.setAppModel( appModel );


		/*
		 * Feature calculation.
		 */

		/*
		 * TODO FIXE Ugly hack to get proper service instantiation. Fix it by
		 * proposing a proper Command decoupled from the GUI.
		 */
		final Context context = new Context();
		final MamutFeatureComputerService featureComputerService = context.getService( MamutFeatureComputerService.class );
		final JFrame owner = null; // TODO
		final Dialog featureComputationDialog = new FeatureAndTagDialog( owner, model, featureComputerService );
		featureComputationDialog.setSize( 400, 400 );

		final ToggleDialogAction toggleFeatureComputationDialogAction = new ToggleDialogAction( "feature computation", featureComputationDialog );
	}

	public void loadProject()
	{
		final String fn = proposedProjectFile == null ? null : proposedProjectFile.getAbsolutePath();
		Component parent = null; // TODO
		final File file = FileChooser.chooseFile(
				parent,
				fn,
				new XmlFileFilter(),
				"Open MaMuT Project File",
				FileChooser.DialogType.LOAD );
		if ( file == null )
			return;

		try
		{
			proposedProjectFile = file;
			final MamutProject project = new MamutProjectIO().load( file.getAbsolutePath() );
			open( project );
		}
		catch ( final IOException | SpimDataException e )
		{
			e.printStackTrace();
		}
	}
}
