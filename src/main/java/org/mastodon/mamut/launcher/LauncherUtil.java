/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2024 Tobias Pietzsch, Jean-Yves Tinevez
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
package org.mastodon.mamut.launcher;

import static org.mastodon.app.MastodonIcons.LOAD_ICON_MEDIUM;
import static org.mastodon.app.MastodonIcons.NEW_ICON_MEDIUM;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.NoRouteToHostException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.function.Consumer;

import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.mastodon.app.MastodonIcons;
import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.io.ProjectLoader;
import org.mastodon.mamut.io.project.MamutProject;
import org.mastodon.mamut.io.project.MamutProjectIO;
import org.mastodon.ui.util.EverythingDisablerAndReenabler;
import org.mastodon.ui.util.ExtensionFileFilter;
import org.mastodon.ui.util.FileChooser;
import org.mastodon.ui.util.FileChooser.SelectionMode;
import org.mastodon.ui.util.XmlFileFilter;
import org.scijava.Context;
import org.scijava.ui.behaviour.util.RunnableAction;

import ch.systemsx.cisd.hdf5.exceptions.HDF5FileNotFoundException;
import mpicbg.spim.data.SpimDataException;
import mpicbg.spim.data.XmlKeys;
import mpicbg.spim.data.generic.AbstractSpimData;
import mpicbg.spim.data.generic.sequence.BasicViewSetup;
import mpicbg.spim.data.sequence.VoxelDimensions;
import net.imglib2.Dimensions;

public class LauncherUtil
{

	private static File proposedProjectRoot;

	private static MamutProject project;

	/**
	 * Interactively creates a new project prompting the user for a path to a
	 * BDV/XML file.
	 * <p>
	 * A dialog is shown to prompt the user for the path to the XML file. If the
	 * image data cannot be loaded a dialog shows up telling the user about the
	 * problem, and offering to start Mastodon on substituted dummy image data.
	 * 
	 * @param context
	 *            the current context.
	 * @param parentComponent
	 *            a component to use as parent to show dialogs during opening.
	 *            Can be <code>null</code>.
	 * @param errorConsumer
	 *            a consumer that will receive an user-readable error message if
	 *            something wrong happens.
	 * @return a new {@link ProjectModel} or <code>null</code> if the user
	 *         clicked cancel, or if the BDV file is faulty and the user
	 *         declined to substitute a dummy dataset.
	 */
	public static synchronized ProjectModel createProjectWithDialog( final Context context, final Component parentComponent, final Consumer< String > errorConsumer )
	{
		final File file = FileChooser.chooseFile(
				parentComponent,
				null,
				new XmlFileFilter(),
				"Open BigDataViewer File",
				FileChooser.DialogType.LOAD,
				NEW_ICON_MEDIUM.getImage() );
		if ( file == null )
			return null;

		return createProjectFromBdvFileWithDialog( file, context, parentComponent, errorConsumer );
	}

	/**
	 * Interactively creates a new project from a BDV/XML file.
	 * <p>
	 * If the specified BDV file cannot be loaded a dialog shows up telling the
	 * user about the problem, and offering to start Mastodon on substituted
	 * dummy image data.
	 *
	 * @param file
	 *            the BDX XML file.
	 * @param context
	 *            the current context.
	 * @param parentComponent
	 *            a component to use as parent to show dialogs during opening.
	 *            Can be <code>null</code>.
	 * @param errorConsumer
	 *            a consumer that will receive an user-readable error message if
	 *            something wrong happens.
	 * @return a new {@link ProjectModel}.
	 */
	public static ProjectModel createProjectFromBdvFileWithDialog( final File file, final Context context, final Component parentComponent, final Consumer< String > errorConsumer )
	{
		final MamutProject project = MamutProjectIO.fromBdvFile( file );
		return openWithDialog( project, context, parentComponent, errorConsumer );
	}

	/**
	 * Opens a project interactively, prompting the user for the project file.
	 * <p>
	 * If the image data cannot be loaded a dialog shows up telling the user
	 * about the problem, and offering to start Mastodon on substituted dummy
	 * image data.
	 * <p>
	 * The GUI state is restored.
	 * 
	 * @param context
	 *            the current context.
	 * @param parentComponent
	 *            a component to use as parent to show dialogs during opening.
	 *            Can be <code>null</code>.
	 * @param errorConsumer
	 *            a consumer that will receive an user-readable error message if
	 *            something wrong happens.
	 * @return the loaded {@link ProjectModel}, or <code>null</code> if the
	 *         image cannot be loaded and the user declined to substitute a
	 *         dummy dataset.
	 */
	public static final ProjectModel openWithDialog( final Context context, final Component parentComponent, final Consumer< String > errorConsumer )
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
		{
			errorConsumer.accept( "User canceled opening project." );
			return null;
		}

