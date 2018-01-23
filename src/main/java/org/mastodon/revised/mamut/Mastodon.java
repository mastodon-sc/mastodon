package org.mastodon.revised.mamut;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Locale;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.scijava.command.Command;
import org.scijava.plugin.Plugin;

import mpicbg.spim.data.SpimDataException;

@Plugin( type = Command.class, menuPath = "Plugins>Mastodon (preview)" )
public class Mastodon implements Command
{
	@Override
	public void run()
	{
		try
		{
			main( null );
		}
		catch ( IOException | SpimDataException | InterruptedException | InvocationTargetException | ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e )
		{
			throw new RuntimeException( e );
		}
	}

	public static void main( final String[] args ) throws IOException, SpimDataException, InvocationTargetException, InterruptedException, ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException
	{
		Locale.setDefault( Locale.US );
		UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );

		System.setProperty( "apple.laf.useScreenMenuBar", "true" );
		final WindowManager windowManager = new WindowManager();
		new MainWindow( windowManager ).setVisible( true );


		final String bdvFile = "samples/datasethdf5.xml";
		final MamutProject project = new MamutProject( new File( "samples/mamutproject" ), new File( bdvFile ) );
//		final MamutProject project = new MamutProjectIO().load( "samples/mamutproject" );

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
