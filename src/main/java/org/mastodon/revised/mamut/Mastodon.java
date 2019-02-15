package org.mastodon.revised.mamut;

import java.io.IOException;
import java.util.Locale;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

import org.mastodon.project.MamutProject;
import org.mastodon.project.MamutProjectIO;
import org.scijava.Context;
import org.scijava.command.Command;
import org.scijava.command.ContextCommand;
import org.scijava.plugin.Plugin;

import mpicbg.spim.data.SpimDataException;

@Plugin( type = Command.class, menuPath = "Plugins>Mastodon (preview)" )
public class Mastodon extends ContextCommand
{
	/* TEMPORARY */
	public WindowManager windowManager;

	/* TEMPORARY */
	public MainWindow mainWindow;

	@Override
	public void run()
	{
		System.setProperty( "apple.laf.useScreenMenuBar", "true" );
		windowManager = new WindowManager( getContext() );
		mainWindow = new MainWindow( windowManager );
		mainWindow.setVisible( true );
	}

	// FOR TESTING ONLY!
	public void openProject( final MamutProject project ) throws IOException, SpimDataException
	{
		windowManager.projectManager.open( project );
	}

	// FOR TESTING ONLY!
	public void setExitOnClose()
	{
		mainWindow.setDefaultCloseOperation( WindowConstants.EXIT_ON_CLOSE );
	}

	public static void main( final String[] args ) throws Exception
	{
		Locale.setDefault( Locale.US );
		UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );

		final Mastodon mastodon = new Mastodon();
		new Context().inject( mastodon );
		mastodon.run();
		mastodon.mainWindow.setDefaultCloseOperation( WindowConstants.EXIT_ON_CLOSE );

		final WindowManager windowManager = mastodon.windowManager;

//		final String bdvFile = "samples/datasethdf5.xml";
//		final MamutProject project = new MamutProject( new File( "samples/mamutproject" ), new File( bdvFile ) );
//		final MamutProject project = new MamutProjectIO().load( "/Volumes/External/Data/Mastodon/Tassos200" );
//		final MamutProject project = new MamutProject( null, new File( "x=1000 y=1000 z=100 sx=1 sy=1 sz=10 t=400.dummy" ) );
//		final MamutProject project = new MamutProjectIO().load( "samples/mamutproject.mastodon" );
//		final MamutProject project = new MamutProjectIO().load( "/Users/pietzsch/Desktop/Mastodon/merging/Mastodon-files_SimView2_20130315/1.SimView2_20130315_Mastodon_Automat-segm-t0-t300" );
//		final MamutProject project = new MamutProjectIO().load( "/Users/pietzsch/Desktop/Mastodon/merging/Mastodon-files_SimView2_20130315/5.SimView2_20130315_Mastodon_Automat-segm-t0-t300_JG" );
		final MamutProject project = new MamutProjectIO().load( "/Users/pietzsch/Desktop/Mastodon/merging/testcopy.mastodon" );

		windowManager.projectManager.open( project );
//		mw.proposedProjectFile = new File( "/Users/pietzsch/Desktop/data/TGMM_METTE/project2.xml" );
//		mw.loadProject( new File( "/Users/pietzsch/Desktop/data/TGMM_METTE/project.xml" ) );
//		mw.createProject();
//		mw.loadProject();
		SwingUtilities.invokeAndWait( () -> {
			windowManager.createBigDataViewer();
			windowManager.createTrackScheme();
//			YamlConfigIO.write( new InputTriggerDescriptionsBuilder( keyconf ).getDescriptions(), new PrintWriter( System.out ) );
		} );

//		DumpInputConfig.writeDefaultConfigToYaml( System.getProperty( "user.home" ) + "/Desktop/DEFAULT.keyconfig.yaml", new Context() );
	}
}
