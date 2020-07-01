package org.mastodon.mamut.project;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class WriteZip implements Closeable
{
	private final ZipOutputStream zos;

	private ZipEntryStream current;

	public WriteZip( String fn ) throws IOException
	{
		this( new File( fn ) );
	}

	public WriteZip( File f ) throws IOException
	{
		zos = new ZipOutputStream( new FileOutputStream( f ) );
		zos.setMethod( ZipOutputStream.DEFLATED );
		zos.setLevel( 0 );
		current = null;
	}

	public OutputStream getOutputStream( String fn ) throws IOException
	{
		if ( current != null && current.isOpen )
			throw new IOException( "OutputStream for previous entry \"" + current.entry + "\" is still open" );
		current = new ZipEntryStream( new ZipEntry( fn ) );
		return current;
	}

	@Override
	public void close() throws IOException
	{
		zos.close();
	}

	private class ZipEntryStream extends OutputStream
	{
		boolean isOpen;

		ZipEntry entry;

		ZipEntryStream( ZipEntry entry ) throws IOException
		{
			zos.putNextEntry( entry );
			isOpen = true;
		}

		@Override
		public void write( final int b ) throws IOException
		{
			zos.write( b );
		}

		@Override
		public void write( final byte[] b ) throws IOException
		{
			zos.write( b );
		}

		@Override
		public void write( final byte[] b, final int off, final int len ) throws IOException
		{
			zos.write( b, off, len );
		}

		@Override
		public void flush() throws IOException
		{
			zos.flush();
		}

		@Override
		public void close() throws IOException
		{
			zos.closeEntry();
			isOpen = false;
		}
	}
}
