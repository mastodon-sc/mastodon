package net.trackmate.trackscheme;

import java.awt.Graphics;
import java.awt.Graphics2D;

import net.imglib2.ui.OverlayRenderer;
import net.imglib2.ui.TransformListener;
import net.trackmate.trackscheme.laf.TrackSchemeLAF;

public class CanvasOverlay implements OverlayRenderer, TransformListener< ScreenTransform >
{
	private double minX;

	private double maxX;

	private double minY;

	private double maxY;

	private int screenWidth;

	private int screenHeight;

	private double yScale;

	private double xScale;

	private final TrackSchemeLAF laf;

	public CanvasOverlay( final TrackSchemeLAF laf )
	{
		this.laf = laf;
	}

	@Override
	public void drawOverlays( final Graphics g )
	{
		final Graphics2D g2 = ( Graphics2D ) g;
		laf.paintBackground( g2, minX, maxX, minY, maxY, screenWidth, screenHeight, xScale, yScale );
	}

	@Override
	public void setCanvasSize( final int width, final int height )
	{}

	@Override
	public void transformChanged( final ScreenTransform transform )
	{
		minX = transform.minX;
		maxX = transform.maxX;
		minY = transform.minY;
		maxY = transform.maxY;
		screenWidth = transform.screenWidth;
		screenHeight = transform.screenHeight;
		yScale = ( screenHeight - 1 ) / ( maxY - minY );
		xScale = ( screenWidth - 1 ) / ( maxX - minX );
	}

}
