package org.mastodon.mamut.io;

import static org.mastodon.app.MastodonIcons.LOAD_ICON_MEDIUM;

import java.awt.Component;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import javax.swing.JOptionPane;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.mastodon.graph.io.RawGraphIO.FileIdToGraphMap;
import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.WindowManager;
import org.mastodon.mamut.feature.MamutRawFeatureModelIO;
import org.mastodon.mamut.io.project.MamutImagePlusProject;
import org.mastodon.mamut.io.project.MamutProject;
import org.mastodon.mamut.io.project.MamutProjectIO;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.Spot;
import org.mastodon.ui.keymap.KeymapManager;
import org.mastodon.ui.util.ExtensionFileFilter;
import org.mastodon.ui.util.FileChooser;
import org.mastodon.ui.util.FileChooser.SelectionMode;
import org.mastodon.util.DummySpimData;
import org.mastodon.views.bdv.SharedBigDataViewerData;
import org.scijava.Context;
import org.scijava.ui.behaviour.KeyPressedManager;

import ij.IJ;
import ij.ImagePlus;
import mpicbg.spim.data.SpimDataException;
import mpicbg.spim.data.SpimDataIOException;
import mpicbg.spim.data.XmlKeys;

/**
 * Static methods to open a Mastodon Mamut project.
 */
public class ProjectLoader
{

	static final String GUI_TAG = "MamutGui";

	static final String WINDOWS_TAG = "Windows";

	private static File proposedProjectRoot;

	private static MamutProject project;

	/**
	 * Opens a project. The GUI state is not restored.
	 * 
	 * @param mastodonFile
	 *            path to a Mastodon file.
	 * @param context
	 *            the current context.
	 * @return the loaded {@link ProjectModel}.
	 * @throws IOException
	 *             if the project points to a regular image file for image data,
	 *             and that file cannot be opened properly, or if there is a
	 *             problem loading the model data, or if there is a problem
	 *             reading the GUI state.
	 * @throws SpimDataException
	 *             if the project points to a BDV file for image data, and that
	 *             BDV cannot be opened properly.
	 */
	public static ProjectModel open( final String mastodonFile, final Context context ) throws IOException, SpimDataException
	{
		final MamutProject project = MamutProjectIO.load( mastodonFile );
		return open( project, context, false, false );
	}

	/**
	 * Opens a project. The GUI state is not restored.
	 * 
	 * @param project
	 *            the object describing the project on disk.
	 * @param context
	 *            the current context.
	 * @return the loaded {@link ProjectModel}.
	 * @throws IOException
	 *             if the project points to a regular image file for image data,
	 *             and that file cannot be opened properly, or if there is a
	 *             problem loading the model data, or if there is a problem
	 *             reading the GUI state.
	 * @throws SpimDataException
	 *             if the project points to a BDV file for image data, and that
	 *             BDV cannot be opened properly.
	 */
	public static ProjectModel open( final MamutProject project, final Context context ) throws IOException, SpimDataException
	{
		return open( project, context, false, false );
	}

	/**
	 * Opens a specified project.
	 * 
	 * @param mastodonFile
	 *            path to a Mastodon file.
	 * @param context
	 *            the current context.
	 * @param restoreGUIState
	 *            if <code>true</code>, the GUI state will be restored.
	 * @param authorizeSubstituteDummyData
	 *            if <code>true</code>, and if the image data cannot be loaded,
	 *            a dummy image data will be substituted. In that case a
	 *            {@link SpimDataException} is never thrown.
	 * @return the loaded {@link ProjectModel}.
	 * @throws IOException
	 *             if the project points to a regular image file for image data,
	 *             and that file cannot be opened properly, or if there is a
	 *             problem loading the model data, or if there is a problem
	 *             reading the GUI state.
	 * @throws SpimDataException
	 *             if the project points to a BDV file for image data, and that
	 *             BDV cannot be opened properly.
	 */
	public static ProjectModel open( final String mastodonFile, final Context context, final boolean restoreGUIState, final boolean authorizeSubstituteDummyData ) throws IOException, SpimDataException
	{
		final MamutProject project = MamutProjectIO.load( mastodonFile );
		return open( project, context, restoreGUIState, authorizeSubstituteDummyData );
	}

