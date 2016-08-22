package net.trackmate.revised.trackscheme.display;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.concurrent.CopyOnWriteArrayList;

import net.imglib2.RealLocalizable;
import net.imglib2.RealPoint;
import net.imglib2.ui.OverlayRenderer;
import net.trackmate.collection.RefList;
import net.trackmate.revised.trackscheme.ScreenEdge;
import net.trackmate.revised.trackscheme.ScreenEntities;
import net.trackmate.revised.trackscheme.ScreenVertex;
import net.trackmate.revised.trackscheme.ScreenVertexRange;
import net.trackmate.revised.trackscheme.TrackSchemeEdge;
import net.trackmate.revised.trackscheme.TrackSchemeFocus;
import net.trackmate.revised.trackscheme.TrackSchemeGraph;
import net.trackmate.revised.trackscheme.TrackSchemeHighlight;
import net.trackmate.revised.trackscheme.TrackSchemeVertex;
import net.trackmate.revised.trackscheme.display.OffsetHeaders.OffsetHeadersListener;

/**
 * An {@link OverlayRenderer} that paints {@link ScreenEntities} of a
 * TrackScheme graph. Comprises methods to paint vertices, edges, and dense
 * vertex ranges. It has no layout capabilities of its own; it just paints
 * laid-out screen objects.
 * <p>
 * It takes the laid-out {@link ScreenEntities} that it receives with the method
 * {@link #setScreenEntities(ScreenEntities)}, and can deal separately with
 * {@link ScreenVertex} and {@link ScreenVertexRange}.
 * <p>
 * This class is abstract and the details of how to paint vertices, edges and
 * background are delegated to concrete implementations. When the
 * {@link #drawOverlays(Graphics)} method is called, the following sequence of
 * abstract methods is executed:
 * <ol>
 * <li>{@link #paintBackground(Graphics2D, ScreenEntities)} to paint background
 * decorations.
 * <li> {@link #beforeDrawEdge(Graphics2D)} to configure the Graphics2D object
 * prior to painting edges.
 * <li> {@link #drawEdge(Graphics2D, ScreenEdge, ScreenVertex, ScreenVertex)} for
 * each edge.
 * <li>{@link #beforeDrawVertex(Graphics2D)} to configure the Graphics2D object
 * prior to painting vertices.
 * <li> {@link #drawVertex(Graphics2D, ScreenVertex)} for each vertex.
 * <li> {@link #beforeDrawVertexRange(Graphics2D)} to configure the Graphics2D
 * object prior to painting vertex ranges.
 * <li>{@link #drawVertexRange(Graphics2D, ScreenVertexRange)} for each vertex
 * range.
 * </ol>
 * <p>
 * It also offers facilities to interrogate what has been painted where, to
 * facilitate writing user interfaces. For instance, it can return the
 * TrackScheme edge or vertex id near a screen xy coordinate.
 *
 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
 */
