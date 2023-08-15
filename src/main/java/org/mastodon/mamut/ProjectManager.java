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
package org.mastodon.mamut;

import static org.mastodon.app.MastodonIcons.LOAD_ICON_MEDIUM;
import static org.mastodon.app.MastodonIcons.MAMUT_EXPORT_ICON_MEDIUM;
import static org.mastodon.app.MastodonIcons.MAMUT_IMPORT_ICON_MEDIUM;
import static org.mastodon.app.MastodonIcons.NEW_ICON_MEDIUM;
import static org.mastodon.app.MastodonIcons.SAVE_ICON_MEDIUM;
import static org.mastodon.mamut.io.project.MamutProjectIO.MAMUTPROJECT_VERSION_ATTRIBUTE_CURRENT;
import static org.mastodon.mamut.io.project.MamutProjectIO.MAMUTPROJECT_VERSION_ATTRIBUTE_NAME;

import java.awt.Component;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.embl.mobie.io.ome.zarr.openers.OMEZarrS3Opener;
import org.embl.mobie.io.util.S3Utils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.mastodon.app.MastodonIcons;
import org.mastodon.graph.io.RawGraphIO.FileIdToGraphMap;
import org.mastodon.graph.io.RawGraphIO.GraphToFileIdMap;
import org.mastodon.mamut.feature.MamutRawFeatureModelIO;
import org.mastodon.mamut.importer.simi.SimiImportDialog;
import org.mastodon.mamut.importer.tgmm.TgmmImportDialog;
import org.mastodon.mamut.importer.trackmate.MamutExporter;
import org.mastodon.mamut.importer.trackmate.TrackMateImporter;
import org.mastodon.mamut.io.project.MamutImagePlusProject;
import org.mastodon.mamut.io.project.MamutProject;
import org.mastodon.mamut.io.project.MamutProject.ProjectReader;
import org.mastodon.mamut.io.project.MamutProject.ProjectWriter;
import org.mastodon.mamut.io.project.MamutProjectIO;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.Spot;
import org.mastodon.ui.keymap.CommandDescriptionProvider;
import org.mastodon.ui.keymap.CommandDescriptions;
import org.mastodon.ui.keymap.KeyConfigContexts;
import org.mastodon.ui.util.ExtensionFileFilter;
import org.mastodon.ui.util.FileChooser;
import org.mastodon.ui.util.FileChooser.SelectionMode;
import org.mastodon.ui.util.XmlFileFilter;
import org.mastodon.util.BDVImagePlusExporter;
import org.mastodon.util.DummySpimData;
import org.mastodon.views.bdv.SharedBigDataViewerData;
import org.scijava.plugin.Plugin;
import org.scijava.ui.behaviour.KeyPressedManager;
import org.scijava.ui.behaviour.util.AbstractNamedAction;
import org.scijava.ui.behaviour.util.Actions;
import org.scijava.ui.behaviour.util.RunnableAction;

import bdv.viewer.RequestRepaint;
import bdv.viewer.ViewerOptions;
import bdv.viewer.animate.MessageOverlayAnimator;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.ImageWindow;
import mpicbg.spim.data.SpimData;
import mpicbg.spim.data.SpimDataException;
import mpicbg.spim.data.SpimDataIOException;
import mpicbg.spim.data.XmlIoSpimData;
import mpicbg.spim.data.XmlKeys;
import mpicbg.spim.data.generic.sequence.BasicViewSetup;

public class ProjectManager
{
	public static final String CREATE_PROJECT = "create new project";

	public static final String CREATE_PROJECT_FROM_URL = "create new project from url";

	public static final String LOAD_PROJECT = "load project";

	public static final String SAVE_PROJECT = "save project";

	public static final String SAVE_PROJECT_AS = "save project as";

	public static final String IMPORT_TGMM = "import tgmm";

	public static final String IMPORT_SIMI = "import simi";

	public static final String IMPORT_MAMUT = "import mamut";

	public static final String EXPORT_MAMUT = "export mamut";

	static final String[] CREATE_PROJECT_KEYS = new String[] { "not mapped" };

	static final String[] CREATE_PROJECT_FROM_URL_KEYS = new String[] { "not mapped" };

	static final String[] LOAD_PROJECT_KEYS = new String[] { "not mapped" };

	static final String[] SAVE_PROJECT_KEYS = new String[] { "not mapped" };

	static final String[] SAVE_PROJECT_AS_KEYS = new String[] { "not mapped" };

	static final String[] IMPORT_TGMM_KEYS = new String[] { "not mapped" };

	static final String[] IMPORT_SIMI_KEYS = new String[] { "not mapped" };

	static final String[] IMPORT_MAMUT_KEYS = new String[] { "not mapped" };

	static final String[] EXPORT_MAMUT_KEYS = new String[] { "not mapped" };

