package org.mastodon.project;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ReadZip implements Closeable
{
	private final ZipFile zipFile;

	public ReadZip( final String fn ) throws IOException
	{
		this( new File( fn ) );
	}

	public ReadZip( final File f ) throws IOException
	{
		zipFile = new ZipFile( f );
	}

	@Override
	public void close() throws IOException
	{
		zipFile.close();
	}

	public InputStream getInputStream( String fn ) throws IOException
	{
		final InputStream is = zipFile.getInputStream( new ZipEntry( fn ) );
		if ( is != null )
			return is;

		throw new FileNotFoundException( "Entry \"" + fn + "\" not found in \"" + zipFile.getName() + "\"" );
	}
}
