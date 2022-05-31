package org.mastodon.views.bdv.export;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.PrintStream;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.mastodon.app.MastodonIcons;
import org.mastodon.ui.keymap.CommandDescriptionProvider;
import org.mastodon.ui.keymap.CommandDescriptions;
import org.mastodon.ui.keymap.KeyConfigContexts;
import org.mastodon.ui.util.FileChooser;
import org.mastodon.ui.util.FileChooser.DialogType;
import org.mastodon.ui.util.FileChooser.SelectionMode;
import org.mastodon.views.bdv.BigDataViewerMamut;
import org.mastodon.views.bdv.overlay.OverlayGraphRenderer;
import org.mastodon.views.trackscheme.display.ColorBarOverlay;
import org.scijava.plugin.Plugin;
import org.scijava.ui.behaviour.util.Actions;

import bdv.BigDataViewerActions;
import bdv.export.ProgressWriter;
import bdv.util.DelayedPackDialog;
import bdv.viewer.OverlayRenderer;
import bdv.viewer.ViewerPanel;
import ij.io.LogStream;

/**
 * Adapted from BDV {@code RecordMovieDialog} to also record the MaMuT overlay.
 *
 * @author Jean-Yves Tinevez
 *
 */
public class RecordMovieDialog extends DelayedPackDialog implements OverlayRenderer
{

	private static final String RECORD_MOVIE_DIALOG = "record movie dialog";

	private static final String[] RECORD_MOVIE_DIALOG_KEYS = { "ctrl R" };

	/*
	 * Command descriptions for all provided commands
	 */
	@Plugin( type = CommandDescriptionProvider.class )
	public static class Descriptions extends CommandDescriptionProvider
	{

		public Descriptions()
		{
			super( KeyConfigContexts.BIGDATAVIEWER );
		}

		@Override
		public void getCommandDescriptions( final CommandDescriptions descriptions )
		{
			descriptions.add( RECORD_MOVIE_DIALOG, RECORD_MOVIE_DIALOG_KEYS, "Show the record movie dialog." );
		}
	}

	public static void install(
			final Actions actions,
			final BigDataViewerMamut bdv, final OverlayGraphRenderer< ?, ? > tracksOverlay, final ColorBarOverlay colorBarOverlay )
	{
		final RecordMovieDialog dialog = new RecordMovieDialog(
				bdv.getViewerFrame(),
				bdv.getViewer(),
				tracksOverlay,
				colorBarOverlay );
		dialog.setLocationRelativeTo( bdv.getViewerFrame() );
		BigDataViewerActions.toggleDialogAction( actions, dialog, RECORD_MOVIE_DIALOG, RECORD_MOVIE_DIALOG_KEYS );
	}

	private static final long serialVersionUID = 1L;

	private final int maxTimepoint;

	private final JTextField pathTextField;

	private final JSpinner spinnerMinTimepoint;

	private final JSpinner spinnerMaxTimepoint;

	private final JSpinner spinnerWidth;

	private final JSpinner spinnerHeight;

	private JTextField textField;

	public RecordMovieDialog(
			final Frame owner,
			final ViewerPanel viewer,
			final OverlayGraphRenderer< ?, ? > tracksOverlay,
			final ColorBarOverlay colorBarOverlay )
	{
		super( owner, "Record BDV movie", false );
		maxTimepoint = viewer.state().getNumTimepoints() - 1;

		final JPanel boxes = new JPanel();
		boxes.setBorder( new EmptyBorder( 5, 5, 5, 5 ) );
		getContentPane().add( boxes, BorderLayout.CENTER );
		final GridBagLayout gblBoxes = new GridBagLayout();
		gblBoxes.columnWidths = new int[] { 309, 0 };
		gblBoxes.rowHeights = new int[] { 0, 0, 30, 0, 25, 30, 0, 0, 30, 10, 30, 0 };
		gblBoxes.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gblBoxes.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, Double.MIN_VALUE };
		boxes.setLayout( gblBoxes );

		final JPanel timepointsPanel = new JPanel();
		final GridBagConstraints gbcTimepointsPanel = new GridBagConstraints();
		gbcTimepointsPanel.fill = GridBagConstraints.BOTH;
		gbcTimepointsPanel.insets = new Insets( 0, 0, 5, 0 );
		gbcTimepointsPanel.gridx = 0;
		gbcTimepointsPanel.gridy = 0;
		boxes.add( timepointsPanel, gbcTimepointsPanel );
		timepointsPanel.setLayout( new BoxLayout( timepointsPanel, BoxLayout.X_AXIS ) );

