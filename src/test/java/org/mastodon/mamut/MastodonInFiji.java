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
package org.mastodon.mamut;

import java.io.IOException;
import java.util.Locale;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.mastodon.mamut.launcher.MastodonLauncherCommand;
import org.mastodon.mamut.project.MamutImagePlusProject;
import org.scijava.Context;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import mpicbg.spim.data.SpimDataException;

public class MastodonInFiji
{

	public static void main( final String[] args ) throws Exception
	{
		setSystemLookAndFeelAndLocale();
		ImageJ.main( args );

		final String path = "/Users/tinevez/Desktop/mitosis.tif";

		final ImagePlus imp = IJ.openImage( path );
//		final ImagePlus imp = IJ.openVirtual( path );

		imp.show();

		final MastodonLauncherCommand launcher = new MastodonLauncherCommand();
		try (Context context = new Context())
		{
			context.inject( launcher );
			launcher.run();
		}
	}

	public static void main2( final String[] args ) throws IOException, SpimDataException
	{
		setSystemLookAndFeelAndLocale();
		ImageJ.main( args );
		
		final String path = "/Users/tinevez/Desktop/mitosis.tif";
		
		final ImagePlus imp = IJ.openImage( path );
//		final ImagePlus imp = IJ.openVirtual( path );
		
		imp.show();

		final MamutImagePlusProject project = new MamutImagePlusProject( imp );

		final WindowManager wm = new WindowManager( new Context() );
		wm.getProjectManager().open( project );
		
		new MainWindow( wm ).setVisible( true );
	}

	private static final void setSystemLookAndFeelAndLocale()
	{
		Locale.setDefault( Locale.ROOT );
		try
		{
			UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
		}
		catch ( ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e )
		{
			e.printStackTrace();
		}
	}
}
