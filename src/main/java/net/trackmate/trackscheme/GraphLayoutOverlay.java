package net.trackmate.trackscheme;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.List;

import net.imglib2.ui.OverlayRenderer;
import net.trackmate.trackscheme.laf.TrackSchemeLAF;

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

	private final TrackSchemeLAF laf;

	public GraphLayoutOverlay( final TrackSchemeLAF laf )
	{
		this.laf = laf;
	}

	@Override
	public synchronized void drawOverlays( final Graphics g )
	{
		final Graphics2D g2 = ( Graphics2D ) g;

		final ScreenEntities ent = entities;
		if ( ent != null )
		{
			edges = ent.getEdges();
			vertices = ent.getVertices();
			final List< ScreenVertexRange > vertexRanges = ent.getVertexRanges();
			ScreenVertex vs = null;
			ScreenVertex vt = null;
			if ( vertices != null )
			{
				vs = vertices.createRef();
				vt = vertices.createRef();
			}

			if ( edges != null )
			{
				laf.beforeDrawEdge( g2 );
				for ( final ScreenEdge edge : edges )
				{
					vertices.get( edge.getSourceScreenVertexIndex(), vs );
					vertices.get( edge.getTargetScreenVertexIndex(), vt );
					laf.drawEdge( g2, edge, vs, vt );
				}
			}
			if ( vertices != null )
			{
				laf.beforeDrawVertex( g2 );
				for ( final ScreenVertex vertex : vertices )
				{
					laf.drawVertex( g2, vertex );
				}
			}
			if ( vertexRanges != null )
			{
				laf.beforeDrawVertexRange( g2 );
				for ( final ScreenVertexRange range : vertexRanges )
				{
					laf.drawVertexRange( g2, range );
				}
			}

			if ( vertices != null )
			{
				vertices.releaseRef( vs );
				vertices.releaseRef( vt );
			}
		}
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
