package org.mastodon.revised.mamut;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import org.mastodon.revised.mamut.feature.MamutFeatureComputerService;
import org.mastodon.revised.model.mamut.Model;
import org.mastodon.revised.ui.util.FileChooser;
import org.mastodon.revised.ui.util.XmlFileFilter;
import org.mastodon.revised.util.ToggleDialogAction;
import org.scijava.Context;
import org.scijava.ui.behaviour.io.InputTriggerConfig;
import org.scijava.ui.behaviour.io.yaml.YamlConfigIO;

import bdv.spimdata.SpimDataMinimal;
import bdv.spimdata.XmlIoSpimDataMinimal;
import mpicbg.spim.data.SpimDataException;

public class MainWindow extends JFrame
{
	private static final long serialVersionUID = 1L;

	private final InputTriggerConfig keyconf;

	private MamutProject project;

	private WindowManager windowManager;

	private File proposedProjectFile;

	private final TgmmImportDialog tgmmImportDialog;

	private ToggleDialogAction toggleFeatureComputationDialogAction;

	public MainWindow( final InputTriggerConfig keyconf )
	{
		super( "Mastodon" );
		this.keyconf = keyconf;

		tgmmImportDialog = new TgmmImportDialog( this );

		final JPanel buttonsPanel = new JPanel();
		final GridBagLayout gbl = new GridBagLayout();
		gbl.columnWeights = new double[] { 1.0, 1.0 };
		gbl.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 };
		buttonsPanel.setLayout(gbl);

		final GridBagConstraints separator_gbc = new GridBagConstraints();
		separator_gbc.fill = GridBagConstraints.HORIZONTAL;
		separator_gbc.gridwidth = 2;
		separator_gbc.insets = new Insets(5, 5, 5, 5);
		separator_gbc.gridx = 0;

		final GridBagConstraints label_gbc = new GridBagConstraints();
		label_gbc.fill = GridBagConstraints.HORIZONTAL;
		label_gbc.gridwidth = 2;
		label_gbc.insets = new Insets(5, 5, 5, 5);
		label_gbc.gridx = 0;

		final GridBagConstraints button_gbc_right = new GridBagConstraints();
		button_gbc_right.fill = GridBagConstraints.BOTH;
		button_gbc_right.insets = new Insets(0, 0, 5, 0);
		button_gbc_right.gridx = 1;

		final GridBagConstraints button_gbc_left = new GridBagConstraints();
		button_gbc_left.fill = GridBagConstraints.BOTH;
		button_gbc_left.insets = new Insets(0, 0, 5, 5);
		button_gbc_left.gridx = 0;

		int gridy = 0;

		label_gbc.gridy = gridy;
		buttonsPanel.add( new JLabel( "Views:" ), label_gbc );

		++gridy;

		final JButton bdvButton = new JButton( "bdv" );
		bdvButton.addActionListener( e -> {
			if ( windowManager != null )
				windowManager.createBigDataViewer();
		} );
		button_gbc_right.gridy = gridy;
		buttonsPanel.add( bdvButton, button_gbc_right );

		++gridy;

		final JButton trackschemeButton = new JButton( "trackscheme" );
		trackschemeButton.addActionListener( e -> {
			if ( windowManager != null )
				windowManager.createTrackScheme();
		} );
		button_gbc_right.gridy = gridy;
		buttonsPanel.add( trackschemeButton, button_gbc_right );

		++gridy;

		separator_gbc.gridy = gridy;
		buttonsPanel.add( new JSeparator(), separator_gbc );

		++gridy;

		label_gbc.gridy = gridy;
		buttonsPanel.add( new JLabel( "Processing:" ), label_gbc );

		++gridy;

		final JButton featureComputationButton = new JButton( "features and tags" );
		featureComputationButton.addActionListener( e -> toggleFeaturesDialog() );
		button_gbc_right.gridy = gridy;
		buttonsPanel.add( featureComputationButton, button_gbc_right );

		++gridy;

		separator_gbc.gridy = gridy;
		buttonsPanel.add( new JSeparator(), separator_gbc );

		++gridy;

