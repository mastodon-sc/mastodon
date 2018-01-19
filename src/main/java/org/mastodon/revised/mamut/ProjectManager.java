package org.mastodon.revised.mamut;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.io.File;
import java.io.IOException;

import java.util.Random;
import javax.swing.JFrame;

import org.mastodon.revised.bdv.SharedBigDataViewerData;
import org.mastodon.revised.bdv.overlay.ui.RenderSettingsManager;
import org.mastodon.revised.mamut.feature.MamutFeatureComputerService;
import org.mastodon.revised.model.mamut.Model;
import org.mastodon.revised.model.tag.TagSetStructure;
import org.mastodon.revised.trackscheme.display.style.TrackSchemeStyleManager;
import org.mastodon.revised.ui.util.FileChooser;
import org.mastodon.revised.ui.util.FileChooser.SelectionMode;
import org.mastodon.revised.ui.util.XmlFileFilter;
import org.mastodon.revised.util.ToggleDialogAction;
import org.scijava.Context;
import org.scijava.ui.behaviour.KeyPressedManager;
import org.scijava.ui.behaviour.io.InputTriggerConfig;
import org.scijava.ui.behaviour.util.AbstractNamedAction;
import org.scijava.ui.behaviour.util.Actions;
import org.scijava.ui.behaviour.util.RunnableAction;

import bdv.spimdata.SpimDataMinimal;
import bdv.spimdata.XmlIoSpimDataMinimal;
import bdv.viewer.ViewerOptions;
import mpicbg.spim.data.SpimDataException;

public class ProjectManager
{
	public static final String CREATE_PROJECT = "create new project";
	public static final String LOAD_PROJECT = "load project";
	public static final String SAVE_PROJECT = "save project";
	public static final String IMPORT_TGMM = "import tgmm";

	static final String[] CREATE_PROJECT_KEYS = new String[] { "not mapped" };
	static final String[] LOAD_PROJECT_KEYS = new String[] { "not mapped" };
	static final String[] SAVE_PROJECT_KEYS = new String[] { "not mapped" };
	static final String[] IMPORT_TGMM_KEYS = new String[] { "not mapped" };

	private final WindowManager windowManager;

	private final TgmmImportDialog tgmmImportDialog;

	private MamutProject project;

	private File proposedProjectFolder;

	private final AbstractNamedAction createProjectAction;

	private final AbstractNamedAction loadProjectAction;

	private final AbstractNamedAction saveProjectAction;

	private final AbstractNamedAction importTgmmAction;

	public ProjectManager( final WindowManager windowManager )
	{
		this.windowManager = windowManager;

		tgmmImportDialog = new TgmmImportDialog( null );

		createProjectAction = new RunnableAction( CREATE_PROJECT, this::createProject );
		loadProjectAction = new RunnableAction( LOAD_PROJECT, this::loadProject );
		saveProjectAction = new RunnableAction( SAVE_PROJECT, this::saveProject );
		importTgmmAction = new RunnableAction( IMPORT_TGMM, this::importTgmm );

		updateEnabledActions();
	}

	private void updateEnabledActions()
	{
		final boolean projectOpen = ( project != null );
		saveProjectAction.setEnabled( projectOpen );
		importTgmmAction.setEnabled( projectOpen );
	}

	/**
	 * Add Project New/Load/Save actions and install them in the specified
	 * {@link Actions}.
	 *
	 * @param actions
	 *            Actions are added here.
	 */
	public void install( final Actions actions )
	{
		actions.namedAction( createProjectAction, CREATE_PROJECT_KEYS );
		actions.namedAction( loadProjectAction, LOAD_PROJECT_KEYS );
		actions.namedAction( saveProjectAction, SAVE_PROJECT_KEYS );
		actions.namedAction( importTgmmAction, IMPORT_TGMM_KEYS );
	}

	public synchronized void createProject()
	{
		final Component parent = null; // TODO
		final File file = FileChooser.chooseFile(
				parent,
				null,
				new XmlFileFilter(),
				"Open BigDataViewer File",
				FileChooser.DialogType.LOAD );
		if ( file == null )
			return;

		try
		{
			open( new MamutProject( file.getParentFile(), file ) );
		}
		catch ( final IOException | SpimDataException e )
		{
			e.printStackTrace();
		}
	}

