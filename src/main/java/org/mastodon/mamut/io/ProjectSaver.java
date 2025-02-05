/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2025 Tobias Pietzsch, Jean-Yves Tinevez
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

import static org.mastodon.app.MastodonIcons.SAVE_ICON_MEDIUM;
import static org.mastodon.mamut.io.ProjectLoader.GUI_TAG;
import static org.mastodon.mamut.io.ProjectLoader.WINDOWS_TAG;
import static org.mastodon.mamut.io.project.MamutProjectIO.MAMUTPROJECT_VERSION_ATTRIBUTE_CURRENT;
import static org.mastodon.mamut.io.project.MamutProjectIO.MAMUTPROJECT_VERSION_ATTRIBUTE_NAME;

import java.awt.Component;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.mastodon.app.MastodonIcons;
import org.mastodon.graph.io.RawGraphIO.GraphToFileIdMap;
import org.mastodon.mamut.MainWindow;
import org.mastodon.mamut.MamutViews;
import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.WindowManager;
import org.mastodon.mamut.feature.MamutRawFeatureModelIO;
import org.mastodon.mamut.io.project.MamutImagePlusProject;
import org.mastodon.mamut.io.project.MamutProject;
import org.mastodon.mamut.io.project.MamutProject.ProjectWriter;
import org.mastodon.mamut.io.project.MamutProjectIO;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.views.MamutViewFactory;
import org.mastodon.mamut.views.bdv.MamutViewBdv;
import org.mastodon.ui.util.ExtensionFileFilter;
import org.mastodon.ui.util.FileChooser;
import org.mastodon.ui.util.FileChooser.SelectionMode;
import org.mastodon.util.BDVImagePlusExporter;
import org.mastodon.views.bdv.SharedBigDataViewerData;
import org.scijava.Context;

import ij.gui.ImageWindow;

/**
 * Static methods to save project to a Mastodon Mamut file.
 */