		timepointsPanel.add( new JLabel( "Record from timepoint" ) );

		final Component horizontalGlue = Box.createHorizontalGlue();
		timepointsPanel.add( horizontalGlue );

		spinnerMinTimepoint = new JSpinner();
		spinnerMinTimepoint.setModel( new SpinnerNumberModel( 0, 0, maxTimepoint, 1 ) );
		timepointsPanel.add( spinnerMinTimepoint );

		timepointsPanel.add( Box.createHorizontalGlue() );
		timepointsPanel.add( new JLabel( "to" ) );

		timepointsPanel.add( Box.createHorizontalStrut( 5 ) );
		spinnerMaxTimepoint = new JSpinner();
		spinnerMaxTimepoint.setModel( new SpinnerNumberModel( maxTimepoint, 0, maxTimepoint, 1 ) );
		timepointsPanel.add( spinnerMaxTimepoint );

		spinnerMinTimepoint.addChangeListener( new ChangeListener()
		{
			@Override
			public void stateChanged( final ChangeEvent e )
			{
				final int min = ( Integer ) spinnerMinTimepoint.getValue();
				final int max = ( Integer ) spinnerMaxTimepoint.getValue();
				if ( max < min )
					spinnerMaxTimepoint.setValue( min );
			}
		} );

		spinnerMaxTimepoint.addChangeListener( new ChangeListener()
		{
			@Override
			public void stateChanged( final ChangeEvent e )
			{
				final int min = ( Integer ) spinnerMinTimepoint.getValue();
				final int max = ( Integer ) spinnerMaxTimepoint.getValue();
				if ( min > max )
					spinnerMinTimepoint.setValue( max );
			}
		} );

		final JPanel widthPanel = new JPanel();
		final GridBagConstraints gbcWidthPanel = new GridBagConstraints();
		gbcWidthPanel.fill = GridBagConstraints.BOTH;
		gbcWidthPanel.insets = new Insets( 0, 0, 5, 0 );
		gbcWidthPanel.gridx = 0;
		gbcWidthPanel.gridy = 1;
		boxes.add( widthPanel, gbcWidthPanel );
		widthPanel.setLayout( new BoxLayout( widthPanel, BoxLayout.X_AXIS ) );

		final JLabel lblTargetSize = new JLabel( "Target Size" );
		widthPanel.add( lblTargetSize );

		widthPanel.add( Box.createHorizontalGlue() );
		widthPanel.add( new JLabel( "width" ) );

		widthPanel.add( Box.createHorizontalStrut( 5 ) );
		spinnerWidth = new JSpinner();
		spinnerWidth.setModel( new SpinnerNumberModel( 800, 10, 5000, 1 ) );
		widthPanel.add( spinnerWidth );

		widthPanel.add( Box.createHorizontalGlue() );
		final JLabel label = new JLabel( "height" );
		widthPanel.add( label );

		widthPanel.add( Box.createHorizontalStrut( 5 ) );
		spinnerHeight = new JSpinner();
		widthPanel.add( spinnerHeight );
		spinnerHeight.setModel( new SpinnerNumberModel( 600, 10, 5000, 1 ) );

		final GridBagConstraints gbcSeparator = new GridBagConstraints();
		gbcSeparator.anchor = GridBagConstraints.SOUTH;
		gbcSeparator.fill = GridBagConstraints.HORIZONTAL;
		gbcSeparator.insets = new Insets( 0, 0, 5, 0 );
		gbcSeparator.gridx = 0;
		gbcSeparator.gridy = 2;
		boxes.add( new JSeparator(), gbcSeparator );

		final JRadioButton rdbtnToPNG = new JRadioButton( "Record as a folder of PNGs " );
		final GridBagConstraints gbcRdbtnToPNG = new GridBagConstraints();
		gbcRdbtnToPNG.anchor = GridBagConstraints.WEST;
		gbcRdbtnToPNG.insets = new Insets( 0, 0, 5, 0 );
		gbcRdbtnToPNG.gridx = 0;
		gbcRdbtnToPNG.gridy = 3;
		boxes.add( rdbtnToPNG, gbcRdbtnToPNG );

