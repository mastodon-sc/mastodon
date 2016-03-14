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
import javax.swing.JFrame;
import javax.swing.JPanel;

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

	private WindowManager windowManager;

	public MainWindow( final InputTriggerConfig keyconf )
	{
		super( "test" );
		this.keyconf = keyconf;

		final JPanel buttonsPanel = new JPanel();
		buttonsPanel.setLayout( new GridLayout( 4, 1 ) );
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

		final JButton importButton = new JButton( "import" );
		importButton.addActionListener( new ActionListener()
		{
			@Override
			public void actionPerformed( final ActionEvent e )
			{
				final String bdvFile = "samples/datasethdf5.xml";
				final String modelFile = "samples/model_revised.raw";
			}
		} );
		buttonsPanel.add( importButton );

		final Container content = getContentPane();
		content.add( buttonsPanel, BorderLayout.NORTH );
	}

	public void open( final MamutProject project ) throws IOException, SpimDataException
	{
		if ( windowManager != null )
			windowManager.closeAllWindows();

		/*
		 * Load Model
		 */
		final Model model = new Model();
		model.loadRaw( project.getRawModelFile() );

		/*
		 * Load SpimData
		 */
		final SpimDataMinimal spimData = new XmlIoSpimDataMinimal().load(
				project.getDatasetXmlFile().getAbsolutePath() );

		windowManager = new WindowManager( spimData, model, keyconf );
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
		mw.pack();
		mw.setVisible( true );

		mw.open( project );
	}

}
