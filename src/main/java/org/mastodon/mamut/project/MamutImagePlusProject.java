package org.mastodon.mamut.project;

import java.io.File;

import ij.ImagePlus;
import ij.io.FileInfo;

/**
 * For Mamut projects that are based on an ImagePlus instead of a BDV file.
 * Ultimately, the imp will have to be converted, but this subclass offers the
 * means for Mastodon to play with an ImagePlus opened in Fiji in the meantime.
 */
public class MamutImagePlusProject extends MamutProject
{

	private final ImagePlus imp;

	public MamutImagePlusProject( final ImagePlus imp )
	{
		super( null, getImagePath( imp ) );
		this.imp = imp;
	}

	public ImagePlus getImagePlus()
	{
		return imp;
	}

	private static File getImagePath( final ImagePlus imp )
	{
		final FileInfo fileInfo = imp.getOriginalFileInfo();
		String imageFileName;
		String imageFolder;
		if ( null != fileInfo )
		{
			imageFileName = fileInfo.fileName;
			imageFolder = fileInfo.directory;
		}
		else
		{
			imageFileName = imp.getShortTitle();
			imageFolder = "";
		}
		return new File( imageFolder, imageFileName );
	}

}
