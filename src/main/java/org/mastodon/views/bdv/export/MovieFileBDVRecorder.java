package org.mastodon.views.bdv.export;

import java.awt.image.BufferedImage;
import java.io.IOException;

import org.mastodon.views.bdv.overlay.OverlayGraphRenderer;
import org.mastodon.views.trackscheme.display.ColorBarOverlay;

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
	protected void initializeRecorder( final int width, final int height )
	{
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
		final BufferedImage convertedImg = new BufferedImage( frame.getWidth(), frame.getHeight(), BufferedImage.TYPE_3BYTE_BGR );
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
