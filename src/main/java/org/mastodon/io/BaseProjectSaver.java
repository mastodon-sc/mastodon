package org.mastodon.io;

import static org.mastodon.app.MastodonIcons.SAVE_ICON_MEDIUM;
import static org.mastodon.mamut.io.project.MamutProjectIO.MAMUTPROJECT_VERSION_ATTRIBUTE_CURRENT;
import static org.mastodon.mamut.io.project.MamutProjectIO.MAMUTPROJECT_VERSION_ATTRIBUTE_NAME;

import java.awt.Component;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.mastodon.app.MastodonIcons;
import org.mastodon.graph.Edge;
import org.mastodon.graph.Vertex;
import org.mastodon.mamut.io.project.MamutImagePlusProject;
import org.mastodon.mamut.io.project.MamutProject;
import org.mastodon.mamut.io.project.MamutProject.ProjectWriter;
import org.mastodon.mamut.io.project.MamutProjectIO;
import org.mastodon.mamut.views.MamutViewFactory;
import org.mastodon.mamut.views.bdv.MamutViewBdv;
import org.mastodon.model.MastodonModel;
import org.mastodon.model.app.BdvAppModel;
import org.mastodon.model.app.WindowManager;
import org.mastodon.ui.util.ExtensionFileFilter;
import org.mastodon.ui.util.FileChooser;
import org.mastodon.ui.util.FileChooser.SelectionMode;
import org.mastodon.util.BDVImagePlusExporter;
import org.mastodon.views.bdv.SharedBigDataViewerData;
import org.scijava.Context;

import ij.gui.ImageWindow;

/**
 * Generic base class for saving Mastodon-based projects.
 *
 * @param <AM>
 *            the AppModel type. Here is extends BdvAppModel, because the
 *            concrete implementations we develop have all images. This should
 *            be generalized if we ever want to support Apps without image data.
 * @param <M>
 *            the DataModel type
 * @param <V>
 *            the graph vertex type
 * @param <E>
 *            the graph edge type
 */
