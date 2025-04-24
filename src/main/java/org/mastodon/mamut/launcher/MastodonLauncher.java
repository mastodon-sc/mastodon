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
package org.mastodon.mamut.launcher;

import java.awt.Font;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.IntUnaryOperator;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

import org.jdom2.JDOMException;
import org.mastodon.app.MastodonIcons;
import org.mastodon.feature.FeatureSpecsService;
import org.mastodon.mamut.MainWindow;
import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.io.ProjectCreator;
import org.mastodon.mamut.io.ProjectLoader;
import org.mastodon.mamut.io.importer.simi.SimiImporter;
import org.mastodon.mamut.io.importer.simi.SimiImporter.LabelFunction;
import org.mastodon.mamut.io.importer.tgmm.TgmmImporter;
import org.mastodon.mamut.io.importer.trackmate.TrackMateImporter;
import org.mastodon.mamut.io.project.MamutProject;
import org.mastodon.mamut.io.project.MamutProjectIO;
import org.mastodon.mamut.model.Model;
import org.mastodon.ui.util.EverythingDisablerAndReenabler;
import org.mastodon.ui.util.ExtensionFileFilter;
import org.mastodon.ui.util.FileChooser;
import org.mastodon.ui.util.FileChooser.SelectionMode;
import org.mastodon.ui.util.XmlFileFilter;
import org.scijava.Context;
import org.scijava.util.VersionUtils;

import ij.ImagePlus;
import ij.gui.ImageWindow;
import mpicbg.spim.data.SpimDataException;
import mpicbg.spim.data.generic.AbstractSpimData;
import mpicbg.spim.data.generic.sequence.AbstractSequenceDescription;
import mpicbg.spim.data.registration.ViewRegistrations;
import mpicbg.spim.data.sequence.TimePoint;
import mpicbg.spim.data.sequence.TimePoints;
import mpicbg.spim.data.sequence.TimePointsPattern;

public class MastodonLauncher extends JFrame
{

	private static final long serialVersionUID = 1L;

	static final String MASTODON_VERSION = VersionUtils.getVersion( MastodonLauncher.class );

	private final LauncherGUI gui;

	private final Context context;

	public MastodonLauncher( final Context context )
	{
		super( "Mastodon launcher" );
		setFont();
		this.context = Optional.ofNullable( context ).orElse( new Context() );

		System.setProperty( "apple.laf.useScreenMenuBar", "true" );

		setIconImage( MastodonIcons.MASTODON_ICON_LARGE.getImage() );
		gui = new LauncherGUI( s -> loadMastodonProject( s ) );

		/*
		 * Wire buttons.
		 */
		gui.btnNew.addActionListener( l -> newMastodonProject() );
		gui.btnOpenURL.addActionListener( l -> showOpenFromURLPanel() );
		gui.btnLoad.addActionListener( l -> showShowRecentProjects() );
		gui.btnImportTgmm.addActionListener( l -> showImportTgmmPanel() );
		gui.btnImportMamut.addActionListener( l -> importMaMuT() );
		gui.btnImportSimi.addActionListener( l -> showImportSimiPanel() );
		gui.btnHelp.addActionListener( l -> showHelpPanel() );

		gui.newMastodonProjectPanel.btnCreate.addActionListener( l -> createNewProject() );
		gui.newFromUrlPanel.btnCreate.addActionListener( l -> createNewProjectFromURL() );
		gui.importTGMMPanel.btnImport.addActionListener( l -> importTgmm() );
		gui.importSimiBioCellPanel.btnImport.addActionListener( l -> importSimi() );

		getContentPane().add( gui );
		setSize( 630, 660 );

		setDropTarget( new LauncherDropTarget() );
	}

	private static void setFont()
	{
		// Determine the OS and set the font
		String os = System.getProperty( "os.name" ).toLowerCase();

		if ( os.contains( "nix" ) || os.contains( "nux" ) )
		{
			Font defaultFont = new Font( "Arial", Font.PLAIN, 12 );
			// Set the font for UI components
			UIManager.put( "Label.font", defaultFont );
			UIManager.put( "Button.font", defaultFont );
			UIManager.put( "TextField.font", defaultFont );
			UIManager.put( "TextArea.font", defaultFont );
			UIManager.put( "ComboBox.font", defaultFont );
			UIManager.put( "List.font", defaultFont );
			UIManager.put( "Table.font", defaultFont );
			UIManager.put( "Menu.font", defaultFont );
			UIManager.put( "MenuItem.font", defaultFont );
		}
	}

