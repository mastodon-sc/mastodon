package org.mastodon.io;

import static org.mastodon.io.BaseProjectSaver.GUI_TAG;
import static org.mastodon.io.BaseProjectSaver.WINDOWS_TAG;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.mastodon.graph.Edge;
import org.mastodon.graph.Vertex;
import org.mastodon.graph.io.RawGraphIO.FileIdToGraphMap;
import org.mastodon.mamut.io.project.MamutImagePlusProject;
import org.mastodon.mamut.io.project.MamutProject;
import org.mastodon.mamut.io.project.MamutProject.ProjectReader;
import org.mastodon.mamut.io.project.MamutProjectIO;
import org.mastodon.model.MastodonModel;
import org.mastodon.model.app.BdvAppModel;
import org.mastodon.util.DummySpimData;
import org.mastodon.views.bdv.SharedBigDataViewerData;
import org.scijava.Context;

import ij.IJ;
import ij.ImagePlus;
import mpicbg.spim.data.SpimDataException;
import mpicbg.spim.data.generic.sequence.BasicViewSetup;

/**
 * Generic base class for loading Mastodon-based projects.
 *
 * @param <AM>
 *            the AppModel type
 * @param <M>
 *            the DataModel type
 * @param <V>
 *            the graph vertex type
 * @param <E>
 *            the graph edge type
 */
