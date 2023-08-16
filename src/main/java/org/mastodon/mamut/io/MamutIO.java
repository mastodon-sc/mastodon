package org.mastodon.mamut.io;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.mastodon.graph.io.RawGraphIO.FileIdToGraphMap;
import org.mastodon.mamut.MamutAppModel;
import org.mastodon.mamut.WindowManager;
import org.mastodon.mamut.feature.MamutRawFeatureModelIO;
import org.mastodon.mamut.io.project.MamutImagePlusProject;
import org.mastodon.mamut.io.project.MamutProject;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.Spot;
import org.mastodon.ui.keymap.KeymapManager;
import org.mastodon.util.DummySpimData;
import org.mastodon.views.bdv.SharedBigDataViewerData;
import org.scijava.Context;
import org.scijava.ui.behaviour.KeyPressedManager;

import ij.IJ;
import ij.ImagePlus;

public class MamutIO
{

	private static final String GUI_TAG = "MamutGui";
	private static final String WINDOWS_TAG = "Windows";

	public static MamutAppModel open( final MamutProject project, final Context context ) throws IOException
	{
		return open( project, context, false );
	}

	public static MamutAppModel open( final MamutProject project, final Context context, final boolean restoreGUIState ) throws IOException
	{
		final Model model = loadModel( project, context );
		final SharedBigDataViewerData imageData = loadImageData( project );
		final KeyPressedManager keyPressedManager = new KeyPressedManager();
		final KeymapManager keymapManager = new KeymapManager();
		final MamutAppModel appModel = new MamutAppModel( context, model, imageData, keyPressedManager, keymapManager );

		if ( restoreGUIState )
			loadGUI( project, appModel.getWindowManager() );

		return appModel;
	}

	public static SharedBigDataViewerData loadImageData( final MamutProject project )
	{
		// Check whether the project points to a BDV file.
		final String canonicalPath = project.getDatasetXmlFile().getAbsolutePath();
		if ( !canonicalPath.endsWith( ".xml" ) && !canonicalPath.endsWith( DummySpimData.DUMMY ) )
		{
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
		}
		else
		{
			// Opening a project with standard BDV (or DUMMY) image data
		}

		/*
		 * Move this to AppModel
		 */

//		// Prepare image data.
//		// Prepare base view options.
//		final ViewerOptions options = ViewerOptions.options()
//				.shareKeyPressedEvents( windowManager.getKeyPressedManager() )
//				.msgOverlay( new MessageOverlayAnimator( 1500, 0.005, 0.02 ) );
//
//		final RequestRepaint requestRepaint = () -> windowManager.forEachBdvView( MamutViewBdv::requestRepaint );

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

}
