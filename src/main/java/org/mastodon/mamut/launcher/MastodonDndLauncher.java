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
package org.mastodon.mamut.launcher;

import java.io.IOException;
import java.util.ArrayList;

import org.mastodon.mamut.MainWindow;
import org.mastodon.mamut.ProjectModel;
import org.scijava.io.AbstractIOPlugin;
import org.scijava.io.IOPlugin;
import org.scijava.io.location.FileLocation;
import org.scijava.io.location.Location;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

@Plugin( type = IOPlugin.class )
public class MastodonDndLauncher extends AbstractIOPlugin< Object >
{

	@Parameter
	private LogService logService;

	@Override
	public boolean supportsOpen( final Location source )
	{
		final String sourcePath = source.getURI().getPath();
		logService.debug( "MastodonDndLauncher was questioned: " + sourcePath );

		if ( !( source instanceof FileLocation ) )
			return false;
		return sourcePath.endsWith( ".mastodon" );
	}

	@Override
	public Object open( final Location source ) throws IOException
	{
		logService.debug( "MastodonDndLauncher was asked to open: " + source.getURI().getPath() );
		final FileLocation fsource = source instanceof FileLocation ? ( FileLocation ) source : null;
		if ( fsource == null )
			return null; // NB: shouldn't happen... (in theory)

		final String projectPath = fsource.getFile().getAbsolutePath();

		// make sure that the menus appear on top of the screen
		// to look natively in the Apple world
		System.setProperty( "apple.laf.useScreenMenuBar", "true" );

		try
		{
			final ProjectModel appModel = LauncherUtil.openWithDialog( projectPath, getContext(), null );
			final MainWindow mainWindow = new MainWindow( appModel );
			mainWindow.setVisible( true );
		}
		catch ( final Exception e )
		{
			logService.error( "Error reading Mastodon project file: " + projectPath );
			logService.error( "Error was: " + e.getMessage() );
		}

		return FAKE_INPUT;
	}

	// the "innocent" product of the (hypothetical) file reading...
	private static final Object FAKE_INPUT = new ArrayList<>( 0 );

	@Override
	public Class< Object > getDataType()
	{
		return Object.class;
	}
}