	public synchronized void loadProject()
	{
		final String fn = proposedProjectFolder == null
				? ( project == null
						? null
						: project.getProjectFolder().getAbsolutePath() )
				: proposedProjectFolder.getAbsolutePath();
		final Component parent = null; // TODO
		final File file = FileChooser.chooseFile(
				parent,
				fn,
				null,
				"Open Mastodon Project",
				FileChooser.DialogType.LOAD,
				SelectionMode.DIRECTORIES_ONLY );
		if ( file == null )
			return;

		try
		{
			proposedProjectFolder = file;
			final MamutProject project = new MamutProjectIO().load( file.getAbsolutePath() );
			open( project );
		}
		catch ( final IOException | SpimDataException e )
		{
			e.printStackTrace();
		}
	}

	public synchronized void saveProject()
	{
		if ( project == null )
			return;

		final String folderPath = proposedProjectFolder == null
				? project.getProjectFolder().getAbsolutePath()
				: proposedProjectFolder.getAbsolutePath();

		final Component parent = null; // TODO
		final File folder = FileChooser.chooseFile(
				parent,
				folderPath,
				null,
				"Save Mastodon Project",
				FileChooser.DialogType.SAVE,
				SelectionMode.DIRECTORIES_ONLY );
		if ( folder == null )
			return;

		try
		{
			proposedProjectFolder = folder;
			saveProject( proposedProjectFolder );
		}
		catch ( final IOException e )
		{
			e.printStackTrace();
		}
	}

	public synchronized void open( final MamutProject project ) throws IOException, SpimDataException
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

		final InputTriggerConfig keyconf = windowManager.getKeyConfig();
		final KeyPressedManager keyPressedManager = windowManager.getKeyPressedManager();
		final TrackSchemeStyleManager trackSchemeStyleManager = windowManager.getTrackSchemeStyleManager();
		final RenderSettingsManager renderSettingsManager = windowManager.getRenderSettingsManager();
		final ViewerOptions options = ViewerOptions.options().shareKeyPressedEvents( keyPressedManager );
		final SharedBigDataViewerData sharedBdvData = new SharedBigDataViewerData(
				spimDataXmlFilename,
				spimData,
				options,
				() -> windowManager.forEachBdvView( bdv -> bdv.requestRepaint() ) );
		final MamutAppModel appModel = new MamutAppModel( model, sharedBdvData, keyconf, keyPressedManager, trackSchemeStyleManager, renderSettingsManager );

		windowManager.setAppModel( appModel );

		/*
		 * Feature calculation.
		 */

		/*
		 * TODO REMOVE
		 * Set TagSetStructure for debugging.
		 */
		final TagSetStructure tss = new TagSetStructure();
		final Random ran = new Random( 0l );
		final TagSetStructure.TagSet reviewedByTag = tss.createTagSet( "Reviewed by" );
		reviewedByTag.createTag( "Pavel", new Color( ran.nextInt() ) );
		reviewedByTag.createTag( "Mette", new Color( ran.nextInt() ) );
		reviewedByTag.createTag( "Tobias", new Color( ran.nextInt() ) );
		reviewedByTag.createTag( "JY", new Color( ran.nextInt() ) );
		final TagSetStructure.TagSet locationTag = tss.createTagSet( "Location" );
		locationTag.createTag( "Anterior", new Color( ran.nextInt() ) );
		locationTag.createTag( "Posterior", new Color( ran.nextInt() ) );
		System.out.println( "Initial TagSetStructure:\n" + tss );
		model.getTagSetModel().setTagSetStructure( tss );

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

		this.project = project;
		updateEnabledActions();
	}

	public synchronized void saveProject( File projectFolder ) throws IOException
	{
		if ( project == null )
			return;

		project.setProjectFolder( projectFolder );
		new MamutProjectIO().save( project );

		final Model model = windowManager.getAppModel().getModel();
		final File modelFile = project.getRawModelFile();
		model.saveRaw( modelFile );

		updateEnabledActions();
	}

	public void importTgmm()
	{
		if ( project == null )
			return;

		final MamutAppModel appModel = windowManager.getAppModel();
		tgmmImportDialog.showImportDialog( appModel.getSharedBdvData().getSpimData(), appModel.getModel() );

		updateEnabledActions();
	}
}
