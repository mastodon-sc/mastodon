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
 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
 */
public abstract class AbstractTrackSchemeOverlay implements OverlayRenderer
{
	private int width;

	private int height;

	/**
	 * The {@link ScreenEntities} that are actually drawn on the canvas.
	 */
	private ScreenEntities entities;

	/**
	 * {@link ScreenEntities} that have been previously
	 * {@link #setScreenEntities(ScreenEntities) set} for painting. Whenever new
	 * entities are set, these are stored here and marked {@link #pending}. Whenever
	 * entities are painted and new entities are pending, the new entities are painted
	 * to the screen. Before doing this, the entities previously used for painting
	 * are swapped into {@link #pendingEntities}. This is used for double-buffering.
	 */
	private ScreenEntities pendingEntities;

	/**
	 * Whether new entitites are pending.
	 */
	private boolean pending;

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
	 * Set the {@link ScreenEntities} to paint.
	 *
	 * @param entities
	 *            {@link ScreenEntities} to paint.
	 */
	public synchronized ScreenEntities setScreenEntities( final ScreenEntities entities )
	{
		final ScreenEntities tmp = pendingEntities;
		pendingEntities = entities;
		pending = true;
		return tmp;
	}

	/**
	 * Provides subclass access to {@link ScreenEntities} to paint.
	 * Implements double-buffering.
	 *
	 * @return current {@link ScreenEntities}.
	 */
	protected synchronized ScreenEntities getScreenEntities()
	{
		synchronized ( this )
		{
			if ( pending )
			{
				final ScreenEntities tmp = entities;
				entities = pendingEntities;
				pendingEntities = tmp;
				pending = false;
			}
		}
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
