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

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

import org.mastodon.ui.coloring.ColorBarOverlay;
import org.mastodon.views.bdv.overlay.OverlayGraphRenderer;

import bdv.cache.CacheControl;
import bdv.export.ProgressWriter;
import bdv.util.Prefs;
import bdv.viewer.BasicViewerState;
import bdv.viewer.ViewerPanel;
import bdv.viewer.ViewerState;
import bdv.viewer.overlay.ScaleBarOverlayRenderer;
import bdv.viewer.render.MultiResolutionRenderer;
import bdv.viewer.render.RenderTarget;
import bdv.viewer.render.awt.BufferedImageRenderResult;
import net.imglib2.Cursor;
import net.imglib2.display.screenimage.awt.ARGBScreenImage;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.util.LinAlgHelpers;

public abstract class AbstractBDVRecorder
{
	protected final ViewerPanel viewer;

	protected final OverlayGraphRenderer< ?, ? > tracksOverlay;

	protected final ColorBarOverlay colorBarOverlay;

	protected final ProgressWriter progressWriter;

	protected AbstractBDVRecorder(
			final ViewerPanel viewer,
			final OverlayGraphRenderer< ?, ? > tracksOverlay,
			final ColorBarOverlay colorBarOverlay,
			final ProgressWriter progressWriter )
	{
		this.viewer = viewer;
		this.tracksOverlay = tracksOverlay;
		this.colorBarOverlay = colorBarOverlay;
		this.progressWriter = progressWriter;
	}

	public void recordMaxProjectionMovie(
			final int width, final int height,
			final int minTimepointIndex, final int maxTimepointIndex,
			final double stepSize, final int numSteps,
			final boolean projectOverlay )
	{
		initializeRecorder( width, height );

		final ViewerState renderState = new BasicViewerState( viewer.state().snapshot() );
		final int canvasW = viewer.getDisplay().getWidth();
		final int canvasH = viewer.getDisplay().getHeight();

		final AffineTransform3D tGV = new AffineTransform3D();
		renderState.getViewerTransform( tGV );
		tGV.set( tGV.get( 0, 3 ) - canvasW / 2, 0, 3 );
		tGV.set( tGV.get( 1, 3 ) - canvasH / 2, 1, 3 );
		tGV.scale( ( double ) width / canvasW );
		tGV.set( tGV.get( 0, 3 ) + width / 2, 0, 3 );
		tGV.set( tGV.get( 1, 3 ) + height / 2, 1, 3 );

		final AffineTransform3D affine = new AffineTransform3D();

		// get voxel width transformed to current viewer coordinates
		final AffineTransform3D tSV = new AffineTransform3D();
		renderState.getSources().get( 0 ).getSpimSource().getSourceTransform( 0, 0, tSV );
		tSV.preConcatenate( tGV );
		final double[] sO = new double[] { 0, 0, 0 };
		final double[] sX = new double[] { 1, 0, 0 };
		final double[] vO = new double[ 3 ];
		final double[] vX = new double[ 3 ];
		tSV.apply( sO, vO );
		tSV.apply( sX, vX );
		LinAlgHelpers.subtract( vO, vX, vO );
		final double dd = LinAlgHelpers.length( vO );

		final ScaleBarOverlayRenderer scalebar = Prefs.showScaleBarInMovie() ? new ScaleBarOverlayRenderer() : null;

		class MyTarget implements RenderTarget< BufferedImageRenderResult >
		{
			final ARGBScreenImage accumulated = new ARGBScreenImage( width, height );

			final BufferedImageRenderResult renderResult = new BufferedImageRenderResult();

			public void clear()
			{
				for ( final ARGBType acc : accumulated )
					acc.setZero();
			}

			@Override
			public BufferedImageRenderResult getReusableRenderResult()
			{
				return renderResult;
			}

			@Override
			public BufferedImageRenderResult createRenderResult()
			{
				return new BufferedImageRenderResult();
			}

			@Override
			public void setRenderResult( final BufferedImageRenderResult renderResult )
			{
				final BufferedImage bufferedImage = renderResult.getBufferedImage();
				final Img< ARGBType > argbs = ArrayImgs.argbs(
						( ( DataBufferInt ) bufferedImage.getData().getDataBuffer() ).getData(), width, height );
				final Cursor< ARGBType > c = argbs.cursor();
				for ( final ARGBType acc : accumulated )
				{
					final int current = acc.get();
					final int in = c.next().get();
					acc.set( ARGBType.rgba(
							Math.max( ARGBType.red( in ), ARGBType.red( current ) ),
							Math.max( ARGBType.green( in ), ARGBType.green( current ) ),
							Math.max( ARGBType.blue( in ), ARGBType.blue( current ) ),
							Math.max( ARGBType.alpha( in ), ARGBType.alpha( current ) ) ) );
				}
			}

			@Override
			public final int getWidth()
			{
				return width;
			}

			@Override
			public int getHeight()
			{
				return height;
			}
		}
		final MyTarget target = new MyTarget();
		final MultiResolutionRenderer renderer = new MultiResolutionRenderer(
				target, () -> {}, new double[] { 1 }, 0, 1, null, false,
				viewer.getOptionValues().getAccumulateProjectorFactory(), new CacheControl.Dummy() );

		// Mastodon overlays.
		if ( tracksOverlay != null )
			tracksOverlay.setCanvasSize( width, height );

		if ( colorBarOverlay != null )
			colorBarOverlay.setCanvasSize( width, height );

		// Loop over time.
		progressWriter.setProgress( 0 );
		for ( int timepoint = minTimepointIndex; timepoint <= maxTimepointIndex; ++timepoint )
		{
			target.clear();
			renderState.setCurrentTimepoint( timepoint );

			for ( int step = 0; step < numSteps; ++step )
			{
				affine.set(
						1, 0, 0, 0,
						0, 1, 0, 0,
						0, 0, 1, -dd * stepSize * ( step - numSteps / 2 ) );
				affine.concatenate( tGV );
				renderState.setViewerTransform( affine );
				renderer.requestRepaint();
				renderer.paint( renderState );
			}

			final BufferedImage bi = target.accumulated.image();
			final Graphics2D g2 = bi.createGraphics();
			g2.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );

			if ( Prefs.showScaleBarInMovie() )
			{
				g2.setClip( 0, 0, width, height );
				scalebar.setViewerState( renderState );
				scalebar.paint( g2 );
			}

			/*
			 * Mastodon overlays.
			 */

			g2.setClip( 0, 0, width, height );
			g2.setRenderingHint( RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON );

			if ( tracksOverlay != null )
			{
				tracksOverlay.timePointChanged( timepoint );
				if ( projectOverlay )
				{
					for ( int step = 0; step < numSteps; ++step )
					{
						affine.set(
								1, 0, 0, 0,
								0, 1, 0, 0,
								0, 0, 1, -dd * stepSize * ( step - numSteps / 2 ) );
						affine.concatenate( tGV );

						if ( tracksOverlay != null )
						{
							tracksOverlay.transformChanged( affine );
							tracksOverlay.drawOverlays( g2 );
						}
					}
				}
				else
				{
					tracksOverlay.transformChanged( tGV );
					tracksOverlay.drawOverlays( g2 );
				}
			}
			if ( colorBarOverlay != null )
				colorBarOverlay.drawOverlays( g2 );

			writeFrame( bi, timepoint );
			progressWriter.setProgress(
					( double ) ( timepoint - minTimepointIndex + 1 ) / ( maxTimepointIndex - minTimepointIndex + 1 ) );
		}

