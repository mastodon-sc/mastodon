package net.trackmate.revised.trackscheme.display;

import java.awt.Graphics;
import java.awt.Graphics2D;

import net.imglib2.ui.OverlayRenderer;
import net.trackmate.graph.collection.RefList;
import net.trackmate.revised.trackscheme.ScreenEdge;
import net.trackmate.revised.trackscheme.ScreenEntities;
import net.trackmate.revised.trackscheme.ScreenVertex;
import net.trackmate.revised.trackscheme.ScreenVertexRange;

/**
 * An {@link OverlayRenderer} that paints {@link ScreenEntities} of a trackscheme
 * graph. Comprises methods to paint vertices, edges, and dense vertex ranges.
 * It has no layout capabilities of its own, just paints layouted screen
 * objects.
 *
 * @author Tobias Pietzsch <tobias.pietzsch@gmail.com>
 */
public abstract class AbstractTrackSchemeOverlay implements OverlayRenderer
{
	private int width;

	private int height;

	private ScreenEntities entities;

	public AbstractTrackSchemeOverlay( final TrackSchemeOptions options )
	{
		width = options.values.getWidth();
		height = options.values.getHeight();
		entities = null;
	}

	@Override
	public void drawOverlays( final Graphics g )
	{
		final Graphics2D g2 = ( Graphics2D ) g;

		final ScreenEntities ent = getScreenEntities();
		if ( ent != null )
		{
			paintBackground( g2, ent );

			final RefList< ScreenEdge > edges = ent.getEdges();
			final RefList< ScreenVertex > vertices = ent.getVertices();
			final RefList< ScreenVertexRange > vertexRanges = ent.getRanges();

			final ScreenVertex vt = vertices.createRef();
			final ScreenVertex vs = vertices.createRef();

			beforeDrawEdge( g2 );
			for ( final ScreenEdge edge : edges )
			{
				vertices.get( edge.getSourceScreenVertexIndex(), vs );
				vertices.get( edge.getTargetScreenVertexIndex(), vt );
				drawEdge( g2, edge, vs, vt );
			}

			beforeDrawVertex( g2 );
			for ( final ScreenVertex vertex : vertices )
			{
				drawVertex( g2, vertex );
			}

			beforeDrawVertexRange( g2 );
			for ( final ScreenVertexRange range : vertexRanges )
			{
				drawVertexRange( g2, range );
			}

			vertices.releaseRef( vs );
			vertices.releaseRef( vt );
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

	/**
	 * Set the current {@link ScreenEntities}.
	 *
	 * @param entities
	 *            current {@link ScreenEntities}.
	 */
	public synchronized void setScreenEntities( final ScreenEntities entities )
	{
		this.entities = entities;
	}

	/**
	 * Provides subclass access to current {@link ScreenEntities}.
	 *
	 * @return current {@link ScreenEntities}.
	 */
	protected synchronized ScreenEntities getScreenEntities()
	{
		return entities;
	}

	protected abstract void paintBackground( Graphics2D g2, ScreenEntities screenEntities );

	protected abstract void beforeDrawVertex( Graphics2D g2 );

	protected abstract void drawVertex( Graphics2D g2, ScreenVertex vertex );

	protected abstract void beforeDrawVertexRange( Graphics2D g2 );

	protected abstract void drawVertexRange( Graphics2D g2, ScreenVertexRange range );

	protected abstract void beforeDrawEdge( Graphics2D g2 );

	protected abstract void drawEdge( Graphics2D g2, ScreenEdge edge, ScreenVertex vs, ScreenVertex vt );
}