	private void importSimi()
	{
		gui.clearLog();
		if ( !gui.importSimiBioCellPanel.checkBDVFile() )
			return;

		final EverythingDisablerAndReenabler disabler =
				new EverythingDisablerAndReenabler( gui, new Class[] { JLabel.class } );
		disabler.disable();

		new Thread( () -> {
			try
			{
				// Create new blank project from BDV file.
				final File bdvFile = new File( gui.importSimiBioCellPanel.textAreaBDVFile.getText() );
				final ProjectModel appModel = LauncherUtil.createProjectFromBdvFileWithDialog( bdvFile, context, gui, gui::error );

				final Model model = appModel.getModel();
				final AbstractSpimData< ? > spimData = appModel.getSharedBdvData().getSpimData();

				final String sbdFilename = gui.importSimiBioCellPanel.textAreaSimiFile.getText();
				final int setupIndex = gui.importSimiBioCellPanel.setupComboBox.getSelectedIndex();
				final ViewRegistrations regs = spimData.getViewRegistrations();
				final AbstractSequenceDescription< ?, ?, ? > seq = spimData.getSequenceDescription();
				final List< TimePoint > timePointsOrdered = seq.getTimePoints().getTimePointsOrdered();
				final int maxtp = timePointsOrdered.size() - 1;
				final int setupID = seq.getViewSetupsOrdered().get( setupIndex ).getId();

				final int frameOffset =
						( ( Number ) gui.importSimiBioCellPanel.spinnerTimeOffset.getValue() ).intValue();

				// maps frame to timepoint index
				final IntUnaryOperator frameToTimepointFunction = frame -> {
					int tp = frame - frameOffset;
					if ( tp < 0 )
					{
						gui.importSimiBioCellPanel.labelInfo.setText(
								"<html>WARNING: simi frame " + frame + " translates to out-of-bounds "
										+ "timepoint index " + tp + ". Clipping to 0.</html>" );
						tp = 0;
					}
					else if ( tp > maxtp )
					{
						gui.importSimiBioCellPanel.labelInfo.setText(
								"<html>WARNING: simi frame " + frame + " translates to out-of-bounds "
										+ "timepoint index " + tp + ". Clipping to " + maxtp + ".</html>" );
						tp = maxtp;
					}
					return tp;
				};

				final LabelFunction labelFunction = ( generic_name, generation_name, name ) -> {
					if ( name != null )
						return name;
					if ( generic_name != null )
						return generic_name;
					if ( generation_name != null )
						return generation_name;
					return null;
				};

				final BiFunction< Integer, double[], double[] > positionFunction = ( frame, lPos ) -> {
					final double[] gPos = new double[ 3 ];
					final int tp = frameToTimepointFunction.applyAsInt( frame );
					final int timepointId = timePointsOrdered.get( tp ).getId();
					regs.getViewRegistration( timepointId, setupID ).getModel().apply( lPos, gPos );
					return gPos;
				};
				final int radius = Integer.parseInt( gui.importSimiBioCellPanel.spotRadiusTextField.getText() );
				final boolean interpolateMissingSpots = gui.importSimiBioCellPanel.interpolateCheckBox.isSelected();
				SimiImporter.read( sbdFilename, frameToTimepointFunction, labelFunction, positionFunction, radius,
						interpolateMissingSpots, model );
				new MainWindow( appModel ).setVisible( true );
				dispose();
			}
			catch ( final IOException e )
			{
				gui.importSimiBioCellPanel.labelInfo.setText(
						"<html>Problem reading the SimiBioCell file.<p>" +
								e.getMessage() + "</html>" );
			}
			catch ( final ParseException e )
			{
				gui.importSimiBioCellPanel.labelInfo.setText(
						"<html>Problem parsing the SimiBioCell file.<p>" +
								e.getMessage() + "</html>" );
			}
			finally
			{
				disabler.reenable();
			}
		} ).start();
	}