	private static final String GUI_TAG = "MamutGui";

	private static final String WINDOWS_TAG = "Windows";

	/*
	 * Command descriptions for all provided commands
	 */
	@Plugin( type = CommandDescriptionProvider.class )
	public static class Descriptions extends CommandDescriptionProvider
	{
		public Descriptions()
		{
			super( KeyConfigContexts.MASTODON );
		}

		@Override
		public void getCommandDescriptions( final CommandDescriptions descriptions )
		{
			descriptions.add( CREATE_PROJECT, CREATE_PROJECT_KEYS, "Create a new project." );
			descriptions.add( CREATE_PROJECT_FROM_URL, CREATE_PROJECT_FROM_URL_KEYS, "Create a new project from URL." );
			descriptions.add( LOAD_PROJECT, LOAD_PROJECT_KEYS, "Load a project." );
			descriptions.add( SAVE_PROJECT, SAVE_PROJECT_KEYS, "Save the current project." );
			descriptions.add( SAVE_PROJECT_AS, SAVE_PROJECT_AS_KEYS, "Save the current project in a new file." );
			descriptions.add( IMPORT_TGMM, IMPORT_TGMM_KEYS,
					"Import tracks from TGMM xml files into the current project." );
			descriptions.add( IMPORT_SIMI, IMPORT_SIMI_KEYS,
					"Import tracks from a Simi Biocell .sbd into the current project." );
			descriptions.add( IMPORT_MAMUT, IMPORT_MAMUT_KEYS, "Import a MaMuT project." );
			descriptions.add( EXPORT_MAMUT, EXPORT_MAMUT_KEYS, "Export current project as a MaMuT project." );
		}
	}

	private final TgmmImportDialog tgmmImportDialog;

	private final SimiImportDialog simiImportDialog;

	private MamutProject project;

	private File proposedProjectRoot;

	private final AbstractNamedAction createProjectAction;

	private final AbstractNamedAction createProjectFromUrlAction;

	private final AbstractNamedAction loadProjectAction;

	private final AbstractNamedAction saveProjectAction;

	private final AbstractNamedAction saveProjectAsAction;

	private final AbstractNamedAction importTgmmAction;

	private final AbstractNamedAction importSimiAction;

	private final AbstractNamedAction importMamutAction;

	private final AbstractNamedAction exportMamutAction;

	public ProjectManager()
	{
		tgmmImportDialog = new TgmmImportDialog( null );
		simiImportDialog = new SimiImportDialog( null );

		createProjectAction = new RunnableAction( CREATE_PROJECT, this::createProject );
		createProjectFromUrlAction = new RunnableAction( CREATE_PROJECT_FROM_URL, this::createProjectFromUrl );
		loadProjectAction = new RunnableAction( LOAD_PROJECT, this::loadProject );
		saveProjectAction = new RunnableAction( SAVE_PROJECT, this::saveProject );
		saveProjectAsAction = new RunnableAction( SAVE_PROJECT_AS, this::saveProjectAs );
		importTgmmAction = new RunnableAction( IMPORT_TGMM, this::importTgmm );
		importSimiAction = new RunnableAction( IMPORT_SIMI, this::importSimi );
		importMamutAction = new RunnableAction( IMPORT_MAMUT, this::importMamut );
		exportMamutAction = new RunnableAction( EXPORT_MAMUT, this::exportMamut );

		updateEnabledActions();
	}

	private void updateEnabledActions()
	{
		final boolean projectOpen = ( project != null );
		saveProjectAction.setEnabled( projectOpen );
		saveProjectAsAction.setEnabled( projectOpen );
		importTgmmAction.setEnabled( projectOpen );
		importSimiAction.setEnabled( projectOpen );
		exportMamutAction.setEnabled( projectOpen );
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
		actions.namedAction( createProjectFromUrlAction, CREATE_PROJECT_FROM_URL_KEYS );
		actions.namedAction( loadProjectAction, LOAD_PROJECT_KEYS );
		actions.namedAction( saveProjectAction, SAVE_PROJECT_KEYS );
		actions.namedAction( saveProjectAsAction, SAVE_PROJECT_AS_KEYS );
		actions.namedAction( importTgmmAction, IMPORT_TGMM_KEYS );
		actions.namedAction( importSimiAction, IMPORT_SIMI_KEYS );
		actions.namedAction( importMamutAction, IMPORT_MAMUT_KEYS );
		actions.namedAction( exportMamutAction, EXPORT_MAMUT_KEYS );
	}

	public synchronized void createProject()
	{
		final Component parent = null; // TODO
		final File file = FileChooser.chooseFile(
				parent,
				null,
				new XmlFileFilter(),
				"Open BigDataViewer File",
				FileChooser.DialogType.LOAD,
				NEW_ICON_MEDIUM.getImage() );
		if ( file == null )
			return;

		try
		{
			open( MamutProjectIO.emptyProject( file ) );
		}
		catch ( final IOException | SpimDataException e )
		{
			e.printStackTrace();
		}
	}