		final JPanel saveAsPanel = new JPanel();
		final GridBagConstraints gbcSaveAsPanel = new GridBagConstraints();
		gbcSaveAsPanel.fill = GridBagConstraints.BOTH;
		gbcSaveAsPanel.insets = new Insets( 0, 0, 5, 0 );
		gbcSaveAsPanel.gridx = 0;
		gbcSaveAsPanel.gridy = 4;
		boxes.add( saveAsPanel, gbcSaveAsPanel );
		saveAsPanel.setLayout( new BoxLayout( saveAsPanel, BoxLayout.X_AXIS ) );

		saveAsPanel.add( new JLabel( "save to" ) );

		saveAsPanel.add( Box.createHorizontalStrut( 5 ) );
		pathTextField = new JTextField();
		saveAsPanel.add( pathTextField );
		pathTextField.setColumns( 20 );

		saveAsPanel.add( Box.createHorizontalStrut( 10 ) );

		final JButton browseButton = new JButton( "Browse" );
		saveAsPanel.add( browseButton );

		browseButton.addActionListener( new ActionListener()
		{
			@Override
			public void actionPerformed( final ActionEvent e )
			{
				final File file = FileChooser.chooseFile(
						FileChooser.useJFileChooser,
						RecordMovieDialog.this,
						pathTextField.getText(),
						null,
						"Browse to a folder to save the PNGs to",
						DialogType.SAVE,
						SelectionMode.DIRECTORIES_ONLY,
						MastodonIcons.BDV_ICON_MEDIUM.getImage() );
				if ( file != null )
					pathTextField.setText( file.getAbsolutePath() );
			}
		} );

		final GridBagConstraints gbcSeparator1 = new GridBagConstraints();
		gbcSeparator1.anchor = GridBagConstraints.SOUTH;
		gbcSeparator1.fill = GridBagConstraints.HORIZONTAL;
		gbcSeparator1.insets = new Insets( 0, 0, 5, 0 );
		gbcSeparator1.gridx = 0;
		gbcSeparator1.gridy = 5;
		boxes.add( new JSeparator(), gbcSeparator1 );

		final JRadioButton rdbtnToMovie = new JRadioButton( "Record to a movie file" );
		final GridBagConstraints gbcRdbtnToMovie = new GridBagConstraints();
		gbcRdbtnToMovie.anchor = GridBagConstraints.WEST;
		gbcRdbtnToMovie.insets = new Insets( 0, 0, 5, 0 );
		gbcRdbtnToMovie.gridx = 0;
		gbcRdbtnToMovie.gridy = 6;
		boxes.add( rdbtnToMovie, gbcRdbtnToMovie );

		final JPanel panelSaveTo2 = new JPanel();
		final GridBagConstraints gbcPanelSaveTo2 = new GridBagConstraints();
		gbcPanelSaveTo2.insets = new Insets( 0, 0, 5, 0 );
		gbcPanelSaveTo2.fill = GridBagConstraints.BOTH;
		gbcPanelSaveTo2.gridx = 0;
		gbcPanelSaveTo2.gridy = 7;
		boxes.add( panelSaveTo2, gbcPanelSaveTo2 );
		panelSaveTo2.setLayout( new BoxLayout( panelSaveTo2, BoxLayout.X_AXIS ) );

		final JLabel lblSaveTo2 = new JLabel( "save to" );
		panelSaveTo2.add( lblSaveTo2 );

		panelSaveTo2.add( Box.createHorizontalStrut( 5 ) );

		textField = new JTextField();
		panelSaveTo2.add( textField );
		textField.setColumns( 10 );

		panelSaveTo2.add( Box.createHorizontalStrut( 10 ) );

		final JButton btnBrowseMovie = new JButton( "Browse" );
		panelSaveTo2.add( btnBrowseMovie );

		final JPanel panelFPS = new JPanel();
		final FlowLayout flowLayout = ( FlowLayout ) panelFPS.getLayout();
		flowLayout.setAlignment( FlowLayout.RIGHT );
		final GridBagConstraints gbcPanelFPS = new GridBagConstraints();
		gbcPanelFPS.insets = new Insets( 0, 0, 5, 0 );
		gbcPanelFPS.fill = GridBagConstraints.BOTH;
		gbcPanelFPS.gridx = 0;
		gbcPanelFPS.gridy = 8;
		boxes.add( panelFPS, gbcPanelFPS );

		final JLabel lblNewLabel = new JLabel( "fps" );
		panelFPS.add( lblNewLabel );

		panelFPS.add( Box.createHorizontalStrut( 10 ) );

