package org.mastodon.revised.mamut;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import org.mastodon.revised.model.mamut.Model;
import org.mastodon.revised.ui.util.FileChooser;
import org.mastodon.revised.ui.util.XmlFileFilter;
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

	public MainWindow( final InputTriggerConfig keyconf )
	{
		super( "test" );
		this.keyconf = keyconf;

		tgmmImportDialog = new TgmmImportDialog( this );

		final JPanel buttonsPanel = new JPanel();
		buttonsPanel.setLayout( new GridLayout( 7, 1 ) );
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
		buttonsPanel.add( bdvButton );
		buttonsPanel.add( trackschemeButton );
		buttonsPanel.add( Box.createVerticalStrut( 20 ) );

		final JButton importButton = new JButton( "import tgmm" );
		importButton.addActionListener( new ActionListener()
		{
			@Override
			public void actionPerformed( final ActionEvent e )
			{
				tgmmImportDialog.showImportDialog( windowManager.getSpimData(), windowManager.getModel() );
			}
		} );
		buttonsPanel.add( importButton );

		final JButton createProjectButton = new JButton( "new project" );
		createProjectButton.addActionListener( new ActionListener()
		{
			@Override
			public void actionPerformed( final ActionEvent e )
			{
				createProject();
			}
		} );
		buttonsPanel.add( createProjectButton );

		final JButton loadProjectButton = new JButton( "load project" );
		loadProjectButton.addActionListener( new ActionListener()
		{
			@Override
			public void actionPerformed( final ActionEvent e )
			{
				loadProject();
			}
		} );
		buttonsPanel.add( loadProjectButton );

		final JButton saveProjectButton = new JButton( "save project" );
		saveProjectButton.addActionListener( new ActionListener()
		{
			@Override
			public void actionPerformed( final ActionEvent e )
			{
				saveProject();
			}
		} );
		buttonsPanel.add( saveProjectButton );

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
		mw.setDefaultCloseOperation( WindowConstants.EXIT_ON_CLOSE );
		mw.pack();
		mw.setVisible( true );

		mw.open( project );
//		mw.proposedProjectFile = new File( "/Users/pietzsch/Desktop/data/TGMM_METTE/project2.xml" );
//		mw.loadProject( new File( "/Users/pietzsch/Desktop/data/TGMM_METTE/project.xml" ) );
//		mw.createProject();
//		mw.loadProject();
		SwingUtilities.invokeAndWait( () -> {
			mw.windowManager.createBigDataViewer();
			mw.windowManager.createTrackScheme();
		} );
//		WindowManager.DumpInputConfig.writeToYaml( System.getProperty( "user.home" ) + "/.mastodon/keyconfig.yaml", mw.windowManager );
	}
}