	private void importTgmm()
	{
		gui.clearLog();
		if ( !gui.importTGMMPanel.checkBDVFile( false ) || !gui.importTGMMPanel.checkTGMMFolder() )
			return;

		final EverythingDisablerAndReenabler disabler =
				new EverythingDisablerAndReenabler( gui, new Class[] { JLabel.class } );
		disabler.disable();

		new Thread( () -> {
			try
			{
				String tgmmFiles = gui.importTGMMPanel.textAreaTGMM.getText();
				if ( !tgmmFiles.endsWith( "/" ) )
					tgmmFiles = tgmmFiles + "/";
				tgmmFiles = tgmmFiles + gui.importTGMMPanel.filenamePatternTextField.getText();

				// Create new blank project from BDV file.
				final File bdvFile = new File( gui.importTGMMPanel.textAreaBDVFile.getText() );
				final ProjectModel appModel = LauncherUtil.createProjectFromBdvFileWithDialog( bdvFile, context, gui, gui::error );

				final Model model = appModel.getModel();
				final AbstractSpimData< ? > spimData = appModel.getSharedBdvData().getSpimData();

				// Read setup id.
				final ViewRegistrations viewRegistrations = spimData.getViewRegistrations();
				final int setupIndex = gui.importTGMMPanel.setupComboBox.getSelectedIndex();
				final AbstractSequenceDescription< ?, ?, ? > seq = spimData.getSequenceDescription();
				final int setupID = seq.getViewSetupsOrdered().get( setupIndex ).getId();

				// Run import.
				final TimePoints timepoints =
						new TimePointsPattern( gui.importTGMMPanel.timepointPatternTextField.getText() );
				final double nSigmas = Double.parseDouble( gui.importTGMMPanel.nSigmasTextField.getText() );
				if ( gui.importTGMMPanel.covCheckBox.isSelected() )
				{
					final double[][] cov = parseCov( gui.importTGMMPanel.covTextField.getText() );
					if ( null == cov )
					{
						gui.importTGMMPanel.labelInfo.setText( "<html>Cannot parse the covariance pattern.</html>" );
						return;
					}
					TgmmImporter.read( tgmmFiles, timepoints, TgmmImporter.getTimepointToIndex( spimData ),
							viewRegistrations, setupID, nSigmas, cov, model );
				}
				else
					TgmmImporter.read( tgmmFiles, timepoints, TgmmImporter.getTimepointToIndex( spimData ),
							viewRegistrations, setupID, nSigmas, model );

				// Success? We move on.
				new MainWindow( appModel ).setVisible( true );
				dispose();
			}
			catch ( final ParseException e )
			{
				gui.importTGMMPanel.labelInfo.setText( "<html>Could not parse timepoint pattern.<p>" +
						e.getMessage() + "</html>" );
			}
			catch ( JDOMException | IOException e )
			{
				gui.importTGMMPanel.labelInfo.setText( "<html>Malformed TGMM dataset.<p>" +
						e.getMessage() + "</html>" );
			}
			disabler.reenable();
		} ).start();
	}

	private double[][] parseCov( final String text )
	{
		try
		{
			final String[] entries = text.split( "\\s+" );
			if ( entries.length == 1 )
			{
				if ( !entries[ 0 ].isEmpty() )
				{
					final double v = Double.parseDouble( entries[ 0 ] );
					return new double[][] { { v, 0, 0 }, { 0, v, 0 }, { 0, 0, v } };
				}
			}
			else if ( entries.length == 3 )
			{
				final double[][] m = new double[ 3 ][ 3 ];
				for ( int r = 0; r < 3; ++r )
					m[ r ][ r ] = Double.parseDouble( entries[ r ] );
				return m;
			}
			else if ( entries.length == 9 )
			{
				final double[][] m = new double[ 3 ][ 3 ];
				for ( int r = 0; r < 3; ++r )
					for ( int c = 0; c < 3; ++c )
						m[ r ][ c ] = Double.parseDouble( entries[ r * 3 + c ] );
				return m;
			}
		}
		catch ( final Exception e )
		{}
		return null;
	}

	private void createNewProjectFromURL()
	{
		gui.clearLog();

		/*
		* Open from a URL.
		*/

		final EverythingDisablerAndReenabler disabler =
				new EverythingDisablerAndReenabler( gui, new Class[] { JLabel.class } );
		disabler.disable();
		new Thread( () -> {
			try
			{
				final ProjectModel appModel =
						LauncherUtil.createProjectFromBdvFileWithDialog( gui.newFromUrlPanel.xmlFile, context, gui, gui::error );
				new MainWindow( appModel ).setVisible( true );
				dispose();
			}
			finally
			{
				disabler.reenable();
			}
		} ).start();
	}

