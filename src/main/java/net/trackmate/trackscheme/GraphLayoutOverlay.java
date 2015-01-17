package net.trackmate.trackscheme;

import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;

import net.imglib2.ui.OverlayRenderer;

public class GraphLayoutOverlay implements OverlayRenderer
{
	private int width;
	private int height;

	@Override
	public void drawOverlays( final Graphics g )
	{
		final Graphics2D g2 = ( Graphics2D ) g;
		test( g2, 100, 100, 12379 );
	}

	final Font font = new Font( "SansSerif", Font.PLAIN, 9 );

	private void test( final Graphics2D g2, final double x, final double y, final int id )
	{
		final double spotradius = 10.0;
		g2.drawOval( ( int ) ( x - spotradius ) , ( int ) ( y - spotradius ), ( int ) ( 2 * spotradius ), ( int ) ( 2 * spotradius ) );

		final String text = Integer.toString( id );
		final FontRenderContext frc = g2.getFontRenderContext();
		final TextLayout layout = new TextLayout( text, font, frc );
		final Rectangle2D bounds = layout.getBounds();
		final float tx = ( float ) ( x - bounds.getCenterX() );
		final float ty = ( float ) ( y - bounds.getCenterY() );
		layout.draw( g2, tx, ty );
	}

	@Override
	public void setCanvasSize( final int width, final int height )
	{
		this.width = width;
		this.height = height;
	}
}