		label_gbc.gridy = gridy;
		buttonsPanel.add( new JLabel( "Input / Output:" ), label_gbc );

		++gridy;

		final JButton createProjectButton = new JButton( "new project" );
		createProjectButton.addActionListener( e -> createProject() );
		button_gbc_left.gridy = gridy;
		buttonsPanel.add( createProjectButton, button_gbc_left );

		final JButton importButton = new JButton( "import tgmm" );
		importButton.addActionListener( e -> tgmmImportDialog.showImportDialog( windowManager.getSpimData(), windowManager.getModel() ) );
		button_gbc_right.gridy = gridy;
		buttonsPanel.add( importButton, button_gbc_right );

		++gridy;

		final JButton saveProjectButton = new JButton( "save project" );
		saveProjectButton.addActionListener( e -> saveProject() );
		button_gbc_left.gridy = gridy;
		buttonsPanel.add( saveProjectButton, button_gbc_left );

		final JButton loadProjectButton = new JButton( "load project" );
		loadProjectButton.addActionListener( e -> loadProject() );
		button_gbc_right.gridy = gridy;
		buttonsPanel.add( loadProjectButton, button_gbc_right );

		final Container content = getContentPane();
		content.add( buttonsPanel, BorderLayout.NORTH );

		setDefaultCloseOperation( WindowConstants.DISPOSE_ON_CLOSE );
		addWindowListener( new WindowAdapter()
		{
			@Override
			public void windowClosed( final WindowEvent e )
			{
				project = null;
				if ( windowManager != null )
					windowManager.closeAllWindows();
				windowManager = null;
			}
		} );

