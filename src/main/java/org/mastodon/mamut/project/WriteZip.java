/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2021 Tobias Pietzsch, Jean-Yves Tinevez
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
