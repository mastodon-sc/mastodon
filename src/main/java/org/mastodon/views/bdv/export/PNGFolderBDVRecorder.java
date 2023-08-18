/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2022 Tobias Pietzsch, Jean-Yves Tinevez
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
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.mastodon.ui.coloring.ColorBarOverlay;
import org.mastodon.views.bdv.overlay.OverlayGraphRenderer;

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