		closeRecorder();
	}

	public void record(
			final int width,
			final int height,
			final int minTimepointIndex,
			final int maxTimepointIndex )
	{
		initializeRecorder( width, height );

		final ViewerState renderState = new BasicViewerState( viewer.state().snapshot() );
		final int canvasW = viewer.getDisplay().getWidth();
		final int canvasH = viewer.getDisplay().getHeight();

		final AffineTransform3D affine = new AffineTransform3D();
		renderState.getViewerTransform( affine );
		affine.set( affine.get( 0, 3 ) - canvasW / 2, 0, 3 );
		affine.set( affine.get( 1, 3 ) - canvasH / 2, 1, 3 );
		affine.scale( ( double ) width / canvasW );
		affine.set( affine.get( 0, 3 ) + width / 2, 0, 3 );
		affine.set( affine.get( 1, 3 ) + height / 2, 1, 3 );
		renderState.setViewerTransform( affine );

		// Scale bar.
		final ScaleBarOverlayRenderer scalebar = Prefs.showScaleBarInMovie() ? new ScaleBarOverlayRenderer() : null;

		// BDV image.
		class MyTarget implements RenderTarget< BufferedImageRenderResult >
		{
			final BufferedImageRenderResult renderResult = new BufferedImageRenderResult();

			@Override
			public BufferedImageRenderResult getReusableRenderResult()
			{
				return renderResult;
			}

			@Override
			public BufferedImageRenderResult createRenderResult()
			{
				return new BufferedImageRenderResult();
			}

			@Override
			public void setRenderResult( final BufferedImageRenderResult renderResult )
			{}

			@Override
			public int getWidth()
			{
				return width;
			}

			@Override
			public int getHeight()
			{
				return height;
			}
		}
		final MyTarget target = new MyTarget();
		final MultiResolutionRenderer renderer = new MultiResolutionRenderer(
				target, () -> {}, new double[] { 1 }, 0, 1, null, false,
				viewer.getOptionValues().getAccumulateProjectorFactory(), new CacheControl.Dummy() );

		// Mastodon overlays.
		if ( tracksOverlay != null )
		{
			tracksOverlay.setCanvasSize( width, height );
			tracksOverlay.transformChanged( affine );
		}
		if ( colorBarOverlay != null )
		{
			colorBarOverlay.setCanvasSize( width, height );
		}

		// Loop over time.
		progressWriter.setProgress( 0 );
		for ( int timepoint = minTimepointIndex; timepoint <= maxTimepointIndex; ++timepoint )
		{
			renderState.setCurrentTimepoint( timepoint );
			renderer.requestRepaint();
			renderer.paint( renderState );

			final BufferedImage bi = target.renderResult.getBufferedImage();
			final Graphics2D g2 = bi.createGraphics();
			g2.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );

			if ( Prefs.showScaleBarInMovie() )
			{
				g2.setClip( 0, 0, width, height );
				scalebar.setViewerState( renderState );
				scalebar.paint( g2 );
			}

			/*
			 * Mastodon overlays.
			 */

			g2.setClip( 0, 0, width, height );
			g2.setRenderingHint( RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON );

			if ( tracksOverlay != null )
			{
				tracksOverlay.timePointChanged( timepoint );
				tracksOverlay.drawOverlays( g2 );
			}
			if ( colorBarOverlay != null )
			{
				colorBarOverlay.drawOverlays( g2 );
			}

			writeFrame( bi, timepoint );
			progressWriter.setProgress(
					( double ) ( timepoint - minTimepointIndex + 1 ) / ( maxTimepointIndex - minTimepointIndex + 1 ) );
		}

		closeRecorder();
	}

	protected abstract void closeRecorder();

	protected abstract void initializeRecorder( int width, int height );

	protected abstract void writeFrame( BufferedImage frame, int timepoint );
}
