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
package org.mastodon.views.bdv.export;

import java.awt.image.BufferedImage;
import java.io.IOException;

import org.mastodon.ui.coloring.ColorBarOverlay;
import org.mastodon.views.bdv.overlay.OverlayGraphRenderer;

import bdv.export.ProgressWriter;
import bdv.viewer.ViewerPanel;
import io.humble.video.Codec;
import io.humble.video.Encoder;
import io.humble.video.MediaPacket;
import io.humble.video.MediaPicture;
import io.humble.video.Muxer;
import io.humble.video.MuxerFormat;
import io.humble.video.PixelFormat;
import io.humble.video.Rational;
import io.humble.video.awt.MediaPictureConverter;
import io.humble.video.awt.MediaPictureConverterFactory;

public class MovieFileBDVRecorder extends AbstractBDVRecorder
{

	private final String filename;

	private final int fps;

	private Muxer muxer;

	private MediaPicture picture;

	private MediaPictureConverter converter;

	private Encoder encoder;

	private MediaPacket packet;

	protected MovieFileBDVRecorder(
			final ViewerPanel viewer,
			final OverlayGraphRenderer< ?, ? > tracksOverlay,
			final ColorBarOverlay colorBarOverlay,
			final ProgressWriter progressWriter,
			final String filename,
			final int fps )
	{
		super( viewer, tracksOverlay, colorBarOverlay, progressWriter );
		this.filename = filename;
		this.fps = fps;
	}

	@Override
	protected void initializeRecorder( final int w, final int h )
	{
		/*
		 * Make sure we only get even numbers for width and height.
		 */
		final int width = Math.round( w / 2 ) * 2;
		final int height = Math.round( h / 2 ) * 2;

		/*
		 * First we create a muxer using the filename to determine code and
		 * format.
		 */
		final Rational framerate = Rational.make( 1, fps );
		this.muxer = Muxer.make( filename, null, null );

		// Determine codec.
		final MuxerFormat format = muxer.getFormat();
		final Codec codec = Codec.findEncodingCodec( format.getDefaultVideoCodecId() );

		// Create an encoder.
		encoder = Encoder.make( codec );
		encoder.setWidth( width );
		encoder.setHeight( height );
		// Default pixel format, assuming it's the most used by codecs.
		final PixelFormat.Type pixelformat = PixelFormat.Type.PIX_FMT_YUV420P;
		encoder.setPixelFormat( pixelformat );
		encoder.setTimeBase( framerate );

		// Global headers if needed.
		if ( format.getFlag( MuxerFormat.Flag.GLOBAL_HEADER ) )
			encoder.setFlag( Encoder.Flag.FLAG_GLOBAL_HEADER, true );

		// Open the encoder.
		encoder.open( null, null );

		// Add this stream to the muxer.
		muxer.addNewStream( encoder );

		// And open the muxer for business.
		try
		{
			muxer.open( null, null );
		}
		catch ( InterruptedException | IOException e )
		{
			e.printStackTrace();
		}

		this.picture = MediaPicture.make(
				encoder.getWidth(),
				encoder.getHeight(),
				pixelformat );
		picture.setTimeBase( framerate );

		packet = MediaPacket.make();
	}

	@Override
	protected void writeFrame( final BufferedImage frame, final int timepoint )
	{
		// Convert BI type to something Humble can harness.
		// Also crop in case we had non-even dimensions.
		final BufferedImage convertedImg =
				new BufferedImage( picture.getWidth(), picture.getHeight(), BufferedImage.TYPE_3BYTE_BGR );
		convertedImg.getGraphics().drawImage( frame, 0, 0, null );

		if ( converter == null )
			converter = MediaPictureConverterFactory.createConverter( convertedImg, picture );

		converter.toPicture( picture, convertedImg, timepoint );

		// Write to output video stream.
		do
		{
			encoder.encode( packet, picture );
			if ( packet.isComplete() )
				muxer.write( packet, false );
		}
		while ( packet.isComplete() );
	}

	@Override
	protected void closeRecorder()
	{
		// Flush.
		do
		{
			encoder.encode( packet, null );
			if ( packet.isComplete() )
				muxer.write( packet, false );
		}
		while ( packet.isComplete() );

		// Close.
		muxer.close();

		// Nullify everything.
		converter = null;
		encoder = null;
		muxer = null;
		packet = null;
		picture = null;
	}
}
