package org.mastodon.project;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;
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

	public InputStream getInputStream( final String fn ) throws IOException
	{
		final InputStream is = zipFile.getInputStream( new ZipEntry( fn ) );
		if ( is != null )
			return is;

		throw new FileNotFoundException( "Entry \"" + fn + "\" not found in \"" + zipFile.getName() + "\"" );
	}

	public Collection< String > listFile( final String fn )
	{
		return Collections.list( zipFile.entries() )
			.stream()
			.filter( e -> e.getName().startsWith( fn + "/" ) )
			.map( e -> e.getName() )
			.map( s -> s.replace( fn + "/", "" ) )
			.collect( Collectors.toList() );
	}
}
