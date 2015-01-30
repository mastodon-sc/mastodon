package net.trackmate.trackscheme;

import java.awt.Color;
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

	private ScreenEntities entities;

	private ScreenEdgeList edges;

	private ScreenVertexList vertices;

	private ScreenVertex vs;

	private ScreenVertex vt;

	@Override
	public synchronized void drawOverlays( final Graphics g )
	{
		final Graphics2D g2 = ( Graphics2D ) g;
		final ScreenEntities ent = entities;
		if ( ent != null )
		{
			edges = ( ScreenEdgeList ) ent.edges;
			vertices = ( ScreenVertexList ) ent.vertices;
			if ( vertices != null )
			{
				vs = vertices.createRef();
				vt = vertices.createRef();
			}

			if ( edges != null )
			{
				for ( final ScreenEdge edge : edges )
				{
					drawEdge( g2, edge );
				}
			}
			if ( vertices != null )
			{
				for ( final ScreenVertex vertex : vertices )
				{
//					drawVertex( g2, vertex );
//					drawVertexSimplified( g2, vertex );
				}
			}

			if ( vertices != null )
			{
				vertices.releaseRef( vs );
				vertices.releaseRef( vt );
			}
		}
	}

	final Font font = new Font( "SansSerif", Font.PLAIN, 9 );

	private void drawVertex( final Graphics2D g2, final ScreenVertex vertex )
	{
		final double spotradius = 10.0;

		final double x = vertex.getX();
		final double y = vertex.getY();
		final String label = vertex.getLabel();

		g2.setColor( Color.WHITE );
		g2.fillOval( ( int ) ( x - spotradius ), ( int ) ( y - spotradius ), ( int ) ( 2 * spotradius ), ( int ) ( 2 * spotradius ) );
		g2.setColor( Color.BLACK );
		g2.drawOval( ( int ) ( x - spotradius ), ( int ) ( y - spotradius ), ( int ) ( 2 * spotradius ), ( int ) ( 2 * spotradius ) );

		final FontRenderContext frc = g2.getFontRenderContext();
		final TextLayout layout = new TextLayout( label, font, frc );
		final Rectangle2D bounds = layout.getBounds();
		final float tx = ( float ) ( x - bounds.getCenterX() );
		final float ty = ( float ) ( y - bounds.getCenterY() );
		layout.draw( g2, tx, ty );
	}

	private void drawVertexSimplified( final Graphics2D g2, final ScreenVertex vertex )
	{
		final double spotradius = 2.0;
		final double x = vertex.getX();
		final double y = vertex.getY();
		g2.setColor( Color.BLACK );
		g2.fillOval( ( int ) ( x - spotradius ), ( int ) ( y - spotradius ), ( int ) ( 2 * spotradius ), ( int ) ( 2 * spotradius ) );
	}

	private void drawEdge( final Graphics2D g2, final ScreenEdge edge )
	{
		vertices.get( edge.getSourceScreenVertexIndex(), vs );
		vertices.get( edge.getTargetScreenVertexIndex(), vt );
		g2.drawLine( ( int ) vs.getX(), ( int ) vs.getY(), ( int ) vt.getX(), ( int ) vt.getY() );
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

	public void setScreenEntities( final ScreenEntities entities )
	{
		this.entities = entities;
	}
}