	public synchronized void createProjectFromUrl()
	{
		final Component parent = null; // TODO
		final String urlString = JOptionPane.showInputDialog( parent, "Please input a url for image data" );
		if ( urlString == null )
			return;

		SpimData spimData = null;
		try
		{
			spimData = OMEZarrS3Opener.readURL( urlString );
		}
		catch ( final RuntimeException e )
		{
			final JLabel lblUsername = new JLabel( "Username" );
			final JTextField textFieldUsername = new JTextField();
			final JLabel lblPassword = new JLabel( "Password" );
			final JPasswordField passwordField = new JPasswordField();
			final Object[] ob = { lblUsername, textFieldUsername, lblPassword, passwordField };
			final int result =
					JOptionPane.showConfirmDialog( null, ob, "Please input credentials", JOptionPane.OK_CANCEL_OPTION );

			if ( result == JOptionPane.OK_OPTION )
			{
				final String username = textFieldUsername.getText();
				final char[] password = passwordField.getPassword();
				try
				{
					S3Utils.setS3AccessAndSecretKey( new String[] { username, new String( password ) } );
				}
				finally
				{
					Arrays.fill( password, '0' );
				}
				try
				{
					spimData = OMEZarrS3Opener.readURL( urlString );
				}
				catch ( final Exception e1 )
				{
					e1.printStackTrace();
				}
			}
			else
			{
				return;
			}
		}
		catch ( final Exception e )
		{
			e.printStackTrace();
		}

		final File file = FileChooser.chooseFile(
				parent,
				null,
				new XmlFileFilter(),
				"Save BigDataViewer File",
				FileChooser.DialogType.SAVE,
				NEW_ICON_MEDIUM.getImage() );
		if ( file == null )
			return;

		final XmlIoSpimData xmlIoSpimData = new XmlIoSpimData();
		spimData.setBasePath( file.getParentFile() );
		try
		{
			xmlIoSpimData.save( spimData, file.getAbsolutePath() );
			open( MamutProjectIO.emptyProject( file ) );
		}
		catch ( final IOException | SpimDataException e )
		{
			e.printStackTrace();
		}
	}

	public synchronized void loadProject()
	{
		String fn = null;
		if ( proposedProjectRoot != null )
			fn = proposedProjectRoot.getAbsolutePath();
		else if ( project != null && project.getProjectRoot() != null )
			fn = project.getProjectRoot().getAbsolutePath();
		final Component parent = null; // TODO
		final File file = FileChooser.chooseFile(
				true,
				parent,
				fn,
				new ExtensionFileFilter( "mastodon" ),
				"Open Mastodon Project",
				FileChooser.DialogType.LOAD,
				SelectionMode.FILES_AND_DIRECTORIES,
				LOAD_ICON_MEDIUM.getImage() );
		if ( file == null )
			return;

		try
		{
			proposedProjectRoot = file;
			final MamutProject project = MamutProjectIO.load( file.getAbsolutePath() );
			openWithDialog( project );
		}
		catch ( final IOException | SpimDataException e )
		{
			e.printStackTrace();
		}
	}

