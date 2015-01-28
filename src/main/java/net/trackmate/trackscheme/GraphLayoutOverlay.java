package net.trackmate.trackscheme;

import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.util.List;

import net.imglib2.ui.OverlayRenderer;

public class GraphLayoutOverlay implements OverlayRenderer
{
	private int width;

	private int height;

	private List< ScreenVertex > vertices;

	@Override
	public void drawOverlays( final Graphics g )
	{
		final Graphics2D g2 = ( Graphics2D ) g;
		if ( vertices != null )
		{
			for ( final ScreenVertex vertex : vertices )
			{
				drawVertex( g2, vertex );
			}
		}
//		test( g2, 100, 100, 12379 );
	}

	final Font font = new Font( "SansSerif", Font.PLAIN, 9 );

	private void test( final Graphics2D g2, final double x, final double y, final int id )
	{
		final double spotradius = 10.0;
		g2.drawOval( ( int ) ( x - spotradius ), ( int ) ( y - spotradius ), ( int ) ( 2 * spotradius ), ( int ) ( 2 * spotradius ) );

		final String text = Integer.toString( id );
		final FontRenderContext frc = g2.getFontRenderContext();
		final TextLayout layout = new TextLayout( text, font, frc );
		final Rectangle2D bounds = layout.getBounds();
		final float tx = ( float ) ( x - bounds.getCenterX() );
		final float ty = ( float ) ( y - bounds.getCenterY() );
		layout.draw( g2, tx, ty );
	}

	private void drawVertex( final Graphics2D g2, final ScreenVertex vertex )
	{
		final double spotradius = 10.0;

		final double x = vertex.getX();
		final double y = vertex.getY();
		final String label = vertex.getLabel();

		g2.drawOval( ( int ) ( x - spotradius ), ( int ) ( y - spotradius ), ( int ) ( 2 * spotradius ), ( int ) ( 2 * spotradius ) );

		final FontRenderContext frc = g2.getFontRenderContext();
		final TextLayout layout = new TextLayout( label, font, frc );
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

	public int getWidth()
	{
		return width;
	}

	public int getHeight()
	{
		return height;
	}

	public void setVertices( final List< ScreenVertex > vertices )
	{
		this.vertices = vertices;
	}
}