	private void createNewProject()
	{
		gui.clearLog();
		if ( gui.newMastodonProjectPanel.rdbtBrowseToBDV.isSelected() )
		{
			/*
			 * Open from a BDV local file.
			 */

			if ( !gui.newMastodonProjectPanel.checkBDVFile() )
				return;

			final File file = new File( gui.newMastodonProjectPanel.textAreaFile.getText() );
			final EverythingDisablerAndReenabler disabler =
					new EverythingDisablerAndReenabler( gui, new Class[] { JLabel.class } );
			disabler.disable();
			new Thread( () -> {
				try
				{
					final ProjectModel appModel = LauncherUtil.createProjectFromBdvFileWithDialog( file, context, gui, gui::error );
					new MainWindow( appModel ).setVisible( true );
					dispose();
				}
				finally
				{
					disabler.reenable();
				}
			} ).start();
		}
		else
		{
			/*
			 * Open from an ImagePlus opened in ImageJ.
			 */

			final String imageName = ( String ) gui.newMastodonProjectPanel.comboBox.getSelectedItem();
			final ImagePlus imp = ij.WindowManager.getImage( imageName );
			if ( imp == null )
			{
				gui.newMastodonProjectPanel.labelInfo.setText( "Invalid image." );
				return;
			}
			final EverythingDisablerAndReenabler disabler =
					new EverythingDisablerAndReenabler( gui, new Class[] { JLabel.class } );
			disabler.disable();
			new Thread( () -> {
				try
				{
					final ProjectModel appModel = ProjectCreator.createProjectFromImp( imp, context );
					final MainWindow mainWindow = new MainWindow( appModel );

					/*
					 * Action when user closes source image plus.
					 */
					final ImageWindow window = imp.getWindow();
					if ( window != null )
					{
						for ( final WindowListener wl : window.getWindowListeners() )
							window.removeWindowListener( wl );

						window.addWindowListener( new WindowAdapter()
						{
							@Override
							public void windowClosing( final WindowEvent e )
							{
								final int val = JOptionPane.showConfirmDialog(
										window,
										"Warning.\n"
												+ "\n"
												+ "If you close this image, the Mastodon \n"
												+ "instance that runs on it will be closed \n"
												+ "as well.\n"
												+ "\n"
												+ "Are you sure you want to close this image?",
										"Confirm closing image",
										JOptionPane.YES_NO_OPTION,
										JOptionPane.QUESTION_MESSAGE,
										MastodonIcons.MASTODON_ICON_MEDIUM );
								if ( val == JOptionPane.YES_OPTION )
								{
									final boolean hasBeenClosed = mainWindow.close();
									if ( hasBeenClosed )
										window.close();
								}
							}
						} );
					}

					// Check whether the imp can be found on disk.
					if ( imp.getOriginalFileInfo() == null ||
							imp.getOriginalFileInfo().directory == null ||
							imp.getOriginalFileInfo().fileName == null ||
							!new File( imp.getOriginalFileInfo().directory, imp.getOriginalFileInfo().fileName )
									.exists() )
					{
						JOptionPane.showMessageDialog( gui,
								"Warning.\n"
										+ "\n"
										+ "The image being used for this new \n"
										+ "Mastodon project cannot be found \n"
										+ "on disk.  \n"
										+ "\n"
										+ "Mastodon will not be able to reopen it \n"
										+ "unless you resave the image as a BDV \n"
										+ "file when saving the Mastodon project. ",
								"Source image not saved",
								JOptionPane.WARNING_MESSAGE,
								MastodonIcons.MASTODON_ICON_MEDIUM );
					}

					mainWindow.setVisible( true );
					dispose();
				}
				catch ( final SpimDataException e )
				{
					gui.newMastodonProjectPanel.labelInfo.setText( "<html>Invalid image.<p>" +
							e.getMessage() + "</html>" );
				}
				finally
				{
					disabler.reenable();
				}
			} ).start();
		}
	}

	private void newMastodonProject()
	{
		gui.showPanel( LauncherGUI.NEW_MASTODON_PROJECT_KEY );
	}

	private void showShowRecentProjects()
	{
		gui.showPanel( LauncherGUI.RECENT_PROJECTS_KEY );
	}

	private void showOpenFromURLPanel()
	{
		gui.showPanel( LauncherGUI.NEW_FROM_URL_KEY );
	}