	public synchronized void saveProjectAs()
	{
		if ( project == null )
			return;

		final String projectRoot = getProposedProjectRoot( project );

		new Thread( new Runnable()
		{
			@Override
			public void run()
			{
				try
				{

					/*
					 * Check if the image data is based on a non-BDV image. If
					 * it's the case, offer to convert.
					 */

					if ( project instanceof MamutImagePlusProject )
					{

						final AtomicInteger returnUserValue = new AtomicInteger( -1 );
						SwingUtilities.invokeAndWait( new Runnable()
						{

							@Override
							public void run()
							{
								final int val = JOptionPane.showConfirmDialog(
										null,
										"The image data is not currently saved as a BDV file, \n"
												+ "which is optimal for Mastodon. Mastodon might fail \n"
												+ "to load the image data when you will reopen the \n"
												+ "project you are about to save.\n"
												+ "\n"
												+ "Do you want to resave the image to the BDV file \n"
												+ "format prior to saving the Mastodon project? \n"
												+ "\n"
												+ "(Clicking 'Yes' will show the BDV exporter \n"
												+ "interface and close all Mastodon windows, \n"
												+ "then offer to save the Mastodon project.)",
										"Image not in BDV file format",
										JOptionPane.YES_NO_OPTION,
										JOptionPane.QUESTION_MESSAGE,
										MastodonIcons.MASTODON_ICON_MEDIUM );
								returnUserValue.set( val );
							}
						} );

						if ( returnUserValue.get() == JOptionPane.YES_OPTION )
						{
							final MamutAppModel appModel = windowManager.getAppModel();

							// Export imp to BDV.
							final MamutImagePlusProject mipp = ( MamutImagePlusProject ) project;
							final int n = projectRoot.indexOf( '.' );
							final String proposedXmlFile = projectRoot.subSequence( 0, n ).toString() + ".xml";
							final File bdvFile = BDVImagePlusExporter.export( mipp.getImagePlus(), proposedXmlFile );
							final MamutProject np = new MamutProject( new File( projectRoot ), bdvFile );
							np.setSpaceUnits( project.getSpaceUnits() );
							np.setTimeUnits( project.getTimeUnits() );
							project = np;

							// Export the settings file with what we can put in.
							final Element root = new Element( "Settings" );
							final SharedBigDataViewerData sbdv = appModel.getSharedBdvData();
							root.addContent( sbdv.getManualTransformation().toXml() );
							root.addContent( sbdv.getSetupAssignments().toXml() );
							root.addContent( sbdv.getBookmarks().toXml() );
							final Document doc = new Document( root );
							final XMLOutputter xout = new XMLOutputter( Format.getPrettyFormat() );
							final String xmlFilename = bdvFile.getAbsolutePath();
							final String settings = xmlFilename.substring( 0, xmlFilename.length() - ".xml".length() )
									+ ".settings" + ".xml";
							xout.output( doc, new FileWriter( settings ) );

							// Reopen the image data from the new BDV file.
							final SharedBigDataViewerData sharedBdvData =
									openImageData( project, windowManager, false );

							// Recreate app model.
							final MamutAppModel nAppModel = new MamutAppModel(
									appModel.getModel(),
									sharedBdvData,
									windowManager.getKeyPressedManager(),
									windowManager.getTrackSchemeStyleManager(),
									windowManager.getDataDisplayStyleManager(),
									windowManager.getRenderSettingsManager(),
									windowManager.getFeatureColorModeManager(),
									windowManager.getFeatureProjectionsManager(),
									windowManager.getKeymapManager(),
									windowManager.getPlugins(),
									windowManager.getGlobalAppActions() );
							windowManager.setAppModel( nAppModel );

							// Remove listener to imp window closing.
							final ImageWindow window = mipp.getImagePlus().getWindow();
							if ( window != null )
							{
								for ( final WindowListener wl : window.getWindowListeners() )
									window.removeWindowListener( wl );

								window.addWindowListener( new WindowAdapter()
								{
									@Override
									public void windowClosing( final java.awt.event.WindowEvent e )
									{
										mipp.getImagePlus().close();
									}
								} );
							}
						}
					}

					/*
					 * Ask for a file path to save to.
					 */

					final Component parent = null; // TODO
					SwingUtilities.invokeAndWait( new Runnable()
					{
						@Override
						public void run()
						{
							final File file = FileChooser.chooseFile( true,
									parent,
									projectRoot,
									new ExtensionFileFilter( "mastodon" ),
									"Save Mastodon Project",
									FileChooser.DialogType.SAVE,
									SelectionMode.FILES_ONLY,
									SAVE_ICON_MEDIUM.getImage() );
							if ( file == null )
								return;

							proposedProjectRoot = file;
							new Thread( () -> {
								try
								{
									saveProject( proposedProjectRoot );
								}
								catch ( final IOException e )
								{
									e.printStackTrace();
								}
							} ).start();
						}
					} );
				}
				catch ( final InterruptedException | InvocationTargetException | SpimDataException | IOException e )
				{
					e.printStackTrace();
				}
			}
		} ).start();
	}

	public synchronized void saveProject()
	{
		if ( project == null )
			return;

		// If a Mastodon project was not yet created, ask to create one.
		if ( project.getProjectRoot() == null )
		{
			saveProjectAs();
			return;
		}

		try
		{
			saveProject( project.getProjectRoot() );
		}
		catch ( final IOException e )
		{
			e.printStackTrace();
		}
	}

	public synchronized void saveProject( final File projectRoot ) throws IOException
	{
		if ( project == null )
			return;

		final File tmpDatasetXml = originalOrBackupDatasetXml( project );

		// update project root
		project.setProjectRoot( projectRoot );
		try (final MamutProject.ProjectWriter writer = project.openForWriting())
		{
			new MamutProjectIO().save( project, writer );
			final Model model = windowManager.getAppModel().getModel();
			final GraphToFileIdMap< Spot, Link > idmap = model.saveRaw( writer );
			// Serialize feature model.
			MamutRawFeatureModelIO.serialize( windowManager.getContext(), model, idmap, writer );
			// Serialize GUI state.
			saveGUI( writer );
			// Save a copy of the Spim Data Xml File
			saveBackupDatasetXml( tmpDatasetXml, writer );
			// Set save point.
			model.setSavePoint();
		}
		updateEnabledActions();
	}