public abstract class AbstractTrackSchemeOverlay implements OverlayRenderer, OffsetHeadersListener
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

	private final TrackSchemeGraph< ?, ? > graph;

	protected final TrackSchemeHighlight highlight;

	protected int highlightedVertexId;

	protected int highlightedEdgeId;

	protected final TrackSchemeFocus focus;

	protected int focusedVertexId;

	private int minTimepoint = 0;

	private int maxTimepoint = 100;

	private int currentTimepoint = 0;

	protected boolean isHeaderVisibleX;

	protected int headerWidth;

	protected boolean isHeaderVisibleY;

	protected int headerHeight;

	/**
	 * The {@link OverlayRenderer}s that draw above the background
	 */
	final protected CopyOnWriteArrayList< OverlayRenderer > overlayRenderers;

	/**
	 * Creates a new overlay for the specified TrackScheme graph.
	 *
	 * @param graph
	 *            the graph to paint.
	 * @param highlight
	 *            the highlight model that indicates which vertex is
	 *            highlighted.
	 * @param focus
	 *            the focus model that indicates which vertex is focused.
	 * @param options
	 *            options for TrackScheme look.
	 */
	public AbstractTrackSchemeOverlay(
			final TrackSchemeGraph< ?, ? > graph,
			final TrackSchemeHighlight highlight,
			final TrackSchemeFocus focus,
			final TrackSchemeOptions options )
	{
		this.graph = graph;
		this.highlight = highlight;
		this.focus = focus;
		width = options.values.getWidth();
		height = options.values.getHeight();
		entities = new ScreenEntities( graph );
		overlayRenderers = new CopyOnWriteArrayList<>();
	}

	@Override
	public void drawOverlays( final Graphics g )
	{
		final Graphics2D g2 = ( Graphics2D ) g;
		g2.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );

		swapScreenEntities();

		final TrackSchemeVertex ref = graph.vertexRef();
		final TrackSchemeEdge eref = graph.edgeRef();

		final TrackSchemeVertex h = highlight.getHighlightedVertex( ref );
		highlightedVertexId = ( h == null ) ? -1 : h.getInternalPoolIndex();

		final TrackSchemeEdge he = highlight.getHighlightedEdge( eref );
		highlightedEdgeId = ( he == null ) ? -1 : he.getInternalPoolIndex();

		final TrackSchemeVertex f = focus.getFocusedVertex( ref );
		focusedVertexId = ( f == null ) ? -1 : f.getInternalPoolIndex();

		graph.releaseRef( ref );

		paintBackground( g2, entities );

		// Paint extra overlay if any.
		for ( final OverlayRenderer or : overlayRenderers )
			or.drawOverlays( g );

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

		paintHeaders( g2, entities );

		vertices.releaseRef( vs );
		vertices.releaseRef( vt );
	}

	/**
	 * Returns the {@link TrackSchemeEdge} currently painted on this display at
	 * screen coordinates specified by {@code x} and {@code y} and within a
	 * distance tolerance.
	 * <p>
	 * This method exists to facilitate writing mouse handlers.
	 * <p>
	 * Note that this really only looks at edges that are individually painted
	 * on the screen. Edges inside dense ranges are ignored.
	 *
	 * @param x
	 *            the x screen coordinate
	 * @param y
	 *            the y screen coordinate
	 * @param tolerance
	 *            the maximal distance to the closest edge.
	 * @param ref
	 *            a reference that will be used to retrieve the result.
	 * @return the {@link TrackSchemeEdge} at {@code (x, y)}, or {@code null} if
	 *         there is no edge within the distance tolerance.
	 */
	public TrackSchemeEdge getEdgeAt( final int x, final int y, final double tolerance, final TrackSchemeEdge ref )
	{
		synchronized ( entities )
		{
			final RealPoint pos = new RealPoint( x, y );
			final RefList< ScreenVertex > vertices = entities.getVertices();
			final ScreenVertex vt = vertices.createRef();
			final ScreenVertex vs = vertices.createRef();

			int i = -1;
			for ( final ScreenEdge e : entities.getEdges() )
			{
				vertices.get( e.getSourceScreenVertexIndex(), vs );
				vertices.get( e.getTargetScreenVertexIndex(), vt );
				if ( distanceToPaintedEdge( pos, e, vs, vt ) <= tolerance )
				{
					i = e.getTrackSchemeEdgeId();
					break;
				}
			}

			vertices.releaseRef( vs );
			vertices.releaseRef( vt );

			if ( i < 0 )
				return null;

			graph.getEdgePool().getObject( i, ref );
			return ref;
		}
	}

	/**
	 * Returns the {@link TrackSchemeVertex} currently painted on this display
	 * at screen coordinates specified by {@code x} and {@code y}.
	 * <p>
	 * This method exists to facilitate writing mouse handlers.
	 * <p>
	 * Note that this really only looks at vertices that are individually
	 * painted on the screen. Vertices inside dense ranges are ignored.
	 *
	 * @param x
	 *            the x screen coordinate
	 * @param y
	 *            the y screen coordinate
	 * @param ref
	 *            a reference that will be used to retrieve the result.
	 * @return the {@link TrackSchemeVertex} at
	 *         {@code (x, y)}, or {@code null} if there is no vertex at this position.
	 */
	public TrackSchemeVertex getVertexAt( final int x, final int y, final TrackSchemeVertex ref )
	{
		synchronized ( entities )
		{
			double d2Best = Double.POSITIVE_INFINITY;
			int iBest = -1;
			final RealPoint pos = new RealPoint( x, y );
			for ( final ScreenVertex v : entities.getVertices() )
			{
				if ( isInsidePaintedVertex( pos, v ) )
				{
					final int i = v.getTrackSchemeVertexId();
					if ( i >= 0 )
					{
						final double dx = v.getX() - x;
						final double dy = v.getY() - y;
						final double d2 = dx * dx + dy * dy;
						if ( d2 < d2Best )
						{
							d2Best = d2;
							iBest = i;
						}
					}
				}
			}

			if ( iBest >= 0 )
			{
				graph.getVertexPool().getObject( iBest, ref );
				return ref;
			}

			return null;
		}
	}

	@Override
	public void setCanvasSize( final int width, final int height )
	{
		this.width = width;
		this.height = height;
		for ( final OverlayRenderer overlay : overlayRenderers )
			overlay.setCanvasSize( width, height );
	}

	@Override
	public void updateHeadersVisibility( final boolean isVisibleX, final int width, final boolean isVisibleY, final int height )
	{
		isHeaderVisibleX = isVisibleX;
		headerWidth = isVisibleX ? width : 0;
		isHeaderVisibleY = isVisibleY;
		headerHeight = isVisibleY ? height : 0;
	}

	/**
	 * Returns the width of this overlay.
	 *
	 * @return the width.
	 */
	public int getWidth()
	{
		return width;
	}

	/**
	 * Returns the hight of this overlay.
	 *
	 * @return the height.
	 */
	public int getHeight()
	{
		return height;
	}

	/**
	 * Set the timepoint range of the dataset.
	 *
	 * @param minTimepoint
	 *            the smallest timepoint of the dataset.
	 * @param maxTimepoint
	 *            the largest timepoint of the dataset.
	 */
	public void setTimepointRange( final int minTimepoint, final int maxTimepoint )
	{
		this.minTimepoint = minTimepoint;
		this.maxTimepoint = maxTimepoint;
	}

	/**
	 * Sets the current timepoint.
	 *
	 * @param timepoint
	 *            the current timepoint.
	 */
	public void setCurrentTimepoint( final int timepoint )
	{
		this.currentTimepoint  = timepoint;
	}

	/**
	 * Returns the smallest timepoint of the dataset.
	 *
	 * @return the smallest timepoint of the dataset.
	 */
	protected int getMinTimepoint()
	{
		return minTimepoint;
	}

	/**
	 * Returns the largest timepoint of the dataset.
	 *
	 * @return the largest timepoint of the dataset.
	 */
	protected int getMaxTimepoint()
	{
		return maxTimepoint;
	}

	/**
	 * Returns the current timepoint.
	 *
	 * @return the current timepoint.
	 */
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

	/**
	 * Adds an extra overlay that will be painted along with this one. Overlays
	 * added by this method will be painted after background has been painted,
	 * but before the graph and decoration are painted, so that they "lay below"
	 * the graph and decoration renderings.
	 *
	 * @param overlay
	 *            the overlay to paint.
	 */
	public void addOverlayRenderer( final OverlayRenderer overlay )
	{
		overlayRenderers.add( overlay );
		setCanvasSize( getWidth(), getHeight() );
	}

	/**
	 * Remove an {@link OverlayRenderer}.
	 *
	 * @param renderer
	 *            overlay renderer to remove.
	 */
	public void removeOverlayRenderer( final OverlayRenderer renderer )
	{
		overlayRenderers.remove( renderer );
	}

	/**
	 * Returns {@code true} if the specified <b>layout</b> coordinates are
	 * inside a painted vertex. As the vertex painting shape is implemented by
	 * possibly different concrete classes, they should return whether a point
	 * is inside a vertex or not.
	 *
	 * @param pos
	 *            the layout position.
	 * @param vertex
	 *            the vertex.
	 * @return {@code true} if the position is inside the vertex painted.
	 */
	protected abstract boolean isInsidePaintedVertex( final RealLocalizable pos, final ScreenVertex vertex );

	/**
	 * Returns the distance from a <b>layout</b> position to a specified edge.
	 *
	 * @param pos
	 *            the layout position.
	 * @param edge
	 *            the edge.
	 * @param source
	 *            the edge source vertex.
	 * @param target
	 *            the edge target vertex.
	 * @return the distance from the specified position to the edge.
	 */
	protected abstract double distanceToPaintedEdge( final RealLocalizable pos, final ScreenEdge edge, ScreenVertex source, ScreenVertex target );

	/**
	 * Paints background decorations.
	 *
	 * @param g2
	 *            the graphics object.
	 * @param screenEntities
	 *            the screen entities to paint.
	 */
	protected abstract void paintBackground( Graphics2D g2, ScreenEntities screenEntities );

	/**
	 * Paints overlay decorations.
	 *
	 * @param g2
	 *            the graphics object.
	 * @param screenEntities
	 *            the screen entities to paint.
	 */
	protected abstract void paintHeaders( Graphics2D g2, ScreenEntities screenEntities );

	/**
	 * Configures the graphics object prior to drawing vertices.
	 *
	 * @param g2
	 *            the graphics object.
	 */
	protected abstract void beforeDrawVertex( Graphics2D g2 );

	/**
	 * Paints the specified vertex.
	 *
	 * @param g2
	 *            the graphics object.
	 * @param vertex
	 *            the vertex to paint.
	 */
	protected abstract void drawVertex( Graphics2D g2, ScreenVertex vertex );

	/**
	 * Configures the graphics object prior to drawing vertex ranges.
	 *
	 * @param g2
	 *            the graphics object.
	 */
	protected abstract void beforeDrawVertexRange( Graphics2D g2 );

	/**
	 * Paints the specified vertex range.
	 *
	 * @param g2
	 *            the graphics object.
	 * @param range
	 *            the vertex range to paint.
	 */
	protected abstract void drawVertexRange( Graphics2D g2, ScreenVertexRange range );

	/**
	 * Configures the graphics object prior to drawing edges.
	 *
	 * @param g2
	 *            the graphics object.
	 */
	protected abstract void beforeDrawEdge( Graphics2D g2 );

	/**
	 * Paints the specified edge.
	 *
	 * @param g2
	 *            the graphics object.
	 * @param edge
	 *            the edge to paint.
	 * @param vs
	 *            the edge source vertex.
	 * @param vt
	 *            the edge target vertex.
	 */
	protected abstract void drawEdge( Graphics2D g2, ScreenEdge edge, ScreenVertex vs, ScreenVertex vt );
}
