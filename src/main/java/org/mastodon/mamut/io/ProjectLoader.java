/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2023 Tobias Pietzsch, Jean-Yves Tinevez
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
package org.mastodon.mamut.io;

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
import org.mastodon.util.DummySpimData;
import org.mastodon.views.bdv.SharedBigDataViewerData;
import org.scijava.Context;

import ij.IJ;
import ij.ImagePlus;
import mpicbg.spim.data.SpimDataException;
import mpicbg.spim.data.generic.sequence.BasicViewSetup;

/**
 * Static methods to open a Mastodon Mamut project.
 */
public class ProjectLoader
{

	static final String GUI_TAG = "MamutGui";

	static final String WINDOWS_TAG = "Windows";

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
		// Load image data.
		final SharedBigDataViewerData imageData = loadImageData( project, authorizeSubstituteDummyData );

		// Try to read units from spimData is they are not present.
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
		final Model model = loadModel( project, context );

		// Build app model.
		final ProjectModel appModel = ProjectModel.create( context, model, imageData, project );

		// Build the branch graph now.
		appModel.getBranchGraphSync().sync();

		// Restore GUI state.
		if ( restoreGUIState )
			loadGUI( project, appModel.getWindowManager() );

		return appModel;
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

				MamutViewStateXMLSerialization.fromXml( windowsEl, windowManager );
			}
			catch ( final FileNotFoundException fnfe )
			{
				// Ignore missing gui file.
			}
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