	/**
	 * Opens a project. If {@code project.getProjectRoot() == null} this is a
	 * new project and data structures are initialized as empty. The image data
	 * {@code project.getDatasetXmlFile()} must always be set. The GUI state is
	 * not restored, even if one is found in the project file.
	 *
	 * @param project
	 *            the project to open.
	 * @throws IOException
	 *             if an IO exception occurs during opening.
	 * @throws SpimDataException
	 *             if a spim-data exception occurs while opening the spim-data
	 *             XML file.
	 */
	public synchronized void open( final MamutProject project ) throws IOException, SpimDataException
	{
		open( project, false, false );
	}

	/**
	 * Opens a project. GUI state is restored. And if the image data cannot
	 * be loaded a dialog shows up telling the user about the problem, and
	 * offering to start Mastodon on dummy image data.
	 */
	public synchronized void openWithDialog( final MamutProject project ) throws IOException, SpimDataException
	{
		try
		{
			open( project, true, false );
		}
		catch ( final SpimDataIOException | RuntimeException e )
		{
			if ( getUserPermissionToOpenDummyData( project, e ) )
			{
				open( project, true, true );
				return;
			}

			throw e;
		}
	}

	/**
	 * Opens a project. If {@code project.getProjectRoot() == null} this is a
	 * new project and data structures are initialized as empty. The image data
	 * {@code project.getDatasetXmlFile()} must always be set.
	 *
	 * @param project
	 *            the project to open.
	 * @param restoreGUIState
	 *            if <code>true</code>, the GUI state settings will be read from
	 *            the project file, and if found, the saved GUI state will be
	 *            restored.
	 * @param loadDummyData
	 *            Load dummy image data (black images), this also works if the
	 *            dataset XML is missing.
	 * @throws IOException
	 *             if an IO exception occurs during opening.
	 * @throws SpimDataException
	 *             if a spim-data exception occurs while opening the spim-data
	 *             XML file.
	 */
	public synchronized void open(
			final MamutProject project,
			final boolean restoreGUIState,
			final boolean loadDummyData ) throws IOException, SpimDataException
	{
		final MamutProject localProject;

		// Check whether the project points to a BDV file.
		final String canonicalPath = project.getDatasetXmlFile().getAbsolutePath();
		if ( !canonicalPath.endsWith( ".xml" ) && !canonicalPath.endsWith( DummySpimData.DUMMY ) )
		{
			final ImagePlus imp;

			// Do we have the ImagePlus already in memory?
			if ( project instanceof MamutImagePlusProject )
			{
				imp = ( ( MamutImagePlusProject ) project ).getImagePlus();
				// No need to morph.
				localProject = project;
			}
			else
			{
				// Assume the path points to a plain image file.
				imp = IJ.openImage( canonicalPath );
				// If it does not work tell the user.
				if ( imp == null )
					throw new IOException( "Cannot open image " + canonicalPath );

				// Morph the project.
				localProject = new MamutImagePlusProject( imp );
				localProject.setProjectRoot( project.getProjectRoot() );
				localProject.setSpaceUnits( project.getSpaceUnits() );
				localProject.setTimeUnits( project.getTimeUnits() );
			}
		}
		else
		{
			// Opening a project with standard BDV (or DUMMY) image data
			localProject = project;
		}

		// Prepare image data.
		final SharedBigDataViewerData sharedBdvData = openImageData( localProject, windowManager, loadDummyData );

		this.project = localProject;

		// Load model.
		loadModel( windowManager, sharedBdvData, localProject, restoreGUIState );

		updateEnabledActions();
	}

