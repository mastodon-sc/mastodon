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
