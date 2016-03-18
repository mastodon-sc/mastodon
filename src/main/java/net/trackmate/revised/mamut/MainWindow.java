package net.trackmate.revised.mamut;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import javax.swing.filechooser.FileFilter;

import org.scijava.ui.behaviour.io.InputTriggerConfig;
import org.scijava.ui.behaviour.io.yaml.YamlConfigIO;

import bdv.spimdata.SpimDataMinimal;
import bdv.spimdata.XmlIoSpimDataMinimal;
import mpicbg.spim.data.SpimDataException;
import net.trackmate.revised.model.mamut.Model;

public class MainWindow extends JFrame
{
	private static final long serialVersionUID = 1L;

	private final InputTriggerConfig keyconf;

	private MamutProject project;

	private WindowManager windowManager;

	private final JFileChooser fileChooser;

	private File proposedProjectFile;

	private TgmmImportDialog tgmmImportDialog;

	public MainWindow( final InputTriggerConfig keyconf )
	{
		super( "test" );
		this.keyconf = keyconf;

		fileChooser = new JFileChooser();
		fileChooser.setFileFilter( new FileFilter()
		{
			@Override
			public String getDescription()
			{
				return "xml files";
			}

			@Override
			public boolean accept( final File f )
			{
				if ( f.isDirectory() )
					return true;
				if ( f.isFile() )
				{
					final String s = f.getName();
					final int i = s.lastIndexOf( '.' );
					if ( i > 0 && i < s.length() - 1 )
					{
						final String ext = s.substring( i + 1 ).toLowerCase();
						return ext.equals( "xml" );
					}
				}
				return false;
			}
		} );

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
			final String name = projectFile.getAbsolutePath();
			if ( name.endsWith( ".xml" ) )
				modelFile = new File( name.substring( 0, name.length() - ".xml".length() ) + ".raw" );
			else
				modelFile = new File( name + ".raw" );
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
		fileChooser.setDialogTitle( "Save MaMuT Project File" );
		fileChooser.setSelectedFile( proposedProjectFile );
		final int returnVal = fileChooser.showSaveDialog( null );
		if ( returnVal == JFileChooser.APPROVE_OPTION )
		{
			proposedProjectFile = fileChooser.getSelectedFile();
			try
			{
				saveProject( proposedProjectFile );
			}
			catch ( final IOException e )
			{
				e.printStackTrace();
			}
		}
	}

	public void loadProject()
	{
		fileChooser.setDialogTitle( "Open MaMuT Project File" );
		fileChooser.setSelectedFile( proposedProjectFile );
		final int returnVal = fileChooser.showOpenDialog( null );
		if ( returnVal == JFileChooser.APPROVE_OPTION )
		{
			try
			{
				proposedProjectFile = fileChooser.getSelectedFile();
				loadProject( proposedProjectFile );
			}
			catch ( final IOException | SpimDataException e )
			{
				e.printStackTrace();
			}
		}
	}

	public void createProject()
	{
		fileChooser.setDialogTitle( "Open BigDataViewer File" );
		final int returnVal = fileChooser.showOpenDialog( null );
		if ( returnVal == JFileChooser.APPROVE_OPTION )
		{
			try
			{
				final File file = fileChooser.getSelectedFile();
				final SpimDataMinimal spimData = new XmlIoSpimDataMinimal().load(
						file.getAbsolutePath() );
				open( new MamutProject( file.getParentFile(), file, null ) );
			}
			catch ( final IOException | SpimDataException e )
			{
				e.printStackTrace();
			}
		}
	}

	public static void main( final String[] args ) throws IOException, SpimDataException
	{
		final String bdvFile = "samples/datasethdf5.xml";
		final String modelFile = "samples/model_revised.raw";
		final MamutProject project = new MamutProject( new File( "." ), new File( bdvFile ), new File( modelFile ) );
//		final MamutProject project = new MamutProjectIO().load( "samples/mamutproject.xml" );

		/*
		 * Load keyconfig
		 */
		InputTriggerConfig keyconf;
		try
		{
			keyconf = new InputTriggerConfig( YamlConfigIO.read( "samples/keyconf.yaml" ) );
		}
		catch ( final IOException e )
		{
			keyconf = new InputTriggerConfig();
		}

		System.setProperty( "apple.laf.useScreenMenuBar", "true" );

		final MainWindow mw = new MainWindow( keyconf );
		mw.setDefaultCloseOperation( WindowConstants.EXIT_ON_CLOSE );
		mw.pack();
		mw.setVisible( true );

		mw.open( project );
//		mw.fileChooser.setSelectedFile( new File( "/Users/pietzsch/Desktop/data/TGMM_METTE/project.xml" ) );
//		mw.fileChooser.setSelectedFile( new File( "/Users/pietzsch/TGMM/data/tifs/datasethdf5.xml" ) );
//		mw.loadProject( new File( "/Users/pietzsch/Desktop/data/TGMM_METTE/project.xml" ) );
//		mw.createProject();
		mw.windowManager.createBigDataViewer();
		mw.windowManager.createTrackScheme();
//		WindowManager.DumpInputConfig.writeToYaml( System.getProperty( "user.home" ) + "/.tm3/mamutkeyconfig.yaml", mw.windowManager );
	}
}