	private static void loadModel( final WindowManager windowManager, final SharedBigDataViewerData sharedBdvData,
			final MamutProject project, final boolean restoreGUIState ) throws IOException
	{
		/*
		 * Try to read units from spimData is they are not present
		 */
		if ( project.getSpaceUnits() == null )
		{
			project.setSpaceUnits(
					sharedBdvData.getSpimData().getSequenceDescription().getViewSetupsOrdered().stream()
							.filter( BasicViewSetup::hasVoxelSize )
							.map( setup -> setup.getVoxelSize().unit() )
							.findFirst()
							.orElse( "pixel" ) );
		}

		if ( project.getTimeUnits() == null )
		{
			project.setTimeUnits( "frame" );
		}

		/*
		 * Load Model
		 */
		final Model model = new Model( project.getSpaceUnits(), project.getTimeUnits() );
		final boolean isNewProject = project.getProjectRoot() == null;
		if ( !isNewProject )
		{
			try (final MamutProject.ProjectReader reader = project.openForReading())
			{
				final FileIdToGraphMap< Spot, Link > idmap = model.loadRaw( reader );
				// Load features.
				MamutRawFeatureModelIO.deserialize(
						windowManager.getContext(),
						model,
						idmap,
						reader );
			}
			catch ( final ClassNotFoundException e )
			{
				e.printStackTrace();
			}
		}

		model.setSavePoint();
		model.declareDefaultFeatures();

		final MamutAppModel appModel = new MamutAppModel(
				model,
				sharedBdvData,
				windowManager.getKeyPressedManager(),
				windowManager.getTrackSchemeStyleManager(),
				windowManager.getDataDisplayStyleManager(),
				windowManager.getRenderSettingsManager(),
				windowManager.getFeatureColorModeManager(),
				windowManager.getFeatureProjectionsManager(),
				windowManager.getKeymapManager(),
				windowManager.getPlugins(),
				windowManager.getGlobalAppActions() );

		windowManager.setAppModel( appModel );

		// Restore GUI state if loaded project, now that we have an App model.
		if ( !isNewProject && restoreGUIState )
		{
			try (final MamutProject.ProjectReader reader = project.openForReading())
			{
				try
				{
					loadGUI( reader, windowManager );
				}
				catch ( final FileNotFoundException fnfe )
				{
					// Ignore missing gui file.
				}
			}
		}
	}

	public synchronized void importTgmm()
	{
		if ( project == null )
			return;

		final MamutAppModel appModel = windowManager.getAppModel();
		tgmmImportDialog.showImportDialog( appModel.getSharedBdvData().getSpimData(), appModel.getModel() );

		updateEnabledActions();
	}

	public synchronized void importSimi()
	{
		if ( project == null )
			return;

		final MamutAppModel appModel = windowManager.getAppModel();
		simiImportDialog.showImportDialog( appModel.getSharedBdvData().getSpimData(), appModel.getModel() );

		updateEnabledActions();
	}

	public synchronized void importMamut()
	{
		final Component parent = null; // TODO
		final File file = FileChooser.chooseFile(
				parent,
				null,
				new XmlFileFilter(),
				"Import MaMuT Project",
				FileChooser.DialogType.LOAD,
				MAMUT_IMPORT_ICON_MEDIUM.getImage() );
		if ( file == null )
			return;

		try
		{
			final TrackMateImporter importer = new TrackMateImporter( file );
			open( importer.createProject() );
			importer.readModel( windowManager.getAppModel().getModel(), windowManager.getFeatureSpecsService() );
		}
		catch ( final IOException | SpimDataException e )
		{
			e.printStackTrace();
		}

		updateEnabledActions();
	}

	public synchronized void exportMamut()
	{
		if ( project == null )
			return;

		final String filename = getProprosedMamutExportFileName( project );

		final Component parent = null; // TODO
		final File file = FileChooser.chooseFile(
				parent,
				filename,
				new XmlFileFilter(),
				"Export As MaMuT Project",
				FileChooser.DialogType.SAVE,
				MAMUT_EXPORT_ICON_MEDIUM.getImage() );
		if ( file == null )
			return;

		try
		{
			MamutExporter.export( file, windowManager.getAppModel().getModel(), project );
		}
		catch ( final IOException e )
		{
			e.printStackTrace();
		}
	}

	private static final String EXT_DOT_MASTODON = ".mastodon";

	private static String stripExtensionIfPresent( final String fn, final String ext )
	{
		return fn.endsWith( ext )
				? fn.substring( 0, fn.length() - ext.length() )
				: fn;
	}

	private static String getProprosedMamutExportFileName( final MamutProject project )
	{
		final File pf = project.getProjectRoot();
		if ( pf != null )
		{
			final String fn = stripExtensionIfPresent( pf.getName(), EXT_DOT_MASTODON );
			return new File( pf.getParentFile(), fn + "_mamut.xml" ).getAbsolutePath();
		}
		else
		{
			final File f = project.getDatasetXmlFile();
			final String fn = stripExtensionIfPresent( f.getName(), ".xml" );
			return new File( f.getParentFile(), fn + "_mamut.xml" ).getAbsolutePath();
		}
	}

	private static String getProposedProjectRoot( final MamutProject project )
	{
		if ( project.getProjectRoot() != null )
			return project.getProjectRoot().getAbsolutePath();
		else
		{
			final File f = project.getDatasetXmlFile();
			final String fn = stripExtensionIfPresent( f.getName(), ".xml" );
			return new File( f.getParentFile(), fn + EXT_DOT_MASTODON ).getAbsolutePath();
		}
	}

	/*
	 * GUI IO methods.
	 */