	/**
	 * Opens a specified project.
	 * 
	 * @param project
	 *            the object describing the project on disk.
	 * @param context
	 *            the current context.
	 * @param restoreGUIState
	 *            if <code>true</code>, the GUI state will be restored.
	 * @param authorizeSubstituteDummyData
	 *            if <code>true</code>, and if the image data cannot be loaded,
	 *            a dummy image data will be substituted. In that case a
	 *            {@link SpimDataException} is never thrown.
	 * @return the loaded {@link ProjectModel}.
	 * @throws IOException
	 *             if the project points to a regular image file for image data,
	 *             and that file cannot be opened properly, or if there is a
	 *             problem loading the model data, or if there is a problem
	 *             reading the GUI state.
	 * @throws SpimDataException
	 *             if the project points to a BDV file for image data, and that
	 *             BDV cannot be opened properly.
	 */
	public static ProjectModel open( final MamutProject project, final Context context, final boolean restoreGUIState, final boolean authorizeSubstituteDummyData ) throws IOException, SpimDataException
	{
		final SharedBigDataViewerData imageData = loadImageData( project, authorizeSubstituteDummyData );
		final Model model = loadModel( project, context );
		final KeyPressedManager keyPressedManager = new KeyPressedManager();
		final KeymapManager keymapManager = new KeymapManager();
		final ProjectModel appModel = new ProjectModel( context, model, imageData, keyPressedManager, keymapManager, project );

		if ( restoreGUIState )
			loadGUI( project, appModel.getWindowManager() );

		return appModel;
	}

	/**
	 * Opens a project interactively, prompting the user for the project file.
	 * <p>
	 * If the image data cannot be loaded a dialog shows up telling the user
	 * about the problem, and offering to start Mastodon on substituted dummy
	 * image data. If the user declines, a {@link SpimDataException} is thrown.
	 * <p>
	 * The GUI state is restored.
	 * 
	 * @param context
	 *            the current context.
	 * @param parentComponent
	 *            a component to use as parent to show dialogs during opening.
	 *            Can be <code>null</code>.
	 * @return the loaded {@link ProjectModel}.
	 * @throws IOException
	 *             if the project points to a regular image file for image data,
	 *             and that file cannot be opened properly, or if there is a
	 *             problem loading the model data, or if there is a problem
	 *             reading the GUI state.
	 * @throws SpimDataException
	 *             if the project points to a BDV file that cannot be opened,
	 *             and the user declined to substitute a dummy dataset.
	 */
	public static final ProjectModel openWithDialog( final Context context, final Component parentComponent )
	{
		String fn = null;
		if ( proposedProjectRoot != null )
			fn = proposedProjectRoot.getAbsolutePath();
		else if ( project != null && project.getProjectRoot() != null )
			fn = project.getProjectRoot().getAbsolutePath();
		final File file = FileChooser.chooseFile(
				true,
				parentComponent,
				fn,
				new ExtensionFileFilter( "mastodon" ),
				"Open Mastodon Project",
				FileChooser.DialogType.LOAD,
				SelectionMode.FILES_AND_DIRECTORIES,
				LOAD_ICON_MEDIUM.getImage() );
		if ( file == null )
			return null;

		try
		{
			proposedProjectRoot = file;
			final MamutProject project = MamutProjectIO.load( file.getAbsolutePath() );
			return openWithDialog( project, context, parentComponent );
		}
		catch ( final IOException | SpimDataException e )
		{
			JOptionPane.showMessageDialog(
					parentComponent,
					"Problem reading Mastodon file:\n" + e.getMessage(),
					"Error reading Mastodon file",
					JOptionPane.ERROR_MESSAGE );
		}
		return null;
	}

	/**
	 * Opens a project interactively from a specified Mastodon file.
	 * <p>
	 * If the image data cannot be loaded a dialog shows up telling the user
	 * about the problem, and offering to start Mastodon on substituted dummy
	 * image data. If the user declines, a {@link SpimDataException} is thrown.
	 * <p>
	 * The GUI state is restored.
	 * 
	 * @param mastodonFile
	 *            path to a Mastodon file
	 * @param context
	 *            the current context.
	 * 
	 * @return the loaded {@link ProjectModel}.
	 * @throws IOException
	 *             if the project points to a regular image file for image data,
	 *             and that file cannot be opened properly, or if there is a
	 *             problem loading the model data, or if there is a problem
	 *             reading the GUI state.
	 * @throws SpimDataException
	 *             if the project points to a BDV file that cannot be opened,
	 *             and the user declined to substitute a dummy dataset.
	 */
	public static synchronized ProjectModel openWithDialog( final String mastodonFile, final Context context ) throws IOException, SpimDataException
	{
		final MamutProject project = MamutProjectIO.load( mastodonFile );
		return openWithDialog( project, context );
	}