public abstract class BaseProjectLoader<
		AM extends BdvAppModel< AM, M, ?, V, E >,
		M extends MastodonModel< ?, V, E >,
		V extends Vertex< E >,
		E extends Edge< V > >
{

	protected final AppIOAdapter< AM, M, V, E > adapter;

	protected BaseProjectLoader( final AppIOAdapter< AM, M, V, E > adapter )
	{
		this.adapter = adapter;
	}

	/**
	 * Opens a project. The GUI state is not restored.
	 *
	 * @param projectFile
	 *            path to a project file.
	 * @param context
	 *            the current context.
	 * @return the loaded app model.
	 * @throws IOException
	 *             if there is a problem loading the project.
	 * @throws SpimDataException
	 *             if there is a problem loading image data.
	 */
	public AM open( final String projectFile, final Context context ) throws IOException, SpimDataException
	{
		final MamutProject project = MamutProjectIO.load( projectFile );
		return open( project, context, false, false );
	}

	/**
	 * Opens a project. The GUI state is not restored.
	 *
	 * @param project
	 *            the project object describing the project on disk.
	 * @param context
	 *            the current context.
	 * @return the loaded app model.
	 * @throws IOException
	 *             if there is a problem loading the project.
	 * @throws SpimDataException
	 *             if there is a problem loading image data.
	 */
	public AM open( final MamutProject project, final Context context ) throws IOException, SpimDataException
	{
		return open( project, context, false, false );
	}

	/**
	 * Opens a specified project.
	 *
	 * @param projectFile
	 *            path to a project file.
	 * @param context
	 *            the current context.
	 * @param restoreGUIState
	 *            if <code>true</code>, the GUI state will be restored.
	 * @param authorizeSubstituteDummyData
	 *            if <code>true</code>, and if the image data cannot be loaded,
	 *            a dummy image data will be substituted.
	 * @return the loaded app model.
	 * @throws IOException
	 *             if there is a problem loading the project.
	 * @throws SpimDataException
	 *             if there is a problem loading image data.
	 */
	public AM open( final String projectFile, final Context context, final boolean restoreGUIState, final boolean authorizeSubstituteDummyData ) throws IOException, SpimDataException
	{
		final MamutProject project = MamutProjectIO.load( projectFile );
		return open( project, context, restoreGUIState, authorizeSubstituteDummyData );
	}

	/**
	 * Opens a specified project.
	 *
	 * @param project
	 *            the project object describing the project on disk.
	 * @param context
	 *            the current context.
	 * @param restoreGUIState
	 *            if <code>true</code>, the GUI state will be restored.
	 * @param authorizeSubstituteDummyData
	 *            if <code>true</code>, and if the image data cannot be loaded,
	 *            a dummy image data will be substituted.
	 * @return the loaded app model.
	 * @throws IOException
	 *             if there is a problem loading the project.
	 * @throws SpimDataException
	 *             if there is a problem loading image data.
	 */
	public AM open( final MamutProject project, final Context context, final boolean restoreGUIState, final boolean authorizeSubstituteDummyData ) throws IOException, SpimDataException
	{
		// Load image data.
		final SharedBigDataViewerData imageData = loadImageData( project, authorizeSubstituteDummyData );

		// Try to read units from spimData if they are not present.
		if ( project.getSpaceUnits() == null )
		{
			project.setSpaceUnits(
					imageData.getSpimData().getSequenceDescription().getViewSetupsOrdered().stream()
							.filter( BasicViewSetup::hasVoxelSize )
							.map( setup -> setup.getVoxelSize().unit() )
							.findFirst()
							.orElse( "pixel" ) );
		}

		if ( project.getTimeUnits() == null )
		{
			project.setTimeUnits( "frame" );
		}

		// Load model.
		final M model = loadModel( project, context );

		// Build app model.
		final AM appModel = adapter.createAppModel( context, model, imageData, project );

		// Build the branch graph now.
		adapter.syncBranchGraph( model );

		// Restore GUI state.
		if ( restoreGUIState )
			loadGUI( project, appModel );

		return appModel;
	}

	/**
	 * Loads the data model stored in a project.
	 *
	 * @param project
	 *            the project.
	 * @param context
	 *            the SciJava context.
	 * @return the loaded data model.
	 * @throws IOException
	 */
	public M loadModel( final MamutProject project, final Context context ) throws IOException
	{
		final M model = adapter.initializeNewModel( project );
		final boolean isNewProject = project.getProjectRoot() == null;
		if ( !isNewProject )
		{
			try (final ProjectReader reader = project.openForReading())
			{
				final FileIdToGraphMap< V, E > idmap = adapter.loadRawGraphModel( model, reader );
				adapter.deserializeFeatureModel( context, model, idmap, reader );
			}
			catch ( final ClassNotFoundException e )
			{
				e.printStackTrace();
			}
		}
		adapter.setSavePoint( model );
		adapter.declareDefaultFeatures( model );
		return model;
	}

	/**
	 * Loads the image data stored in a project, and wraps it in a
	 * {@link SharedBigDataViewerData}.
	 *
	 * @param project
	 *            the project.
	 * @param authorizeSubstituteDummyData
	 *            if <code>true</code>, and if the image data cannot be loaded,
	 *            a dummy image data will be substituted.
	 * @return a new {@link SharedBigDataViewerData}.
	 * @throws IOException
	 *             if there is a problem loading image data.
	 * @throws SpimDataException
	 *             if there is a problem loading a BDV file.
	 */
	public SharedBigDataViewerData loadImageData( final MamutProject project, final boolean authorizeSubstituteDummyData ) throws SpimDataException, IOException
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
	 * Recreates the GUI configuration saved in the specified project. When
	 * calling this method, windows are created and shown on the display.
	 *
	 * @param project
	 *            the project to read from.
	 * @param appModel
	 *            the application model, used to restore the GUI state into.
	 * @throws IOException
	 *             if there is a problem reading the project.
	 */
	public void loadGUI( final MamutProject project, final AM appModel ) throws IOException
	{
		final boolean isNewProject = project.getProjectRoot() == null;
		if ( isNewProject )
			return;

		try ( final MamutProject.ProjectReader reader = project.openForReading() )
		{
			try
			{
				final SAXBuilder sax = new SAXBuilder();
				Document guiDoc;
				try ( InputStream inputStream = reader.getGuiInputStream() )
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

				ViewStateXMLSerialization.fromXml( windowsEl, appModel );
			}
			catch ( final FileNotFoundException fnfe )
			{
				// Ignore missing gui file.
			}
		}
	}

	/**
	 * Opens dummy image data, either from backup dataset XML or by inferring
	 * from the model.
	 */
	protected SharedBigDataViewerData openDummyImageData( final MamutProject project ) throws IOException, SpimDataException
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

	/**
	 * Creates simple dummy image data by inferring dimensions from the model.
	 */
	private SharedBigDataViewerData simpleDummyData( final MamutProject project ) throws IOException
	{
		try ( final MamutProject.ProjectReader reader = project.openForReading() )
		{
			// Load without context
			final M model = loadModel( project, null );
			final String requiredImageSizeAsString = adapter.requiredImageSizeAsString( model );
			return SharedBigDataViewerData.fromDummyFilename( requiredImageSizeAsString );
		}
		catch ( final IOException e )
		{
			throw new RuntimeException( "Could not create dummy image data", e );
		}
	}


	/**
	 * Gets the original dataset XML file or creates a temporary copy from backup.
	 */
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
		catch ( final IOException | NullPointerException e )
		{
			return null;
		}
	}

	/**
	 * Copies the backup dataset XML from the project to a temporary file.
	 */
	private static File copyBackupDatasetXmlToTmpFile( final MamutProject project ) throws IOException
	{
		try ( final MamutProject.ProjectReader reader = project.openForReading();
				final InputStream is = reader.getBackupDatasetXmlInputStream() )
		{
			final File tmp = File.createTempFile( "mastodon-dataset-xml-backup", ".xml" );
			tmp.deleteOnExit();
			Files.copy( is, tmp.toPath(), StandardCopyOption.REPLACE_EXISTING );
			return tmp;
		}
	}

	/**
	 * Rounds up a double value to the nearest long.
	 */
	protected static long roundUp( final double x )
	{
		return ( long ) Math.ceil( x );
	}
}
