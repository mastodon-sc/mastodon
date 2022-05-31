package org.mastodon.views.bdv.export;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import org.mastodon.views.bdv.overlay.OverlayGraphRenderer;
import org.mastodon.views.trackscheme.display.ColorBarOverlay;

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
import net.imglib2.realtransform.AffineTransform3D;

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
			progressWriter.setProgress( ( double ) ( timepoint - minTimepointIndex + 1 ) / ( maxTimepointIndex - minTimepointIndex + 1 ) );
		}

		closeRecorder();
	}

	protected abstract void closeRecorder();

	protected abstract void initializeRecorder( int width, int height );

	protected abstract void writeFrame( BufferedImage frame, int timepoint );
}