public class ProjectSaver
{

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
	public static synchronized void saveProjectAs( final ProjectModel appModel, final Component parentComponent )
	{
		final MamutProject project = appModel.getProject();
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
					/*
					 * If the user chose to resave to BDV, then we make a new
					 * project out of the new data, save it and reopen it.
					 */
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
					final File file = FileChooser.chooseFile( true,
							parentComponent,
							projectRoot,
							new ExtensionFileFilter( "mastodon" ),
							"Save Mastodon Project",
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
	public static void saveProject( final ProjectModel appModel, final Component parentComponent )
	{
		final MamutProject project = appModel.getProject();
		// If a Mastodon project was not yet created, ask to create one.
		if ( project.getProjectRoot() == null )
		{
			saveProjectAs( appModel, parentComponent );
			return;
		}

		try
		{
			saveProject( appModel.getProject().getProjectRoot(), appModel );
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
	 * Saves the specified project to the specified file. The file should be a
	 * path ending in <code>.mastodon</code> (but folders from previous versions
	 * are supported).
	 * 
	 * @param saveTo
	 *            the file to save the project to.
	 * @param appModel
	 *            the project model.
	 * @throws IOException
	 *             if there is an error writing to the file.
	 */
	public static synchronized void saveProject( final File saveTo, final ProjectModel appModel ) throws IOException
	{
		// Current project.
		final MamutProject project = appModel.getProject();
		final File tmpDatasetXml = ProjectLoader.originalOrBackupDatasetXml( project );

		// Possibly update project root.
		project.setProjectRoot( saveTo );
		try (final MamutProject.ProjectWriter writer = project.openForWriting())
		{
			MamutProjectIO.save( project, writer );
			// Synchronize branch graph with main graph before saving. This is required to make saved state consistent.
			appModel.getBranchGraphSync().sync();
			final Model model = appModel.getModel();
			// Save Raw Graph Model
			final GraphToFileIdMap< Spot, Link > idmap = model.saveRaw( writer );
			// Serialize feature model.
			MamutRawFeatureModelIO.serialize( appModel.getContext(), model, idmap, writer );
			// Serialize GUI state.
			saveGUI( writer, appModel.getWindowManager() );
			// Save a copy of the Spim Data Xml File
			saveBackupDatasetXml( tmpDatasetXml, writer );
			// Set save point.
			model.setSavePoint();
		}

		// Save BDV settings.
		// Imperfect because a full saving requires have a view opened,
		// which is not necessarily our case.
		final SharedBigDataViewerData sbdv = appModel.getSharedBdvData();
		if ( sbdv.getProposedSettingsFile() != null )
		{
			final String settingsFile = sbdv.getProposedSettingsFile().getAbsolutePath();
			final AtomicBoolean alreadySaved = new AtomicBoolean( false );
			appModel.getWindowManager().forEachView( MamutViewBdv.class, ( v ) -> {
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
	private static void saveGUI( final ProjectWriter writer, final WindowManager windowManager ) throws IOException
	{
		final Element guiRoot = new Element( GUI_TAG );
		guiRoot.setAttribute( MAMUTPROJECT_VERSION_ATTRIBUTE_NAME, MAMUTPROJECT_VERSION_ATTRIBUTE_CURRENT );
		final Element windows = new Element( WINDOWS_TAG );
		final MamutViews viewFactories = windowManager.getViewFactories();
		windowManager.forEachView( ( view ) -> {
			@SuppressWarnings( "rawtypes" )
			final MamutViewFactory factory = viewFactories.getFactory( view.getClass() );
			@SuppressWarnings( "unchecked" )
			final Element element = MamutViewStateXMLSerialization.toXml( factory.getGuiState( view ) );
			windows.addContent( element );

		} );
		guiRoot.addContent( windows );
		final Document doc = new Document( guiRoot );
		final XMLOutputter xout = new XMLOutputter( Format.getPrettyFormat() );
		try (OutputStream outputStream = writer.getGuiOutputStream())
		{
			xout.output( doc, outputStream );
		}
	}

	/**
	 * Saves a copy of the dataset.xml (if there is any) to the projection
	 * location.
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

	static final String EXT_DOT_MASTODON = ".mastodon";

	static String stripExtensionIfPresent( final String fn, final String ext )
	{
		return fn.endsWith( ext )
				? fn.substring( 0, fn.length() - ext.length() )
				: fn;
	}

	private static void saveAndReopenImagePlusProject( final ProjectModel appModel, final Component parentComponent ) throws IOException
	{
		final MamutImagePlusProject project = ( MamutImagePlusProject ) appModel.getProject();

		// Export imp to BDV.
		final String projectRoot = getProposedProjectRoot( project );
		final int n = projectRoot.indexOf( '.' );
		final String proposedXmlFile = projectRoot.subSequence( 0, n ).toString() + ".xml";
		final File bdvFile = BDVImagePlusExporter.export( project.getImagePlus(), proposedXmlFile );

		/*
		 * Create a settings file for the BDV file with what we can put in,
		 * inferred from the existing SharedBigDataViewerData created from the
		 * imp.
		 */
		final Element root = new Element( "Settings" );
		final SharedBigDataViewerData sbdv = appModel.getSharedBdvData();
		root.addContent( sbdv.getManualTransformation().toXml() );
		root.addContent( sbdv.toXmlSetupAssignments() );
		root.addContent( sbdv.getBookmarks().toXml() );
		final Document doc = new Document( root );
		final XMLOutputter xout = new XMLOutputter( Format.getPrettyFormat() );
		final String xmlFilename = bdvFile.getAbsolutePath();
		final String settings = xmlFilename.substring( 0, xmlFilename.length() - ".xml".length() )
				+ ".settings" + ".xml";
		xout.output( doc, new FileWriter( settings ) );

		// Make a new project pointing to the new BDV file.
		final MamutProject np = new MamutProject( new File( projectRoot ), bdvFile );
		np.setSpaceUnits( project.getSpaceUnits() );
		np.setTimeUnits( project.getTimeUnits() );

		// Remove listener to imp window closing.
		final ImageWindow window = project.getImagePlus().getWindow();
		if ( window != null )
		{
			for ( final WindowListener wl : window.getWindowListeners() )
				window.removeWindowListener( wl );

			window.addWindowListener( new WindowAdapter()
			{
				@Override
				public void windowClosing( final java.awt.event.WindowEvent e )
				{
					project.getImagePlus().close();
				}
			} );
		}

		// And now the weird part: we reopen the project we just created.
		final Context context = appModel.getContext();
		final Model model = appModel.getModel();
		final ProjectModel nmam = ProjectModel.create( context, model, sbdv, np );

		// Offer to save the new project.
		final File file = FileChooser.chooseFile( true,
				parentComponent,
				projectRoot,
				new ExtensionFileFilter( "mastodon" ),
				"Save Mastodon Project",
				FileChooser.DialogType.SAVE,
				SelectionMode.FILES_ONLY,
				SAVE_ICON_MEDIUM.getImage() );
		if ( file == null )
			return;

		try
		{
			saveProject( file, nmam );
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

		// Close the old one.
		appModel.close();

		// Show the UI for the new one.
		new MainWindow( nmam ).setVisible( true );
	}
}