	/**
	 * Opens a project interactively from a specified project object.
	 * <p>
	 * If the image data cannot be loaded a dialog shows up telling the user
	 * about the problem, and offering to start Mastodon on substituted dummy
	 * image data. If the user declines, a {@link SpimDataException} is thrown.
	 * <p>
	 * The GUI state is restored.
	 * 
	 * @param project
	 *            the object describing the project on disk.
	 * @param context
	 *            the current context.
	 * 
	 * @return the loaded {@link ProjectModel}.
	 * @throws IOException
	 *             if the project points to a regular image file for image data,
	 *             and that file cannot be opened properly, or if there is a
	 *             problem loading the model data, or if there is a problem
	 *             reading the GUI state.
	 * @throws SpimDataException
	 *             if the project points to a BDV file that cannot be opened,
	 *             and the user declined to substitute a dummy dataset.
	 */
	public static synchronized ProjectModel openWithDialog( final MamutProject project, final Context context ) throws IOException, SpimDataException
	{
		return openWithDialog( project, context, null );
	}

	/**
	 * Opens a project interactively from a Mastodon file.
	 * <p>
	 * If the image data cannot be loaded a dialog shows up telling the user
	 * about the problem, and offering to start Mastodon on substituted dummy
	 * image data.
	 * <p>
	 * The GUI state is restored.
	 * 
	 * @param mastodonFile
	 *            path to a Mastodon file
	 * @param context
	 *            the current context.
	 * @param parentComponent
	 *            a component to use as parent to show dialogs during opening.
	 *            Can be <code>null</code>.
	 * @throws IOException
	 *             if the project points to a regular image file for image data,
	 *             and that file cannot be opened properly and the user declined
	 *             to substitute dummy data; or if there is a problem loading
	 *             the model data; or if there is a problem reading the GUI
	 *             state.
	 * @throws SpimDataException
	 *             if the project points to a BDV file that cannot be opened,
	 *             and the user declined to substitute a dummy dataset.
	 * @return the loaded {@link ProjectModel}.
	 */
	public static synchronized ProjectModel openWithDialog( final String mastodonFile, final Context context, final Component parentComponent ) throws IOException, SpimDataException
	{
		final MamutProject project = MamutProjectIO.load( mastodonFile );
		return openWithDialog( project, context, parentComponent );
	}

	/**
	 * Opens a project interactively from a project object.
	 * <p>
	 * If the image data cannot be loaded a dialog shows up telling the user
	 * about the problem, and offering to start Mastodon on substituted dummy
	 * image data.
	 * <p>
	 * The GUI state is restored.
	 * 
	 * @param project
	 *            the object describing the project on disk.
	 * @param context
	 *            the current context.
	 * @param parentComponent
	 *            a component to use as parent to show dialogs during opening.
	 *            Can be <code>null</code>.
	 * @throws IOException
	 *             if the project points to a regular image file for image data,
	 *             and that file cannot be opened properly and the user declined
	 *             to substitute dummy data; or if there is a problem loading
	 *             the model data; or if there is a problem reading the GUI
	 *             state.
	 * @throws SpimDataException
	 *             if the project points to a BDV file that cannot be opened,
	 *             and the user declined to substitute a dummy dataset.
	 * @return the loaded {@link ProjectModel}.
	 */
	public static synchronized ProjectModel openWithDialog( final MamutProject project, final Context context, final Component parentComponent ) throws IOException, SpimDataException
	{
		try
		{
			return open( project, context, true, false );
		}
		catch ( final SpimDataIOException | RuntimeException e )
		{
			if ( getUserPermissionToOpenDummyData( project, e, parentComponent ) )
				return open( project, context, true, true );

			throw e;
		}
	}

