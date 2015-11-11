package net.trackmate.revised.trackscheme.display;

import java.awt.Graphics;
import java.awt.Graphics2D;

import net.imglib2.RealLocalizable;
import net.imglib2.RealPoint;
import net.imglib2.ui.OverlayRenderer;
import net.trackmate.graph.collection.RefList;
import net.trackmate.revised.trackscheme.ScreenEdge;
import net.trackmate.revised.trackscheme.ScreenEntities;
import net.trackmate.revised.trackscheme.ScreenVertex;
import net.trackmate.revised.trackscheme.ScreenVertexRange;
import net.trackmate.revised.trackscheme.TrackSchemeHighlight;

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

	protected final TrackSchemeHighlight< ?, ? > highlight;

	protected int highlightedVertexId;

	private int minTimepoint = 0;

	private int maxTimepoint = 100;

	private int currentTimepoint = 0;

	public AbstractTrackSchemeOverlay( final TrackSchemeHighlight< ?, ? > highlight, final TrackSchemeOptions options )
	{
		this.highlight = highlight;
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
			highlightedVertexId = highlight.getHighlightedVertexId();

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

	public void mouseOverHighlight( final int x, final int y )
	{
		final ScreenEntities ent = getScreenEntities();
		if ( ent != null )
		{
			final RealPoint pos = new RealPoint( x, y );
			for ( final ScreenVertex v : ent.getVertices() )
			{
				if ( isInsidePaintedVertex( pos, v ) )
				{
					highlight.highlightVertex( v.getTrackSchemeVertexId() );
					return;
				}
			}
		}
		highlight.highlightVertex( -1 );
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
	 * Set the timepoint range of the dataset.
	 *
	 * @param minTimepoint
	 * @param maxTimepoint
	 */
	public void setTimepointRange( final int minTimepoint, final int maxTimepoint )
	{
		this.minTimepoint = minTimepoint;
		this.maxTimepoint = maxTimepoint;
	}

	/**
	 * Set the current timepoint.
	 *
	 * @param timepoint
	 */
	public void setCurrentTimepoint( final int timepoint )
	{
		this.currentTimepoint  = timepoint;
	}

	protected int getMinTimepoint()
	{
		return minTimepoint;
	}

	protected int getMaxTimepoint()
	{
		return maxTimepoint;
	}

	protected int getCurrentTimepoint()
	{
		return currentTimepoint;
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

	protected abstract boolean isInsidePaintedVertex( final RealLocalizable pos, final ScreenVertex vertex );

	protected abstract void paintBackground( Graphics2D g2, ScreenEntities screenEntities );

	protected abstract void beforeDrawVertex( Graphics2D g2 );

	protected abstract void drawVertex( Graphics2D g2, ScreenVertex vertex );

	protected abstract void beforeDrawVertexRange( Graphics2D g2 );

	protected abstract void drawVertexRange( Graphics2D g2, ScreenVertexRange range );

	protected abstract void beforeDrawEdge( Graphics2D g2 );

	protected abstract void drawEdge( Graphics2D g2, ScreenEdge edge, ScreenVertex vs, ScreenVertex vt );
}
