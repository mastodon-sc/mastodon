package net.trackmate.trackscheme;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.util.List;

import net.imglib2.ui.OverlayRenderer;

/**
 * A {@link OverlayRenderer} that paints {@link ScreenEntities} of a trackscheme
 * graph. Comprises methods to paint vertices, edges, and dense vertex ranges.
 * It has no layout capabilities of its own, just paints layouted screen
 * objects.
 *
 * @author Tobias Pietzsch <tobias.pietzsch@gmail.com>
 */
public class GraphLayoutOverlay implements OverlayRenderer
{
	private int width;

	private int height;

	private ScreenEntities entities;

	private ScreenEdgeList edges;

	private ScreenVertexList vertices;

	private ScreenVertex vs;

	private ScreenVertex vt;

	public static final double minDisplayVertexDist = 20.0;

	public static final double maxDisplayVertexSize = 100.0;

	public static final double minDisplaySimplifiedVertexDist = 5.0;

	public static final double simplifiedVertexRadius = 3.0;

	private final Font font = new Font( "SansSerif", Font.PLAIN, 9 );

	private final double avgLabelLetterWidth = 5.0;

	private final Color edgeColor = Color.black;

	private final Color vertexFillColor = Color.white;

	private final Color vertexDrawColor = Color.black;

	private final Color selectedVertexFillColor = new Color( 128, 255, 128 );

	private final Color selectedVertexDrawColor = Color.black;

	private final Color simplifiedVertexFillColor = Color.black;

	private final Color selectedSimplifiedVertexFillColor = new Color( 0, 128, 0 );

	private final Color vertexRangeColor = new Color( 128, 128, 128 );

	@Override
	public synchronized void drawOverlays( final Graphics g )
	{
		final Graphics2D g2 = ( Graphics2D ) g;
		g2.setColor( edgeColor );

		final ScreenEntities ent = entities;
		if ( ent != null )
		{
			edges = ( ScreenEdgeList ) ent.edges; // TODO: get rid of cast
			vertices = ( ScreenVertexList ) ent.vertices; // TODO: get rid of
															// cast
			final List< ScreenVertexRange > vertexRanges = ent.vertexRanges;
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
					final double d = vertex.getVertexDist();
					if ( d >= minDisplayVertexDist )
						drawVertex( g2, vertex );
					else if ( d >= minDisplaySimplifiedVertexDist )
						drawVertexSimplified( g2, vertex );
				}
			}
			if ( vertexRanges != null )
			{
				g2.setColor( vertexRangeColor );
				for ( final ScreenVertexRange range : vertexRanges )
				{
					drawVertexRange( g2, range );
				}
			}

			if ( vertices != null )
			{
				vertices.releaseRef( vs );
				vertices.releaseRef( vt );
			}
		}
	}

	private void drawVertex( final Graphics2D g2, final ScreenVertex vertex )
	{
		final double spotdiameter = Math.min( vertex.getVertexDist() - 10.0, maxDisplayVertexSize );
		final double spotradius = ( int ) ( spotdiameter / 2 );
		final int sd = ( int ) spotdiameter;

		final double x = vertex.getX();
		final double y = vertex.getY();

		final boolean selected = vertex.isSelected();
		g2.setColor( selected ? selectedVertexFillColor : vertexFillColor );
		g2.fillOval( ( int ) ( x - spotradius ), ( int ) ( y - spotradius ), sd, sd );
		g2.setColor( selected ? selectedVertexDrawColor : vertexDrawColor );
		g2.drawOval( ( int ) ( x - spotradius ), ( int ) ( y - spotradius ), sd, sd );

		final int maxLabelLength = ( int ) ( spotdiameter / avgLabelLetterWidth );
		if ( maxLabelLength > 2 )
		{
			String label = vertex.getLabel();
			if ( label.length() > maxLabelLength )
				label = label.substring( 0, maxLabelLength - 2 ) + "...";

			final FontRenderContext frc = g2.getFontRenderContext();
			final TextLayout layout = new TextLayout( label, font, frc );
			final Rectangle2D bounds = layout.getBounds();
			final float tx = ( float ) ( x - bounds.getCenterX() );
			final float ty = ( float ) ( y - bounds.getCenterY() );
			layout.draw( g2, tx, ty );
		}
	}

	private void drawVertexSimplified( final Graphics2D g2, final ScreenVertex vertex )
	{
		final double spotradius = simplifiedVertexRadius;
		final double x = vertex.getX();
		final double y = vertex.getY();
		g2.setColor( vertex.isSelected() ? selectedSimplifiedVertexFillColor : simplifiedVertexFillColor );
		g2.fillOval( ( int ) ( x - spotradius ), ( int ) ( y - spotradius ), ( int ) ( 2 * spotradius ), ( int ) ( 2 * spotradius ) );
	}

	private void drawVertexRange( final Graphics2D g2, final ScreenVertexRange range )
	{
		final int x = ( int ) range.getMinX();
		final int y = ( int ) range.getMinY();
		final int w = ( int ) range.getMaxX() - x;
		final int h = ( int ) range.getMaxY() - y;
		g2.fillRect( x, y, w, h );
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