	/**
	 * Loads the image data stored in a project, and wraps in a
	 * {@link SharedBigDataViewerData}.
	 * 
	 * @param project
	 *            the project.
	 * @param authorizeSubstituteDummyData
	 *            if <code>true</code>, and if the image data cannot be loaded,
	 *            a dummy image data will be substituted. In that case a
	 *            {@link SpimDataException} is never thrown.
	 * @return a new {@link SharedBigDataViewerData}.
	 * @throws IOException
	 *             if the project points to a regular image file for image data,
	 *             and that file cannot be opened properly.
	 * @throws SpimDataException
	 *             if the project points to a BDV file for image data, and that
	 *             BDV cannot be opened properly, and
	 *             <code>authorizeSubstituteDummyData</code> is false.
	 */
	public static SharedBigDataViewerData loadImageData( final MamutProject project, final boolean authorizeSubstituteDummyData ) throws SpimDataException, IOException
	{
		// Check to what kind of data points the image file.
		final String canonicalPath = project.getDatasetXmlFile().getAbsolutePath();

		if ( DummySpimData.isDummyString( canonicalPath ) )
		{
			// Opening a project with standard BDV (or DUMMY) image data
			return SharedBigDataViewerData.fromDummyFilename( canonicalPath );
		}
		else if ( !canonicalPath.endsWith( ".xml" ) )
		{
			// Opening a project with a path to a regular image.
			final ImagePlus imp;
			// Do we have the ImagePlus already in memory?
			if ( project instanceof MamutImagePlusProject )
			{
				imp = ( ( MamutImagePlusProject ) project ).getImagePlus();
			}
			else
			{
				// Assume the path points to a plain image file.
				imp = IJ.openImage( canonicalPath );
				// If it does not work tell the user.
				if ( imp == null )
					throw new IOException( "Cannot open image " + canonicalPath );
			}
			return SharedBigDataViewerData.fromImagePlus( imp );
		}
		else
		{
			// Opening a project that points to a BDV file.

			// Try to open a BDV file. If it fails, substitute dummy data.
			if ( authorizeSubstituteDummyData )
				return openDummyImageData( project );

			// Try to open a BDV file. If it fails, crash.
			return SharedBigDataViewerData.fromSpimDataXmlFile( project.getDatasetXmlFile().getAbsolutePath() );
		}
	}

	/**
	 * Loads a {@link Model} from a project file.
	 * 
	 * @param project
	 *            the project to load from.
	 * @param context
	 *            the current context, used to get feature serializers.
	 * @return a new model.
	 * @throws IOException
	 *             if there is a problem reading the project.
	 */
	public static final Model loadModel( final MamutProject project, final Context context ) throws IOException
	{
		final Model model = new Model( project.getSpaceUnits(), project.getTimeUnits() );
		final boolean isNewProject = project.getProjectRoot() == null;
		if ( !isNewProject )
		{
			try (final MamutProject.ProjectReader reader = project.openForReading())
			{
				final FileIdToGraphMap< Spot, Link > idmap = model.loadRaw( reader );
				// Load features.
				MamutRawFeatureModelIO.deserialize(
						context,
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
		return model;
	}

	/**
	 * Recreates the GUI configuration saved in the specified project. When
	 * calling this method, windows are created and shown on the display.
	 * 
	 * @param project
	 *            the project to read from.
	 * @param windowManager
	 *            a window manager instance, used to create windows.
	 * @throws IOException
	 *             if there is a problem reading the project.
	 */
	public static final void loadGUI( final MamutProject project, final WindowManager windowManager ) throws IOException
	{
		final boolean isNewProject = project.getProjectRoot() == null;
		if ( isNewProject )
			return;

		try (final MamutProject.ProjectReader reader = project.openForReading())
		{
			try
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
			catch ( final FileNotFoundException fnfe )
			{
				// Ignore missing gui file.
			}
		}
	}

	/**
	 * Shows an dialog the explains to the user why the image data could not
	 * been loaded, and offers to open Mastodon with dummy image data.
	 */
	private static boolean getUserPermissionToOpenDummyData( final MamutProject project, final Exception e, final Component parentComponent )
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
		final int dialogResult = JOptionPane.showOptionDialog( parentComponent, message, title, JOptionPane.YES_NO_OPTION,
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

	private static SharedBigDataViewerData openDummyImageData( final MamutProject project )
	{
		try
		{
			final String backupDatasetXml = originalOrBackupDatasetXml( project ).getAbsolutePath();
			return SharedBigDataViewerData.createDummyDataFromSpimDataXml( backupDatasetXml );
		}
		catch ( final Throwable e )
		{
			return simpleDummyData( project );
		}
	}

	static File originalOrBackupDatasetXml( final MamutProject project )
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

	private static SharedBigDataViewerData simpleDummyData( final MamutProject project )
	{
		try (final MamutProject.ProjectReader reader = project.openForReading())
		{
			final Model model = new Model( project.getSpaceUnits(), project.getTimeUnits() );
			model.loadRaw( reader );
			final String requiredImageSizeAsString = requiredImageSizeAsString( model );
			return SharedBigDataViewerData.fromDummyFilename( requiredImageSizeAsString );
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
}
