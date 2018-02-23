package org.mastodon.revised.mamut;

import java.awt.event.WindowAdapter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Locale;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.scijava.Context;
import org.scijava.command.Command;
import org.scijava.command.ContextCommand;
import org.scijava.plugin.Plugin;

import mpicbg.spim.data.SpimDataException;

@Plugin( type = Command.class, menuPath = "Plugins>Mastodon (preview)" )
public class Mastodon extends ContextCommand
{
	private WindowManager windowManager;

	private MainWindow mainWindow;

	@Override
	public void run()
	{
		System.setProperty( "apple.laf.useScreenMenuBar", "true" );
		windowManager = new WindowManager( getContext() );
		mainWindow = new MainWindow( windowManager );
		mainWindow.setVisible( true );
	}

	public static void main( final String[] args ) throws IOException, SpimDataException, InvocationTargetException, InterruptedException, ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException
	{
		Locale.setDefault( Locale.US );
		UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );

		final Mastodon mastodon = new Mastodon();
		new Context().inject( mastodon );
		mastodon.run();
		mastodon.mainWindow.addWindowListener( new WindowAdapter()
		{
			@Override
			public void windowClosed( final java.awt.event.WindowEvent e )
			{
				// Close JVM gracefully.
				System.exit( 0 );
			};
		} );

		final WindowManager windowManager = mastodon.windowManager;

//		final String bdvFile = "samples/datasethdf5.xml";
//		final MamutProject project = new MamutProject( new File( "samples/mamutproject" ), new File( bdvFile ) );
//		final MamutProject project = new MamutProjectIO().load( "/Volumes/External/Data/Mastodon/Tassos200" );
		final MamutProject project = new MamutProjectIO().load( "samples/mamutproject" );

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
//		WindowManager.DumpInputConfig.writeToYaml( System.getProperty( "user.home" ) + "/.mastodon/keyconfig.yaml", windowManager );
	}
}