		final JSpinner spinnerFPS = new JSpinner();
		spinnerFPS.setModel( new SpinnerNumberModel( 10, 1, 200, 1 ) );
		panelFPS.add( spinnerFPS );

		final GridBagConstraints gbcSeparator2 = new GridBagConstraints();
		gbcSeparator2.anchor = GridBagConstraints.NORTH;
		gbcSeparator2.fill = GridBagConstraints.HORIZONTAL;
		gbcSeparator2.insets = new Insets( 0, 0, 5, 0 );
		gbcSeparator2.gridx = 0;
		gbcSeparator2.gridy = 9;
		boxes.add( new JSeparator(), gbcSeparator2 );

		final JPanel panelRecord = new JPanel();
		final GridBagConstraints gbcPanelRecord = new GridBagConstraints();
		gbcPanelRecord.anchor = GridBagConstraints.SOUTH;
		gbcPanelRecord.fill = GridBagConstraints.HORIZONTAL;
		gbcPanelRecord.gridx = 0;
		gbcPanelRecord.gridy = 10;
		boxes.add( panelRecord, gbcPanelRecord );
		panelRecord.setLayout( new BoxLayout( panelRecord, BoxLayout.X_AXIS ) );

		final JProgressBar progressBar = new JProgressBar();
		panelRecord.add( progressBar );

		final JButton recordButton = new JButton( "Record" );
		panelRecord.add( recordButton );

		/*
		 * Progress bar.
		 */

		progressBar.getModel().setMinimum( 0 );
		progressBar.getModel().setMaximum( 100 );
		progressBar.setStringPainted( true );
		final ProgressWriter progressWriter = new ProgressWriter()
		{
			private final LogStream ls = new LogStream();

			@Override
			public void setProgress( final double completionRatio )
			{
				progressBar.setValue( ( int ) ( 100 * completionRatio ) );
			}

			@Override
			public PrintStream out()
			{
				return ls;
			}

			@Override
			public PrintStream err()
			{
				return ls;
			}
		};

		/*
		 * Listeners.
		 */

		recordButton.addActionListener( new ActionListener()
		{
			@Override
			public void actionPerformed( final ActionEvent e )
			{
				final String dirname = pathTextField.getText();
				final int fps = 30;
				final String filename = "C:/Users/tinevez/Desktop/testexport/testMastodon.mp4";
				final AbstractBDVRecorder recorder = new MovieFileBDVRecorder( viewer, tracksOverlay, colorBarOverlay, progressWriter, filename, fps );

				final File dir = new File( dirname );
				if ( !dir.exists() )
					dir.mkdirs();
				if ( !dir.exists() || !dir.isDirectory() )
				{
					System.err.println( "Invalid export directory " + dirname );
					return;
				}
				final int minTimepointIndex = ( Integer ) spinnerMinTimepoint.getValue();
				final int maxTimepointIndex = ( Integer ) spinnerMaxTimepoint.getValue();
				final int width = ( Integer ) spinnerWidth.getValue();
				final int height = ( Integer ) spinnerHeight.getValue();
				new Thread()
				{
					@Override
					public void run()
					{
						try
						{
							recordButton.setEnabled( false );
							recorder.record( width, height, minTimepointIndex, maxTimepointIndex );
							recordButton.setEnabled( true );
						}
						catch ( final Exception ex )
						{
							ex.printStackTrace();
						}
					}
				}.start();
			}
		} );

		final JFileChooser fileChooser = new JFileChooser();
		fileChooser.setMultiSelectionEnabled( false );
		fileChooser.setFileSelectionMode( JFileChooser.DIRECTORIES_ONLY );

		final ActionMap am = getRootPane().getActionMap();
		final InputMap im = getRootPane().getInputMap( JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT );
		final Object hideKey = new Object();
		final Action hideAction = new AbstractAction()
		{
			@Override
			public void actionPerformed( final ActionEvent e )
			{
				setVisible( false );
			}

			private static final long serialVersionUID = 1L;
		};
		im.put( KeyStroke.getKeyStroke( KeyEvent.VK_ESCAPE, 0 ), hideKey );
		am.put( hideKey, hideAction );

		pack();
	}

	@Override
	public void drawOverlays( final Graphics g )
	{}

	@Override
	public void setCanvasSize( final int width, final int height )
	{
		spinnerWidth.setValue( width );
		spinnerHeight.setValue( height );
	}
}