	/**
	 * Serialize window positions and states.
	 *
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	private void saveGUI( final ProjectWriter writer ) throws FileNotFoundException, IOException
	{
		final Element guiRoot = new Element( GUI_TAG );
		guiRoot.setAttribute( MAMUTPROJECT_VERSION_ATTRIBUTE_NAME, MAMUTPROJECT_VERSION_ATTRIBUTE_CURRENT );
		final Element windows = new Element( WINDOWS_TAG );
		windowManager.forEachView( ( view ) -> windows.addContent(
				MamutViewStateSerialization.toXml( view ) ) );
		windowManager.forEachBranchView( ( view ) -> windows.addContent(
				MamutViewStateSerialization.toXml( view ) ) );
		guiRoot.addContent( windows );
		final Document doc = new Document( guiRoot );
		final XMLOutputter xout = new XMLOutputter( Format.getPrettyFormat() );
		try (OutputStream outputStream = writer.getGuiOutputStream())
		{
			xout.output( doc, outputStream );
		}
	}

	private static void loadGUI( final ProjectReader reader, final WindowManager windowManager ) throws IOException
	{
		final SAXBuilder sax = new SAXBuilder();
		Document guiDoc;
		try (InputStream inputStream = reader.getGuiInputStream())
		{
			guiDoc = sax.build( inputStream );
		}
		catch ( final JDOMException e )
		{
			throw new IOException( e );
		}
		final Element root = guiDoc.getRootElement();
		if ( !GUI_TAG.equals( root.getName() ) )
			throw new IOException( "expected <" + GUI_TAG + "> root element. wrong file?" );

		final Element windowsEl = root.getChild( WINDOWS_TAG );
		if ( null == windowsEl )
			return;

		MamutViewStateSerialization.fromXml( windowsEl, windowManager );
	}

	/**
	 * Opens and prepares the shared image data, based on whether the Mamut
	 * project points to a BDV XML/H5 pair or an opened ImagePlus.
	 *
	 * @param project
	 *            the project.
	 * @param windowManager
	 *            the {@link WindowManager} instance, used to create a lambda
	 *            that refreshes all BDV views and get the session
	 *            {@link KeyPressedManager}.
	 * @param dummyData
	 *            if {@code true}: in case image data was not found during
	 *            opening, the user will be provided with work around options
	 *            (e.g. loading dummy image data, fixing the project manually)
	 * @return a new {@link SharedBigDataViewerData} instance or {@code null},
	 *         if image data (BDV) was not found and the user decided to not
	 *         open dummy image data instead
	 */
	private static final SharedBigDataViewerData openImageData(
			final MamutProject project,
			final WindowManager windowManager,
			final boolean dummyData ) throws SpimDataException, IOException
	{
		// Prepare base view options.
		final ViewerOptions options = ViewerOptions.options()
				.shareKeyPressedEvents( windowManager.getKeyPressedManager() )
				.msgOverlay( new MessageOverlayAnimator( 1500, 0.005, 0.02 ) );

		final RequestRepaint requestRepaint = () -> windowManager.forEachBdvView( MamutViewBdv::requestRepaint );

		// Is it based on ImagePlus?
		if ( project instanceof MamutImagePlusProject )
		{
			final MamutImagePlusProject mipp = ( MamutImagePlusProject ) project;
			return SharedBigDataViewerData.fromImagePlus( mipp.getImagePlus(), options, requestRepaint );
		}

		// Open dummy data string?
		final String spimDataXmlFilename = project.getDatasetXmlFile().getPath();
		if ( DummySpimData.isDummyString( spimDataXmlFilename ) )
			return SharedBigDataViewerData.fromDummyFilename( spimDataXmlFilename, options, requestRepaint );

		// Open dummy data flag?
		if ( dummyData )
			return openDummyImageData( project, options, requestRepaint );

		return SharedBigDataViewerData.fromSpimDataXmlFile( project.getDatasetXmlFile().getAbsolutePath(),
				options,
				requestRepaint );
	}

	private static SharedBigDataViewerData openDummyImageData( final MamutProject project, final ViewerOptions options,
			final RequestRepaint requestRepaint )
	{
		try
		{
			final String backupDatasetXml = originalOrBackupDatasetXml( project ).getAbsolutePath();
			return SharedBigDataViewerData.createDummyDataFromSpimDataXml( backupDatasetXml, options, requestRepaint );
		}
		catch ( final Throwable e )
		{
			return simpleDummyData( project, options, requestRepaint );
		}
	}

	private static SharedBigDataViewerData simpleDummyData( final MamutProject project, final ViewerOptions options,
			final RequestRepaint requestRepaint )
	{
		try (final MamutProject.ProjectReader reader = project.openForReading())
		{
			final Model model = new Model( "pixel", "frame" );
			model.loadRaw( reader );
			final String requiredImageSizeAsString = requiredImageSizeAsString( model );
			return SharedBigDataViewerData.fromDummyFilename( requiredImageSizeAsString, options, requestRepaint );
		}
		catch ( final IOException e )
		{
			throw new RuntimeException( e );
		}
	}

