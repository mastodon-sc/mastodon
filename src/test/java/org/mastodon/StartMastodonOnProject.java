/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2022 Tobias Pietzsch, Jean-Yves Tinevez
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
package org.mastodon;

import javax.swing.JFileChooser;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.mastodon.mamut.MainWindow;
import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.io.ProjectLoader;
import org.scijava.Context;
import org.scijava.thread.ThreadService;

/**
 * Starts Mastodon on a given project file.
 */
public class StartMastodonOnProject
{

	public static void main( final String... args )
	{
		final String projectPath = fileOpenDialog();
		launch( projectPath );
	}

	public static void launch( final String projectPath )
	{
		try
		{
			UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
		}
		catch ( ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e )
		{
			e.printStackTrace();
		}
		System.setProperty( "apple.laf.useScreenMenuBar", "true" );
		final Context context = new Context();
		final ThreadService threadService = context.getService( ThreadService.class );
		threadService.run( () -> {
			try
			{
				final ProjectModel appModel = ProjectLoader.open( projectPath, context, true, false );
				final MainWindow win = new MainWindow( appModel );
				win.setVisible( true );
				win.setDefaultCloseOperation( WindowConstants.EXIT_ON_CLOSE );
			}
			catch ( final Exception e )
			{
				e.printStackTrace();
			}
		} );
	}

	private static String fileOpenDialog()
	{
		final JFileChooser fileChooser = new JFileChooser( "Open Mastodon Project" );
		fileChooser.setFileFilter( new FileNameExtensionFilter( "Mastodon Project (*.mastodon)", "mastodon" ) );
		fileChooser.showOpenDialog( null );
		return fileChooser.getSelectedFile().getAbsolutePath();
	}
}
