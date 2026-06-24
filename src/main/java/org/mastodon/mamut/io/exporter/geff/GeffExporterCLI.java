/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2025 Tobias Pietzsch, Jean-Yves Tinevez
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
package org.mastodon.mamut.io.exporter.geff;

import java.io.IOException;

import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.io.ProjectLoader;
import org.scijava.Context;

import mpicbg.spim.data.SpimDataException;

/**
 * Command-line interface for exporting a Mastodon project to a Geff Zarr file.
 * <p>
 * Usage: {@code GeffExporterCLI <input.mastodon> <output.geff>}
 */
public class GeffExporterCLI
{

	public static void main( final String[] args )
	{
		if ( args.length != 2 )
		{
			System.err.println( "Usage: GeffExporterCLI <input.mastodon> <output.geff>" );
			System.exit( 1 );
		}

		final String mastodonFile = args[ 0 ];
		final String geffPath = args[ 1 ];

		try ( final Context context = new Context() )
		{
			System.out.println( "Loading project: " + mastodonFile );
			final ProjectModel projectModel = ProjectLoader.open( mastodonFile, context, false, true );

			System.out.println( "Exporting to Geff: " + geffPath );
			GeffExporter.exportGeff( projectModel, geffPath );

			System.out.println( "Done." );
		}
		catch ( final IOException e )
		{
			System.err.println( "I/O error: " + e.getMessage() );
			System.exit( 1 );
		}
		catch ( final SpimDataException e )
		{
			System.err.println( "Failed to load image data: " + e.getMessage() );
			System.exit( 1 );
		}
		System.exit( 0 );
	}
}