		proposedProjectRoot = file;
		MamutProject project = null;
		try
		{
			project = MamutProjectIO.load( file.getAbsolutePath() );
		}
		catch ( final IOException e )
		{
			errorConsumer.accept( "Problem opening project file " + file.getAbsolutePath() + ":" + e.getMessage() );
			return null;
		}
		return openWithDialog( project, context, parentComponent, errorConsumer );
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
	 * @param errorConsumer
	 *            a consumer that will receive an user-readable error message if
	 *            something wrong happens.
	 * @return the loaded {@link ProjectModel}.
	 * 
	 */
	public static synchronized ProjectModel openWithDialog( final String mastodonFile, final Context context, final Consumer< String > errorConsumer )
	{
		MamutProject project = null;
		try
		{
			project = MamutProjectIO.load( mastodonFile );
			return openWithDialog( project, context, errorConsumer );
		}
		catch ( final IOException e )
		{
			errorConsumer.accept( getProblemDescription( project, e ) );
		}
		return null;
	}

	/**
	 * Opens a project interactively from a specified project object.
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
	 * @param errorConsumer
	 *            a consumer that will receive an user-readable error message if
	 *            something wrong happens.
	 * @return the loaded {@link ProjectModel}, or <code>null</code> if the
	 *         image cannot be loaded and the user declined to substitute a
	 *         dummy dataset.
	 */
	public static synchronized ProjectModel openWithDialog( final MamutProject project, final Context context, final Consumer< String > errorConsumer )
	{
		return openWithDialog( project, context, null, errorConsumer );
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
	 * @param errorConsumer
	 *            a consumer that will receive an user-readable error message if
	 *            something wrong happens.
	 * @return the loaded {@link ProjectModel}.
	 */
	public static synchronized ProjectModel openWithDialog( final String mastodonFile, final Context context, final Component parentComponent, final Consumer< String > errorConsumer )
	{
		MamutProject project = null;
		try
		{
			project = MamutProjectIO.load( mastodonFile );
		}
		catch ( final IOException e )
		{
			final String errorMessage = "Unable to open project file " + mastodonFile + ":\n" + e.getMessage();
			errorConsumer.accept( errorMessage );
			return null;
		}
		return openWithDialog( project, context, parentComponent, errorConsumer );
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
	 * @param errorConsumer
	 *            a consumer that will receive an user-readable error message if
	 *            something wrong happens.
	 * @return the loaded {@link ProjectModel}, or <code>null</code> if the
	 *         image cannot be loaded and the user declined to substitute a
	 *         dummy dataset.
	 */
	public static synchronized ProjectModel openWithDialog( final MamutProject project, final Context context, final Component parentComponent, final Consumer< String > errorConsumer )
	{
		try
		{
			return ProjectLoader.open( project, context, true, false );
		}
		catch ( final SpimDataException | IOException | RuntimeException e )
		{
			if ( getUserPermissionToOpenDummyData( project, e, parentComponent ) )
			{
				try
				{
					return ProjectLoader.open( project, context, true, true );
				}
				catch ( final Exception e1 )
				{
					final String errorMsg = getProblemDescription( project, e1 );
					errorConsumer.accept( errorMsg );
				}
			}
			else
			{
				final String errorMsg = getProblemDescription( project, e );
				errorConsumer.accept( errorMsg );
			}
			return null;
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

	/**
	 * To be used when <b>opening</b> a Mastodon file. Generates a user-readable
	 * error message from the specified exception that was triggered when
	 * loading the specified project.
	 * 
	 * @param project
	 *            the project.
	 * @param e
	 *            the exception.
	 * @return an error message.
	 */
	public static String getProblemDescription( final MamutProject project, final Exception e )
	{
		File datasetXml = new File( "" );
		if ( project != null )
		{
			datasetXml = project.getDatasetXmlFile();
			if ( !datasetXml.exists() )
				return "The BDV image XML file was not found:\n" + datasetXml;
		}

		if ( FileNotFoundException.class.isInstance( e.getCause() ) )
		{
			/*
			 * Local or remote XML file not found (happens with remote opening
			 * when the server is down).
			 */
			return "Could not find the image data file:\n" + e.getCause().getMessage();
		}
		else if ( HDF5FileNotFoundException.class.isInstance( e ) )
		{
			/*
			 * Local file but the H5 file cannot be found.
			 */
			return "Error in the BDV XML file:" + datasetXml
					+ "\nCould not find the HDF5 image data file:\n"
					+ e.getMessage();
		}
		else if ( UnknownHostException.class.isInstance( e.getCause() ) )
		{
			/*
			 * Trying to create a new project with a remove server that unknown.
			 */
			return "Unknown host " + errorMessageUnknownHost( datasetXml, e.getCause().getMessage() );
		}
		else if ( NoRouteToHostException.class.isInstance( e.getCause() ) )
		{
			/*
			 * Trying to load or create a project with a remote image data
			 * stored on a server that we cannot access to now (my computer has
			 * no internet).
			 */
			return "Cannot access remote host " + errorMessageUnknownHost( datasetXml, e.getCause().getMessage() );
		}

		// Everything else.
		e.printStackTrace();
		return e.getMessage();
	}

	private static String errorMessageUnknownHost( final File datasetXml, final String hostError )
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
			return baseUrl + "\n" + hostError;
		}
		catch ( final Exception e )
		{
			return "and unparsable dataset file: " + e.getMessage();
		}
	}

	static final void showHelp( final URL helpURL, final String title, final Component parent )
	{

		final JEditorPane editorPane = new JEditorPane();
		editorPane.setBorder( BorderFactory.createEmptyBorder( 5, 5, 5, 5 ) );
		editorPane.setEditable( false );
		editorPane.addHyperlinkListener( new HyperlinkListener()
		{
			@Override
			public void hyperlinkUpdate( final HyperlinkEvent e )
			{
				if ( e.getEventType() == HyperlinkEvent.EventType.ACTIVATED && Desktop.isDesktopSupported() )
				{
					try
					{
						Desktop.getDesktop().browse( e.getURL().toURI() );
					}
					catch ( IOException | URISyntaxException e1 )
					{
						e1.printStackTrace();
					}
				}
			}
		} );

		try

		{
			editorPane.setPage( helpURL );
		}
		catch (

		final IOException e )
		{
			editorPane.setText( "Attempted to read a bad URL: " + helpURL );
		}

		final JScrollPane editorScrollPane = new JScrollPane( editorPane );

		editorScrollPane.setVerticalScrollBarPolicy( JScrollPane.VERTICAL_SCROLLBAR_ALWAYS );
		editorScrollPane.setPreferredSize( new Dimension( 600, 300 ) );
		editorScrollPane.setMinimumSize( new Dimension( 10, 10 ) );

		final JFrame f = new JFrame();
		f.setIconImage( MastodonIcons.MASTODON_ICON_MEDIUM.getImage() );
		f.setTitle( title );
		f.setSize( 600, 400 );
		f.setLocationRelativeTo( parent );
		f.getContentPane().add( editorScrollPane, BorderLayout.CENTER );
		f.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
		f.setResizable( true );
		f.setVisible( true );
	}

	static final void browseToBDVFile( final String suggestedFile, final JTextArea target, final Runnable onSucess,
			final JComponent parent )
	{
		final EverythingDisablerAndReenabler disabler =
				new EverythingDisablerAndReenabler( parent, new Class[] { JLabel.class } );
		disabler.disable();
		try
		{
			final File file = FileChooser.chooseFile(
					parent,
					suggestedFile,
					new XmlFileFilter(),
					"Open BigDataViewer File",
					FileChooser.DialogType.LOAD );
			if ( file == null )
				return;

			target.setText( file.getAbsolutePath() );
			onSucess.run();
		}
		finally
		{
			disabler.reenable();
		}
	}

	static final void decorateJComponent( final JComponent component, final Runnable toExecute )
	{
		final KeyStroke enter = KeyStroke.getKeyStroke( "ENTER" );
		final String TEXT_SUBMIT = "PRESSED_ENTER";
		final InputMap input = component.getInputMap();
		input.put( enter, TEXT_SUBMIT );
		final ActionMap actions = component.getActionMap();
		actions.put( TEXT_SUBMIT, new RunnableAction( "EnterPressed", toExecute ) );
	}

	static final String buildInfoString( final AbstractSpimData< ? > spimData )
	{
		final StringBuilder str = new StringBuilder();
		str.append( "<html>" );
		str.append( "<ul>" );

		final int nTimePoints = spimData.getSequenceDescription().getTimePoints().size();
		str.append( "<li>N time-points: " + nTimePoints );

		final int nViews = spimData.getSequenceDescription().getViewSetupsOrdered().size();
		str.append( "<li>N views: " + nViews );

		if ( nViews > 0 )
		{
			str.append( "<ol start=\"0\">" );
			for ( int i = 0; i < nViews; i++ )
			{
				final BasicViewSetup setup = spimData.getSequenceDescription().getViewSetupsOrdered().get( i );
				str.append( "<li>" );
				if ( setup.hasName() )
					str.append( setup.getName() + ": " );

				if ( setup.hasSize() && setup.getSize().numDimensions() > 0 )
				{
					final Dimensions size = setup.getSize();
					for ( int d = 0; d < size.numDimensions(); d++ )
						str.append( size.dimension( d ) + " x " );
					str.delete( str.length() - 3, str.length() );

					if ( setup.hasVoxelSize() && setup.getVoxelSize().numDimensions() > 0 )
					{
						str.append( "; " );
						final VoxelDimensions voxelSize = setup.getVoxelSize();
						for ( int d = 0; d < voxelSize.numDimensions(); d++ )
							str.append( voxelSize.dimension( d ) + " x " );

						str.delete( str.length() - 2, str.length() );
						str.append( voxelSize.unit() );
					}
				}

			}
			str.append( "</ol>" );
		}

		str.append( "</ul>" );
		str.append( "</html>" );
		return str.toString();
	}
}
