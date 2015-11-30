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
import net.trackmate.revised.trackscheme.TrackSchemeGraph;
import net.trackmate.revised.trackscheme.TrackSchemeHighlight;
import net.trackmate.revised.trackscheme.TrackSchemeVertex;

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
	protected final ScreenEntities entities;

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

	protected final TrackSchemeHighlight highlight;

	protected int highlightedVertexId;

	private int minTimepoint = 0;

	private int maxTimepoint = 100;

	private int currentTimepoint = 0;


	public AbstractTrackSchemeOverlay(
			final TrackSchemeGraph< ?, ? > graph,
			final TrackSchemeHighlight highlight,
			final TrackSchemeOptions options )
	{
		this.highlight = highlight;
		width = options.values.getWidth();
		height = options.values.getHeight();
		entities = new ScreenEntities( graph );
	}

	@Override
	public void drawOverlays( final Graphics g )
	{
		final Graphics2D g2 = ( Graphics2D ) g;

		swapScreenEntities();

		highlightedVertexId = highlight.getHighlightedVertexId();

		paintBackground( g2, entities );

		final RefList< ScreenEdge > edges = entities.getEdges();
		final RefList< ScreenVertex > vertices = entities.getVertices();
		final RefList< ScreenVertexRange > vertexRanges = entities.getRanges();

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

	public int getEdgeIdAt( final int x, final int y, final double tolerance )
	{
		synchronized ( entities )
		{
			final RealPoint pos = new RealPoint( x, y );
			final RefList< ScreenVertex > vertices = entities.getVertices();
			final ScreenVertex vt = vertices.createRef();
			final ScreenVertex vs = vertices.createRef();
			for ( final ScreenEdge e : entities.getEdges() )
			{
				vertices.get( e.getSourceScreenVertexIndex(), vs );
				vertices.get( e.getTargetScreenVertexIndex(), vt );
				if ( distanceToPaintedEdge( pos, e, vs, vt ) <= tolerance ) { return e.getTrackSchemeEdgeId(); }
			}
		}
		return -1;
	}

	/**
	 * Returns the internal pool index of the {@link TrackSchemeVertex}
	 * currently painted on this display at screen coordinates specified by
	 * {@code x} and {@code y}.
	 * <p>
	 * This method exists to facilitate writing mouse handlers.
	 *
	 * @param x
	 *            the x screen coordinate
	 * @param y
	 *            the y screen coordinate
	 * @return the internal pool index of the {@link TrackSchemeVertex} at
	 *         {@code (x, y)}, or -1 if there is no vertex at this position.
	 */
	public int getVertexIdAt( final int x, final int y )
	{
		synchronized ( entities )
		{
			final RealPoint pos = new RealPoint( x, y );
			for ( final ScreenVertex v : entities.getVertices() )
				if ( isInsidePaintedVertex( pos, v ) )
					return v.getTrackSchemeVertexId();
			return -1;
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
	private synchronized ScreenEntities swapScreenEntities()
	{
		if ( pending )
		{
			synchronized ( entities )
			{
				entities.set( pendingEntities );
				pending = false;
			}
		}
		return entities;
	}

	protected abstract boolean isInsidePaintedVertex( final RealLocalizable pos, final ScreenVertex vertex );

	protected abstract double distanceToPaintedEdge( final RealLocalizable pos, final ScreenEdge edge, ScreenVertex source, ScreenVertex target );

	protected abstract void paintBackground( Graphics2D g2, ScreenEntities screenEntities );

	protected abstract void beforeDrawVertex( Graphics2D g2 );

	protected abstract void drawVertex( Graphics2D g2, ScreenVertex vertex );

	protected abstract void beforeDrawVertexRange( Graphics2D g2 );

	protected abstract void drawVertexRange( Graphics2D g2, ScreenVertexRange range );

	protected abstract void beforeDrawEdge( Graphics2D g2 );

	protected abstract void drawEdge( Graphics2D g2, ScreenEdge edge, ScreenVertex vs, ScreenVertex vt );
}
