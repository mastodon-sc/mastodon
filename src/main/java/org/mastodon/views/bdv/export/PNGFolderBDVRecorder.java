package org.mastodon.views.bdv.export;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.mastodon.views.bdv.overlay.OverlayGraphRenderer;
import org.mastodon.views.trackscheme.display.ColorBarOverlay;

import bdv.export.ProgressWriter;
import bdv.viewer.ViewerPanel;

public class PNGFolderBDVRecorder extends AbstractBDVRecorder
{

	private final File targetFolder;

	protected PNGFolderBDVRecorder(
			final ViewerPanel viewer,
			final OverlayGraphRenderer< ?, ? > tracksOverlay,
			final ColorBarOverlay colorBarOverlay,
			final ProgressWriter progressWriter,
			final File targetFolder )
	{
		super( viewer, tracksOverlay, colorBarOverlay, progressWriter );
		this.targetFolder = targetFolder;
	}

	@Override
	protected void initializeRecorder( final int width, final int height )
	{}

	@Override
	protected void writeFrame( final BufferedImage bi, final int timepoint )
	{
		try
		{
			ImageIO.write( bi, "png", new File( String.format( "%s/img-%03d.png", targetFolder, timepoint ) ) );
		}
		catch ( final IOException e )
		{
			e.printStackTrace();
		}
	}

	@Override
	protected void closeRecorder()
	{}
}
