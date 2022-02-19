/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2021 Tobias Pietzsch, Jean-Yves Tinevez
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
package org.mastodon.mamut;

import java.io.IOException;
import java.util.Locale;

import javax.swing.UIManager;
import javax.swing.WindowConstants;

import org.mastodon.mamut.project.MamutProject;
import org.mastodon.mamut.project.MamutProjectIO;
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

	// FOR TESTING ONLY!
	public WindowManager getWindowManager()
	{
		return windowManager;
	}

	public static void main( final String[] args ) throws Exception
	{
		Locale.setDefault( Locale.US );
		UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );

		try (final Context context = new Context())
		{
			final Mastodon mastodon = new Mastodon();
			context.inject( mastodon );
			mastodon.run();
			final MainWindow mw = mastodon.mainWindow;
			mw.setDefaultCloseOperation( WindowConstants.EXIT_ON_CLOSE );


//			final String bdvFile = "samples/datasethdf5.xml";
//			final MamutProject project = new MamutProject( null, new File( bdvFile ) );
//			final MamutProject project = new MamutProjectIO().load( "/Volumes/External/Data/Mastodon/Tassos200" );
//			final MamutProject project = new MamutProject( null, new File( "x=1000 y=1000 z=100 sx=1 sy=1 sz=10 t=400.dummy" ) );
			final MamutProject project = new MamutProjectIO().load( "samples/drosophila_crop_3_spots.mastodon" );

			final WindowManager windowManager = mastodon.windowManager;
			windowManager.projectManager.open( project );

//			mw.proposedProjectFile = new File( "/Users/pietzsch/Desktop/data/TGMM_METTE/project2.xml" );
//			mw.loadProject( new File( "/Users/pietzsch/Desktop/data/TGMM_METTE/project.xml" ) );
//			mw.createProject();
//			mw.loadProject();
//			SwingUtilities.invokeAndWait( () -> {
//				windowManager.createBigDataViewer();
//				windowManager.createTrackScheme();
//				YamlConfigIO.write( new InputTriggerDescriptionsBuilder( keyconf ).getDescriptions(), new PrintWriter( System.out ) );
//			} );

//			DumpInputConfig.writeDefaultConfigToYaml( System.getProperty( "user.home" ) + "/Desktop/DEFAULT.keyconfig.yaml", new Context() );
		}
	}
}
