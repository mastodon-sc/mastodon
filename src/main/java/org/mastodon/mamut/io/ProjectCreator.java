/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2023 Tobias Pietzsch, Jean-Yves Tinevez
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
package org.mastodon.mamut.io;

import java.io.File;
import java.io.IOException;

import org.mastodon.mamut.ProjectModel;
import org.mastodon.mamut.io.project.MamutProject;
import org.mastodon.mamut.io.project.MamutProjectIO;
import org.scijava.Context;

import ij.ImagePlus;
import mpicbg.spim.data.SpimDataException;

/**
 * Static methods to create new Mastodon Mamut projects from images.
 */
public class ProjectCreator
{

	/**
	 * Creates a new project from a BDV/XML file.
	 * 
	 * @param file
	 *            the BDV file.
	 * @param context
	 *            the current context.
	 * @return a new {@link ProjectModel}.
	 * @throws SpimDataException
	 *             if the BDV file cannot be opened properly.
	 */
	public static ProjectModel createProjectFromBdvFile( final File file, final Context context ) throws SpimDataException
	{
		final MamutProject project = MamutProjectIO.fromBdvFile( file );
		try
		{
			return ProjectLoader.open( project, context );
		}
		catch ( final IOException e )
		{
			// Should not happen because the data model and the GUI state are empty.
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Creates a new project from an {@link ImagePlus}.
	 * 
	 * @param imp
	 *            the source image.
	 * @param context
	 *            the current context.
	 * @return a new {@link ProjectModel}.
	 * @throws SpimDataException
	 *             SpimDataException if the project points to a BDV file for
	 *             image data, and that BDV cannot be opened properly.
	 */
	public static ProjectModel createProjectFromImp( final ImagePlus imp, final Context context ) throws SpimDataException
	{
		final MamutProject project = MamutProjectIO.fromImagePlus( imp );
		try
		{
			return ProjectLoader.open( project, context );
		}
		catch ( final IOException e )
		{
			// Should not happen because the data model and the GUI state are empty.
			e.printStackTrace();
		}
		return null;
	}
}