public abstract class BaseProjectSaver<
		AM extends BdvAppModel< AM, M, ?, V, E >,
		M extends MastodonModel< ?, V, E >,
		V extends Vertex< E >,
		E extends Edge< V > >
{

	static final String GUI_TAG = "MamutGui";

	static final String WINDOWS_TAG = "Windows";

	protected final AppIOAdapter< AM, M, V, E > adapter;

	protected BaseProjectSaver( final AppIOAdapter< AM, M, V, E > adapter )
	{
		this.adapter = adapter;
	}

	/**
	 * Interactively saves the specified project. A dialog is shown prompting
	 * the user to a save path.
	 * <p>
	 * Must <b>not</b> be called on the EDT.
	 *
	 * @param appModel
	 *            the project model.
	 * @param parentComponent
	 *            a component to use as parent to show dialogs during opening.
	 *            Can be <code>null</code>.
	 */
	public synchronized void saveProjectAs( final AM appModel, final Component parentComponent )
	{
		final MamutProject project = adapter.getProject( appModel );
		final String projectRoot = getProposedProjectRoot( project );

		try
		{
			/*
			 * Check if the image data is based on a non-BDV image. If it's the
			 * case, offer to convert.
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
								parentComponent,
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
					saveAndReopenImagePlusProject( appModel, parentComponent );
					return;
				}
			}

			/*
			 * Ask for a file path to save to. We want to go on the EDT and keep
			 * a ref to what we will receive; we use a StringBuilder for that.
			 */
			final StringBuilder str = new StringBuilder();
			SwingUtilities.invokeAndWait( new Runnable()
			{
				@Override
				public void run()
				{
					final File file = FileChooser.chooseFile(
							true,
							parentComponent,
							projectRoot,
							new ExtensionFileFilter( adapter.getFileExtension() ),
							"Save " + adapter.getProjectTypeName(),
							FileChooser.DialogType.SAVE,
							SelectionMode.FILES_ONLY,
							SAVE_ICON_MEDIUM.getImage() );
					if ( file == null )
						return;
					str.append( file.getAbsolutePath() );
				}
			} );

			if ( str.length() == 0 ) // Abort
				return;

			try
			{
				saveProject( new File( str.toString() ), appModel );
			}
			catch ( final IOException e )
			{
				JOptionPane.showMessageDialog(
						parentComponent,
						"Could not save project:\n" + e.getMessage(),
						"Error writing to file",
						JOptionPane.ERROR_MESSAGE );
				e.printStackTrace();
			}
		}
		catch ( final InterruptedException | InvocationTargetException | IOException e )
		{
			JOptionPane.showMessageDialog(
					parentComponent,
					"Problem writing the project:\n" + e.getMessage(),
					"Error writing to file",
					JOptionPane.ERROR_MESSAGE );
			e.printStackTrace();
		}
	}

	/**
	 * Saves the specified project to the file specified in its
	 * {@link MamutProject} object.
	 *
	 * @param appModel
	 *            the project model.
	 * @param parentComponent
	 *            a component to use as parent to show dialogs during opening.
	 *            Can be <code>null</code>.
	 */
	public void saveProject( final AM appModel, final Component parentComponent )
	{
		final MamutProject project = adapter.getProject( appModel );
		// If a Mastodon project was not yet created, ask to create one.
		if ( project.getProjectRoot() == null )
		{
			saveProjectAs( appModel, parentComponent );
			return;
		}

		try
		{
			saveProject( project.getProjectRoot(), appModel );
		}
		catch ( final IOException e )
		{
			JOptionPane.showMessageDialog(
					parentComponent,
					"Could not save project:\n" + e.getMessage(),
					"Error writing to file",
					JOptionPane.ERROR_MESSAGE );
			e.printStackTrace();
		}
	}

	/**
	 * Saves the specified project to the specified file.
	 *
	 * @param saveTo
	 *            the file to save the project to.
	 * @param appModel
	 *            the project model.
	 * @throws IOException
	 *             if there is an error writing to the file.
	 */
	public synchronized void saveProject( final File saveTo, final AM appModel ) throws IOException
	{
		final MamutProject project = adapter.getProject( appModel );
		final File tmpDatasetXml = originalOrBackupDatasetXml( project );

		// Possibly update project root.
		project.setProjectRoot( saveTo );
		try ( final ProjectWriter writer = project.openForWriting() )
		{
			MamutProjectIO.save( project, writer );

			// Synchronize branch graph with main graph before saving.
			final M model = adapter.getDataModel( appModel );
			adapter.syncBranchGraph( model );

			// Save Raw Graph Model
			final GraphToFileIdMapMapping< V, E > idmap = new GraphToFileIdMapMapping<>(
					adapter.saveRawGraphModel( model, writer ) );

			// Serialize feature model.
			adapter.serializeFeatureModel(
					adapter.getWindowManager( appModel ).getContext(),
					model,
					idmap.getIdMap(),
					writer );

			// Serialize GUI state.
			saveGUI( writer, adapter.getWindowManager( appModel ) );

			// Save a copy of the Spim Data Xml File
			saveBackupDatasetXml( tmpDatasetXml, writer );

			// Set save point.
			adapter.setSavePoint( model );
		}

		// Save BDV settings.
		final SharedBigDataViewerData sbdv = appModel.imageData();
		if ( sbdv != null && sbdv.getProposedSettingsFile() != null )
		{
			final String settingsFile = sbdv.getProposedSettingsFile().getAbsolutePath();
			final AtomicBoolean alreadySaved = new AtomicBoolean( false );
			adapter.getWindowManager( appModel ).forEachView( MamutViewBdv.class, ( v ) -> {
				if ( !alreadySaved.get() )
				{
					try
					{
						sbdv.saveSettings( settingsFile, v.getViewerPanelMamut() );
						alreadySaved.set( true );
					}
					catch ( final IOException e )
					{
						e.printStackTrace();
					}
				}
			} );
			if ( !alreadySaved.get() )
				sbdv.saveSettings( settingsFile, null );
		}
	}

	/**
	 * Serialize window positions and states.
	 *
	 * @throws IOException
	 *             if an error occurs when writing to the GUI file.
	 */
	private void saveGUI( final ProjectWriter writer, final WindowManager< AM > windowManager ) throws IOException
	{
		final Element guiRoot = new Element( GUI_TAG );
		guiRoot.setAttribute( MAMUTPROJECT_VERSION_ATTRIBUTE_NAME, MAMUTPROJECT_VERSION_ATTRIBUTE_CURRENT );
		final Element windows = new Element( WINDOWS_TAG );
		final WindowManager< AM >.ViewFactories viewFactories = windowManager.getViewFactories();

		windowManager.forEachView( ( view ) -> {
			@SuppressWarnings( "rawtypes" )
			final MamutViewFactory factory = ( MamutViewFactory ) viewFactories.getFactory( view.getClass() );
			@SuppressWarnings( "unchecked" )
			final Element element = ViewStateXMLSerialization.toXml( factory.getGuiState( view ) );
			windows.addContent( element );
		} );

		guiRoot.addContent( windows );
		final Document doc = new Document( guiRoot );
		final XMLOutputter xout = new XMLOutputter( Format.getPrettyFormat() );
		try ( OutputStream outputStream = writer.getGuiOutputStream() )
		{
			xout.output( doc, outputStream );
		}
	}

	/**
	 * Gets the proposed project root directory.
	 */
	private String getProposedProjectRoot( final MamutProject project )
	{
		if ( project.getProjectRoot() != null )
			return project.getProjectRoot().getAbsolutePath();
		else
		{
			final File f = project.getDatasetXmlFile();
			final String fn = stripExtensionIfPresent( f.getName(), ".xml" );
			return new File( f.getParentFile(), fn + "." + adapter.getFileExtension() ).getAbsolutePath();
		}
	}

	/**
	 * Strips an extension from a filename if present.
	 */
	static String stripExtensionIfPresent( final String fn, final String ext )
	{
		return fn.endsWith( ext )
				? fn.substring( 0, fn.length() - ext.length() )
				: fn;
	}

	/**
	 * Handles the workflow for converting an ImagePlus project to BDV and saving.
	 */
	private void saveAndReopenImagePlusProject( final AM appModel, final Component parentComponent ) throws IOException
	{
		final MamutProject project = adapter.getProject( appModel );
		final MamutImagePlusProject impProject = ( MamutImagePlusProject ) project;

		// Export imp to BDV.
		final String projectRoot = getProposedProjectRoot( project );
		final int n = projectRoot.indexOf( '.' );
		final String proposedXmlFile = projectRoot.subSequence( 0, n ).toString() + ".xml";
		final File bdvFile = BDVImagePlusExporter.export( impProject.getImagePlus(), proposedXmlFile );

		// Create a settings file for the BDV file.
		final Element root = new Element( "Settings" );
		final SharedBigDataViewerData sbdv = appModel.imageData();
		root.addContent( sbdv.getManualTransformation().toXml() );
		root.addContent( sbdv.toXmlSetupAssignments() );
		root.addContent( sbdv.getBookmarks().toXml() );
		final Document doc = new Document( root );
		final XMLOutputter xout = new XMLOutputter( Format.getPrettyFormat() );
		final String xmlFilename = bdvFile.getAbsolutePath();
		final String settings = xmlFilename.substring( 0, xmlFilename.length() - ".xml".length() ) + ".settings.xml";
		xout.output( doc, new FileWriter( settings ) );

		// Make a new project pointing to the new BDV file.
		final MamutProject np = new MamutProject( new File( projectRoot ), bdvFile );
		np.setSpaceUnits( project.getSpaceUnits() );
		np.setTimeUnits( project.getTimeUnits() );

		// Remove listeners from ImagePlus window.
		final ImageWindow window = impProject.getImagePlus().getWindow();
		if ( window != null )
		{
			for ( final WindowListener wl : window.getWindowListeners() )
				window.removeWindowListener( wl );

			window.addWindowListener( new WindowAdapter()
			{
				@Override
				public void windowClosing( final java.awt.event.WindowEvent e )
				{
					impProject.getImagePlus().close();
				}
			} );
		}

		// Create new app model with converted data.
		final Context context = adapter.getWindowManager( appModel ).getContext();
		final M model = adapter.getDataModel( appModel );
		final AM newAppModel = adapter.createAppModel( context, model, sbdv, np );

		// Offer to save the new project.
		final File file = FileChooser.chooseFile(
				true,
				parentComponent,
				projectRoot,
				new ExtensionFileFilter( adapter.getFileExtension() ),
				"Save " + adapter.getProjectTypeName(),
				FileChooser.DialogType.SAVE,
				SelectionMode.FILES_ONLY,
				SAVE_ICON_MEDIUM.getImage() );

		if ( file == null )
			return;

		try
		{
			saveProject( file, newAppModel );
		}
		catch ( final IOException e )
		{
			JOptionPane.showMessageDialog(
					parentComponent,
					"Could not save project:\n" + e.getMessage(),
					"Error writing to file",
					JOptionPane.ERROR_MESSAGE );
			e.printStackTrace();
			return;
		}

		// Close the old one and show the new one.
		appModel.close();
		adapter.openMainWindow( newAppModel );
	}

	/**
	 * Wrapper to expose GraphToFileIdMap in a type-safe manner.
	 */
	private static class GraphToFileIdMapMapping< V extends Vertex< E >, E extends Edge< V > >
	{

		private final org.mastodon.graph.io.RawGraphIO.GraphToFileIdMap< V, E > idMap;

		GraphToFileIdMapMapping( final org.mastodon.graph.io.RawGraphIO.GraphToFileIdMap< V, E > idMap )
		{
			this.idMap = idMap;
		}

		org.mastodon.graph.io.RawGraphIO.GraphToFileIdMap< V, E > getIdMap()
		{
			return idMap;
		}
	}

	/**
	 * Saves a copy of the dataset.xml to the project location.
	 *
	 * @throws IOException
	 *             if there is an IO error when creating the backup.
	 */
	private static void saveBackupDatasetXml( final File tmpDatasetXml, final ProjectWriter projectWriter ) throws IOException
	{
		if ( tmpDatasetXml == null )
			return;

		try (OutputStream out = projectWriter.getBackupDatasetXmlOutputStream())
		{
			Files.copy( tmpDatasetXml.toPath(), out );
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
		catch ( final IOException | NullPointerException e )
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
}