	private void showImportTgmmPanel()
	{
		gui.showPanel( LauncherGUI.IMPORT_TGMM_KEY );
	}

	private void showImportSimiPanel()
	{
		gui.showPanel( LauncherGUI.IMPORT_SIMI_KEY );
	}

	private void showHelpPanel()
	{
		gui.showPanel( LauncherGUI.WELCOME_PANEL_KEY );
	}

	private void importMaMuT()
	{
		final EverythingDisablerAndReenabler disabler =
				new EverythingDisablerAndReenabler( gui, new Class[] { JLabel.class } );
		disabler.disable();
		final File file = FileChooser.chooseFile(
				this,
				null,
				new XmlFileFilter(),
				"Import MaMuT Project",
				FileChooser.DialogType.LOAD );
		if ( file == null )
		{
			disabler.reenable();
			return;
		}

		gui.showPanel( LauncherGUI.LOGGER_KEY );
		new Thread( () -> {
			MamutProject project = null;
			try
			{
				final TrackMateImporter importer = new TrackMateImporter( file );
				project = importer.createProject();
				final ProjectModel appModel = ProjectLoader.open( project, context );

				final FeatureSpecsService featureSpecsService = context.getService( FeatureSpecsService.class );
				importer.readModel( appModel.getModel(), featureSpecsService );
				new MainWindow( appModel ).setVisible( true );
				dispose();
			}
			catch ( final IOException | SpimDataException e )
			{
				gui.error( "Invalid MaMuT file.\n\n" + LauncherUtil.getProblemDescription( project, e ) );
			}
			finally
			{
				disabler.reenable();
			}
		} ).start();
	}

	private void loadMastodonProject( final String projectPath )
	{
		final EverythingDisablerAndReenabler disabler = new EverythingDisablerAndReenabler( gui, new Class[] { JLabel.class } );
		disabler.disable();
		gui.clearLog();
		gui.showPanel( LauncherGUI.LOGGER_KEY );
		final File file;
		if ( projectPath == null )
		{
			// Use the the most recent opened location as initial
			// location for the file chooser
			final Iterator< String > iterator = RecentProjectsPanel.recentProjects.iterator();
			final String previousPath = iterator.hasNext() ? iterator.next() : null;
			// We have to use the JFileChooser to open folders.
			file = FileChooser.chooseFile(
					true,
					this,
					previousPath,
					new ExtensionFileFilter( "mastodon" ),
					"Open Mastodon Project",
					FileChooser.DialogType.LOAD,
					SelectionMode.FILES_AND_DIRECTORIES );
			if ( file == null )
			{
				gui.showPanel( LauncherGUI.RECENT_PROJECTS_KEY );
				disabler.reenable();
				return;
			}
		}
		else
		{
			file = new File( projectPath );
		}
		gui.log( "Opening Mastodon project file " + file + "\n" );

		new Thread( () -> {
			try
			{
				try
				{
					final MamutProject project = MamutProjectIO.load( file.getAbsolutePath() );
					final ProjectModel appModel = LauncherUtil.openWithDialog( project, context, this, gui::error );
					if ( appModel == null )
						return;
					new MainWindow( appModel ).setVisible( true );
					dispose();
					/*
					 * We update the list of recent projects here so that only
					 * projects that were successfully opened are added to the
					 * list.
					 */
					RecentProjectsPanel.recentProjects.add( file.getAbsolutePath() );
				}
				catch ( final IOException e )
				{
					gui.error( "Invalid Mastodon file.\nMaybe it is not a Mastodon file?\n\n"
							+ LauncherUtil.getProblemDescription( null, e ) );
				}
			}
			finally
			{
				disabler.reenable();
			}
		} ).start();
	}

	private class LauncherDropTarget extends DropTarget
	{
		private static final long serialVersionUID = 1L;

		@Override
		public synchronized void drop( final DropTargetDropEvent dropTargetDropEvent )
		{
			try
			{
				dropTargetDropEvent.acceptDrop( DnDConstants.ACTION_COPY );
				@SuppressWarnings( "unchecked" )
				final List< File > droppedFiles =
						( List< File > ) dropTargetDropEvent.getTransferable().getTransferData( DataFlavor.javaFileListFlavor );
				for ( final File file : droppedFiles )
				{
					// process files
					loadMastodonProject( file.getAbsolutePath() );
				}
			}
			catch ( final Exception e )
			{
				e.printStackTrace( System.err );
			}
		}
	}
}
