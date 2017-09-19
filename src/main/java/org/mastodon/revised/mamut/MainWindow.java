package org.mastodon.revised.mamut;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ExecutionException;

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
import org.scijava.Context;
import org.scijava.ui.behaviour.io.InputTriggerConfig;
import org.scijava.ui.behaviour.io.yaml.YamlConfigIO;

import bdv.spimdata.SpimDataMinimal;
import bdv.spimdata.XmlIoSpimDataMinimal;
import bdv.tools.ToggleDialogAction;
import mpicbg.spim.data.SpimDataException;

public class MainWindow extends JFrame
{
	private static final long serialVersionUID = 1L;

	/*
	 * FIELDS.
	 */

	private final InputTriggerConfig keyconf;

	private MamutProject project;

	private WindowManager windowManager;

	private File proposedProjectFile;

	private final TgmmImportDialog tgmmImportDialog;

	private final JButton featureComputationButton;


	public MainWindow( final InputTriggerConfig keyconf )
	{
		super( "test" );
		setTitle("Mastodon MaMuT");
		this.keyconf = keyconf;

		tgmmImportDialog = new TgmmImportDialog( this );

		final JPanel buttonsPanel = new JPanel();
		final GridBagLayout gbl_buttonsPanel = new GridBagLayout();
		gbl_buttonsPanel.columnWeights = new double[]{1.0, 1.0};
		gbl_buttonsPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
		buttonsPanel.setLayout(gbl_buttonsPanel);
		final JButton bdvButton = new JButton( "bdv" );
		bdvButton.addActionListener( new ActionListener()
		{
			@Override
			public void actionPerformed( final ActionEvent e )
			{
				if ( windowManager != null )
					windowManager.createBigDataViewer();
			}
		} );

		final JLabel lblViews = new JLabel("Views:");
		final GridBagConstraints gbc_lblViews = new GridBagConstraints();
		gbc_lblViews.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblViews.gridwidth = 2;
		gbc_lblViews.insets = new Insets(5, 5, 5, 5);
		gbc_lblViews.gridx = 0;
		gbc_lblViews.gridy = 0;
		buttonsPanel.add(lblViews, gbc_lblViews);
		final GridBagConstraints gbc_bdvButton = new GridBagConstraints();
		gbc_bdvButton.fill = GridBagConstraints.BOTH;
		gbc_bdvButton.insets = new Insets(0, 0, 5, 0);
		gbc_bdvButton.gridx = 1;
		gbc_bdvButton.gridy = 1;
		buttonsPanel.add( bdvButton, gbc_bdvButton );
		final JButton trackschemeButton = new JButton( "trackscheme" );
		trackschemeButton.addActionListener( new ActionListener()
		{
			@Override
			public void actionPerformed( final ActionEvent e )
			{
				if ( windowManager != null )
					windowManager.createTrackScheme();
			}
		} );
		final GridBagConstraints gbc_trackschemeButton = new GridBagConstraints();
		gbc_trackschemeButton.fill = GridBagConstraints.BOTH;
		gbc_trackschemeButton.insets = new Insets(0, 0, 5, 0);
		gbc_trackschemeButton.gridx = 1;
		gbc_trackschemeButton.gridy = 2;
		buttonsPanel.add( trackschemeButton, gbc_trackschemeButton );

		final JSeparator separator = new JSeparator();
		final GridBagConstraints gbc_separator = new GridBagConstraints();
		gbc_separator.fill = GridBagConstraints.HORIZONTAL;
		gbc_separator.gridwidth = 2;
		gbc_separator.insets = new Insets(5, 5, 5, 5);
		gbc_separator.gridx = 0;
		gbc_separator.gridy = 3;
		buttonsPanel.add(separator, gbc_separator);

		final JLabel lblProcessing = new JLabel("Processing:");
		final GridBagConstraints gbc_lblProcessing = new GridBagConstraints();
		gbc_lblProcessing.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblProcessing.gridwidth = 2;
		gbc_lblProcessing.insets = new Insets(5, 5, 5, 5);
		gbc_lblProcessing.gridx = 0;
		gbc_lblProcessing.gridy = 4;
		buttonsPanel.add(lblProcessing, gbc_lblProcessing);

		this.featureComputationButton = new JButton( "features and tags" );
		final GridBagConstraints gbc_featureComputationButton = new GridBagConstraints();
		gbc_featureComputationButton.fill = GridBagConstraints.BOTH;
		gbc_featureComputationButton.insets = new Insets(0, 0, 5, 0);
		gbc_featureComputationButton.gridx = 1;
		gbc_featureComputationButton.gridy = 5;
		buttonsPanel.add( featureComputationButton, gbc_featureComputationButton );

		final JButton importButton = new JButton( "import tgmm" );
		importButton.addActionListener( new ActionListener()
		{
			@Override
			public void actionPerformed( final ActionEvent e )
			{
				tgmmImportDialog.showImportDialog( windowManager.getSpimData(), windowManager.getModel() );
			}
		} );

		final JSeparator separator_1 = new JSeparator();
		final GridBagConstraints gbc_separator_1 = new GridBagConstraints();
		gbc_separator_1.fill = GridBagConstraints.BOTH;
		gbc_separator_1.gridwidth = 2;
		gbc_separator_1.insets = new Insets(5, 5, 5, 5);
		gbc_separator_1.gridx = 0;
		gbc_separator_1.gridy = 6;
		buttonsPanel.add(separator_1, gbc_separator_1);

		final JLabel lblInputOutput = new JLabel("Input / Output:");
		final GridBagConstraints gbc_lblInputOutput = new GridBagConstraints();
		gbc_lblInputOutput.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblInputOutput.gridwidth = 2;
		gbc_lblInputOutput.insets = new Insets(5, 5, 5, 5);
		gbc_lblInputOutput.gridx = 0;
		gbc_lblInputOutput.gridy = 7;
		buttonsPanel.add(lblInputOutput, gbc_lblInputOutput);

		final JButton createProjectButton = new JButton( "new project" );
		createProjectButton.addActionListener( new ActionListener()
		{
			@Override
			public void actionPerformed( final ActionEvent e )
			{
				createProject();
			}
		} );
		final GridBagConstraints gbc_createProjectButton = new GridBagConstraints();
		gbc_createProjectButton.fill = GridBagConstraints.BOTH;
		gbc_createProjectButton.insets = new Insets(0, 0, 5, 5);
		gbc_createProjectButton.gridx = 0;
		gbc_createProjectButton.gridy = 8;
		buttonsPanel.add( createProjectButton, gbc_createProjectButton );
		final GridBagConstraints gbc_importButton = new GridBagConstraints();
		gbc_importButton.fill = GridBagConstraints.BOTH;
		gbc_importButton.insets = new Insets(0, 0, 5, 0);
		gbc_importButton.gridx = 1;
		gbc_importButton.gridy = 8;
		buttonsPanel.add( importButton, gbc_importButton );

		final JButton loadProjectButton = new JButton( "load project" );
		loadProjectButton.addActionListener( new ActionListener()
		{
			@Override
			public void actionPerformed( final ActionEvent e )
			{
				loadProject();
			}
		} );

		final JButton saveProjectButton = new JButton( "save project" );
		saveProjectButton.addActionListener( new ActionListener()
		{
			@Override
			public void actionPerformed( final ActionEvent e )
			{
				saveProject();
			}
		} );
		final GridBagConstraints gbc_saveProjectButton = new GridBagConstraints();
		gbc_saveProjectButton.fill = GridBagConstraints.BOTH;
		gbc_saveProjectButton.insets = new Insets(0, 0, 0, 5);
		gbc_saveProjectButton.gridx = 0;
		gbc_saveProjectButton.gridy = 9;
		buttonsPanel.add( saveProjectButton, gbc_saveProjectButton );
		final GridBagConstraints gbc_loadProjectButton = new GridBagConstraints();
		gbc_loadProjectButton.fill = GridBagConstraints.BOTH;
		gbc_loadProjectButton.gridx = 1;
		gbc_loadProjectButton.gridy = 9;
		buttonsPanel.add( loadProjectButton, gbc_loadProjectButton );

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

		featureComputationButton.addActionListener( new ToggleDialogAction( "feature computation", featureComputationDialog ) );
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

	/**
	 * Try to load {@link InputTriggerConfig} from files in this order:
	 * <ol>
	 * <li>"keyconfig.yaml" in the current directory.
	 * <li>".mastodon/keyconfig.yaml" in the user's home directory.
	 * </ol>
	 */
	static final InputTriggerConfig getInputTriggerConfig()
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

	public static void main( final String[] args ) throws IOException, SpimDataException, InvocationTargetException, InterruptedException, ExecutionException
	{

		final String bdvFile = "samples/datasethdf5.xml";
		final String modelFile = "samples/model_revised.raw";
		final MamutProject project = new MamutProject( new File( "." ), new File( bdvFile ), new File( modelFile ) );
//		final MamutProject project = new MamutProjectIO().load( "samples/mamutproject.xml" );


		System.setProperty( "apple.laf.useScreenMenuBar", "true" );

		final MainWindow mw = new MainWindow( getInputTriggerConfig() );
		mw.open( project );
		mw.setVisible( true );

		SwingUtilities.invokeAndWait( () -> {
			mw.windowManager.createBigDataViewer();
			mw.windowManager.createTrackScheme();
		} );
	}
}