	private static String requiredImageSizeAsString( final Model model )
	{
		int time = 0;
		double x = 0;
		double y = 0;
		double z = 0;
		for ( final Spot spot : model.getGraph().vertices() )
		{
			time = Math.max( time, spot.getTimepoint() );
			final double radius = Math.sqrt( spot.getBoundingSphereRadiusSquared() );
			x = Math.max( x, spot.getDoublePosition( 0 ) + radius );
			y = Math.max( y, spot.getDoublePosition( 1 ) + radius );
			z = Math.max( z, spot.getDoublePosition( 2 ) + radius );
		}
		return String.format( "x=%s y=%s z=%s t=%s.dummy",
				roundUp( x ) + 1,
				roundUp( y ) + 1,
				roundUp( z ) + 1,
				time + 1 );
	}

	private static long roundUp( final double x )
	{
		return ( long ) Math.ceil( x );
	}

	private static File originalOrBackupDatasetXml( final MamutProject project )
	{
		try
		{
			final File datasetXml = project.getDatasetXmlFile();
			if ( datasetXml.exists() )
				return datasetXml;
			else
				return copyBackupDatasetXmlToTmpFile( project );
		}
		catch ( final IOException e )
		{
			return null;
		}
	}

	private static File copyBackupDatasetXmlToTmpFile( final MamutProject project ) throws IOException
	{
		try (final MamutProject.ProjectReader reader = project.openForReading();
				final InputStream is = reader.getBackupDatasetXmlInputStream())
		{
			final File tmp = File.createTempFile( "mastodon-dataset-xml-backup", ".xml" );
			tmp.deleteOnExit();
			Files.copy( is, tmp.toPath(), StandardCopyOption.REPLACE_EXISTING );
			return tmp;
		}
	}

	/**
	 * Saves a copy of the dataset.xml (if there is any) to the projection location.
	 */
	private void saveBackupDatasetXml( final File tmpDatasetXml, final ProjectWriter projectWriter )
	{
		if ( tmpDatasetXml == null )
			return;

		try (OutputStream out = projectWriter.getBackupDatasetXmlOutputStream())
		{
			Files.copy( tmpDatasetXml.toPath(), out );
		}
		catch ( final IOException e )
		{
			System.err.println( "Could not create backup of the dataset.xml file. Reason: '" + e.getMessage() + "'." );
		}
	}

	/**
	 * Show an dialog the explains to the user why the image data could not been
	 * loaded, and offers to open Mastodon with dummy image data.
	 */
	private static boolean getUserPermissionToOpenDummyData( final MamutProject project, final Exception e )
	{
		final String problemDescription = getProblemDescription( project, e );
		System.err.println( problemDescription );
		final String title = "Problem Opening Mastodon Project";
		String message = "";
		message += "Mastodon could not find the images associated with this project.\n";
		message += "\n";
		message += problemDescription + "\n";
		message += "\n";
		message += "It is still possible to open the project.\n";
		message += "You can inspect and modify the tracking data.\n";
		message += "But you won't be able to see the image data.\n";
		message += "\n";
		message += "You may fix this problem by correcting the image path in the Mastodon project.\n";
		message += "In the Mastodon menu select: File -> Fix Image Path.\n";
		message += "\n";
		message += "How would you like to continue?";
		final String[] options = { "Open With Dummy Images", "Cancel" };
		final int dialogResult = JOptionPane.showOptionDialog( null, message, title, JOptionPane.YES_NO_OPTION,
				JOptionPane.WARNING_MESSAGE, null, options, null );
		return dialogResult == JOptionPane.YES_OPTION;
	}

	private static String getProblemDescription( final MamutProject project, final Exception e )
	{
		final File datasetXml = project.getDatasetXmlFile();
		if ( !datasetXml.exists() )
			return "The image data XML was not found:\n" + datasetXml;
		final Throwable cause = e.getCause();
		if ( cause instanceof UnknownHostException )
			return errorMessageUnknownHost( datasetXml, cause.getMessage() );
		return e.getMessage();
	}

	private static String errorMessageUnknownHost( final File datasetXml, final String host )
	{
		final SAXBuilder sax = new SAXBuilder();
		try
		{
			final Document doc = sax.build( datasetXml );
			final Element root = doc.getRootElement();
			final String baseUrl = root
					.getChild( XmlKeys.SEQUENCEDESCRIPTION_TAG )
					.getChild( XmlKeys.IMGLOADER_TAG )
					.getChildText( "baseUrl" );
			return "Cannot reach host  " + host + " for the dataset URL: " + baseUrl;
		}
		catch ( final Exception e )
		{
			return "Unparsable dataset file: " + e.getMessage();
		}
	}
}