		setDefaultCloseOperation( WindowConstants.EXIT_ON_CLOSE );
		pack();
	}

	public void open( final MamutProject project ) throws IOException, SpimDataException
	{
		/*
		 * Load Model
		 */
		final Model model = new Model();
		if ( project.getRawModelFile() != null )
			model.loadRaw( project.getRawModelFile() );

		/*
		 * Load SpimData
		 */
		final String spimDataXmlFilename = project.getDatasetXmlFile().getAbsolutePath();
		final SpimDataMinimal spimData = new XmlIoSpimDataMinimal().load( spimDataXmlFilename );

		this.project = project;

		if ( windowManager != null )
			windowManager.closeAllWindows();

		windowManager = new WindowManager( spimDataXmlFilename, spimData, model, keyconf );

		/*
		 * Feature calculation.
		 */

		/*
		 * TODO FIXE Ugly hack to get proper service instantiation. Fix it by
		 * proposing a proper Command decoupled from the GUI.
		 */
		final Context context = new Context();
		final MamutFeatureComputerService featureComputerService = context.getService( MamutFeatureComputerService.class );
		final Dialog featureComputationDialog = new FeatureAndTagDialog( this, windowManager.getModel(), featureComputerService );
		featureComputationDialog.setSize( 400, 400 );

		toggleFeatureComputationDialogAction = new ToggleDialogAction( "feature computation", featureComputationDialog );
	}

	public void saveProject( final File projectFile ) throws IOException
	{
		File modelFile = project.getRawModelFile();
		if ( modelFile == null )
		{
			modelFile = MamutProject.deriveRawModelFile( projectFile );
			project.setRawModelFile( modelFile );
		}

		project.setBasePath( projectFile.getParentFile() );

		final Model model = windowManager.getModel();
		model.saveRaw( modelFile );

		new MamutProjectIO().save( project, projectFile.getAbsolutePath() );
	}

	public void loadProject( final File projectFile ) throws IOException, SpimDataException
	{
		open( new MamutProjectIO().load( projectFile.getAbsolutePath() ) );
	}

	public void saveProject()
	{
		String fn = proposedProjectFile == null ? null : proposedProjectFile.getAbsolutePath();

		File file = FileChooser.chooseFile(
				this,
				fn,
				new XmlFileFilter(),
				"Save MaMuT Project File",
				FileChooser.DialogType.SAVE );
		if ( file == null )
			return;

		fn = file.getAbsolutePath();
		if ( !fn.endsWith( ".xml" ) )
			file = new File( fn + ".xml" );

		if ( !file.equals( proposedProjectFile ) )
			project.setRawModelFile( MamutProject.deriveRawModelFile( file ) );

		try
		{
			proposedProjectFile = file;
			saveProject( proposedProjectFile );
		}
		catch ( final IOException e )
		{
			e.printStackTrace();
		}
	}

	public void loadProject()
	{
		final String fn = proposedProjectFile == null ? null : proposedProjectFile.getAbsolutePath();
		final File file = FileChooser.chooseFile(
				this,
				fn,
				new XmlFileFilter(),
				"Open MaMuT Project File",
				FileChooser.DialogType.LOAD );
		if ( file == null )
			return;

		try
		{
			proposedProjectFile = file;
			loadProject( proposedProjectFile );
		}
		catch ( final IOException | SpimDataException e )
		{
			e.printStackTrace();
		}
	}

	public void createProject()
	{
		final File file = FileChooser.chooseFile(
				this,
				null,
				new XmlFileFilter(),
				"Open BigDataViewer File",
				FileChooser.DialogType.LOAD );
		if ( file == null )
			return;

		try
		{
			open( new MamutProject( file.getParentFile(), file, null ) );
		}
		catch ( final IOException | SpimDataException e )
		{
			e.printStackTrace();
		}
	}

	public void toggleFeaturesDialog()
	{
		if ( toggleFeatureComputationDialogAction != null )
			toggleFeatureComputationDialogAction.actionPerformed( null );
	}

	/**
	 * Try to load {@link InputTriggerConfig} from files in this order:
	 * <ol>
	 * <li>"keyconfig.yaml" in the current directory.
	 * <li>".mastodon/keyconfig.yaml" in the user's home directory.
	 * </ol>
	 */
	static InputTriggerConfig getInputTriggerConfig()
	{
		InputTriggerConfig conf = null;

		// try "keyconfig.yaml" in current directory
		if ( new File( "keyconfig.yaml" ).isFile() )
		{
			try
			{
				conf = new InputTriggerConfig( YamlConfigIO.read( "keyconfig.yaml" ) );
			}
			catch ( final IOException e )
			{}
		}

		// try "~/.mastodon/keyconfig.yaml"
		if ( conf == null )
		{
			final String fn = System.getProperty( "user.home" ) + "/.mastodon/keyconfig.yaml";
			if ( new File( fn ).isFile() )
			{
				try
				{
					conf = new InputTriggerConfig( YamlConfigIO.read( fn ) );
				}
				catch ( final IOException e )
				{}
			}
		}

		if ( conf == null )
		{
			conf = new InputTriggerConfig();
		}

		return conf;
	}

	public static void main( final String[] args ) throws IOException, SpimDataException, InvocationTargetException, InterruptedException
	{
		final String bdvFile = "samples/datasethdf5.xml";
		final String modelFile = "samples/model_revised.raw";
		final MamutProject project = new MamutProject( new File( "." ), new File( bdvFile ), new File( modelFile ) );
//		final MamutProject project = new MamutProjectIO().load( "samples/mamutproject.xml" );

		System.setProperty( "apple.laf.useScreenMenuBar", "true" );

		final InputTriggerConfig keyconf = getInputTriggerConfig();
		final MainWindow mw = new MainWindow( keyconf );
		mw.setVisible( true );

		mw.open( project );
//		mw.proposedProjectFile = new File( "/Users/pietzsch/Desktop/data/TGMM_METTE/project2.xml" );
//		mw.loadProject( new File( "/Users/pietzsch/Desktop/data/TGMM_METTE/project.xml" ) );
//		mw.createProject();
//		mw.loadProject();
		SwingUtilities.invokeAndWait( () -> {
			mw.windowManager.createBigDataViewer();
			mw.windowManager.createTrackScheme();
//			YamlConfigIO.write( new InputTriggerDescriptionsBuilder( keyconf ).getDescriptions(), new PrintWriter( System.out ) );
		} );
//		WindowManager.DumpInputConfig.writeToYaml( System.getProperty( "user.home" ) + "/.mastodon/keyconfig.yaml", mw.windowManager );
	}
}
