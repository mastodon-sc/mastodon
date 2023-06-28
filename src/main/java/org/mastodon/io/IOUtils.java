package org.mastodon.io;

import java.io.File;

public class IOUtils
{

	public static boolean mkdirs( final String fileName )
	{
		final File dir = new File( fileName ).getParentFile();
		return dir == null ? false : dir.mkdirs();
	}
}
