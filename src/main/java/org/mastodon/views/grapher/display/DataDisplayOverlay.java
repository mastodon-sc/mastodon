/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2021 Tobias Pietzsch, Jean-Yves Tinevez
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package org.mastodon.views.grapher.display;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.concurrent.CopyOnWriteArrayList;

import org.mastodon.collection.RefList;
import org.mastodon.model.FocusModel;
import org.mastodon.model.HighlightModel;
import org.mastodon.util.GeometryUtil;
import org.mastodon.views.grapher.datagraph.DataEdge;
import org.mastodon.views.grapher.datagraph.DataGraph;
import org.mastodon.views.grapher.datagraph.DataVertex;
import org.mastodon.views.grapher.datagraph.ScreenEdge;
import org.mastodon.views.grapher.datagraph.ScreenEntities;
import org.mastodon.views.grapher.datagraph.ScreenVertex;
import org.mastodon.views.grapher.display.OffsetAxes.OffsetAxesListener;
import org.mastodon.views.grapher.display.style.DataDisplayStyle;

import bdv.viewer.OverlayRenderer;

public class DataDisplayOverlay implements OverlayRenderer, OffsetAxesListener
{
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

	private final DataGraph< ?, ? > graph;

	private final HighlightModel< DataVertex, DataEdge > highlight;

	private final FocusModel< DataVertex, DataEdge > focus;

	private int width;

	private int height;

	private int axesWidth;

	private int axesHeight;

	private final DataDisplayStyle style;

	private final PaintDecorations paintDecorations;

	private final PaintGraph paintGraph;

	/**
	 * The {@link OverlayRenderer}s that draw above the background
	 */
	private final CopyOnWriteArrayList< OverlayRenderer > overlayRenderers;

	/**
	 * Creates a new overlay for the specified Data graph.
	 *
	 * @param graph
	 *            the graph to paint.
	 * @param highlight
	 *            the highlight model that indicates which vertex is
	 *            highlighted.
	 * @param focus
	 *            the focus model that indicates which vertex is focused.
	 * @param paintDecorations
	 *            for painting background and headers
	 * @param paintGraph
	 *            for painting vertices and edges
	 * @param options
	 *            options for Data look.
	 */
	public DataDisplayOverlay(
			final DataGraph< ?, ? > graph,
			final HighlightModel< DataVertex, DataEdge > highlight,
			final FocusModel< DataVertex, DataEdge > focus,
			final PaintDecorations paintDecorations,
			final PaintGraph paintGraph,
			final DataDisplayOptions options )
	{
		this.graph = graph;
		this.highlight = highlight;
		this.focus = focus;
		this.paintDecorations = paintDecorations;
		this.paintGraph = paintGraph;
		style = options.values.getStyle();
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

		final DataVertex ref = graph.vertexRef();
		final DataEdge eref = graph.edgeRef();

		final DataVertex h = highlight.getHighlightedVertex( ref );
		final int highlightedVertexId = ( h == null ) ? -1 : h.getInternalPoolIndex();

		final DataEdge he = highlight.getHighlightedEdge( eref );
		final int highlightedEdgeId = ( he == null ) ? -1 : he.getInternalPoolIndex();

		final DataVertex f = focus.getFocusedVertex( ref );
		final int focusedVertexId = ( f == null ) ? -1 : f.getInternalPoolIndex();

		graph.releaseRef( ref );

		paintDecorations.paintBackground( g2, width, height, axesWidth, axesHeight, entities, style );

		// Paint extra overlay if any.
		for ( final OverlayRenderer or : overlayRenderers )
			or.drawOverlays( g );

		final boolean antialiasOffForGraph = entities.getVertices().size() > 10000;
		if ( antialiasOffForGraph )
			g2.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF );
		paintGraph.paintGraph( g2, entities, highlightedVertexId, highlightedEdgeId, focusedVertexId, style );
		if ( antialiasOffForGraph )
			g2.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );

		paintDecorations.paintHeaders( g2, width, height, axesWidth, axesHeight, entities, style );
	}

	/**
	 * Returns the {@link DataEdge} currently painted on this display at screen
	 * coordinates specified by {@code x} and {@code y} and within a distance
	 * tolerance.
	 * <p>
	 * This method exists to facilitate writing mouse handlers.
	 *
	 * @param x
	 *            the x screen coordinate
	 * @param y
	 *            the y screen coordinate
	 * @param tolerance
	 *            the maximal distance to the closest edge.
	 * @param ref
	 *            a reference that will be used to retrieve the result.
	 * @return the {@link DataEdge} at {@code (x, y)}, or {@code null} if there
	 *         is no edge within the distance tolerance.
	 */
	public DataEdge getEdgeAt( final int x, final int y, final double tolerance, final DataEdge ref )
	{
		synchronized ( entities )
		{
			final RefList< ScreenVertex > vertices = entities.getVertices();
			final ScreenVertex vt = vertices.createRef();
			final ScreenVertex vs = vertices.createRef();

			int i = -1;
			for ( final ScreenEdge e : entities.getEdges() )
			{
				vertices.get( e.getSourceScreenVertexIndex(), vs );
				vertices.get( e.getTargetScreenVertexIndex(), vt );
				if ( distanceToPaintedEdge( x, y, e, vs, vt ) <= tolerance )
				{
					i = e.getDataEdgeId();
					break;
				}
			}

			vertices.releaseRef( vs );
			vertices.releaseRef( vt );

			return ( i >= 0 )
					? graph.getEdgePool().getObjectIfExists( i, ref )
					: null;
		}
	}

	/**
	 * Returns the {@link DataVertex} currently painted on this display at
	 * screen coordinates specified by {@code x} and {@code y}.
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
	 * @return the {@link DataVertex} at {@code (x, y)}, or {@code null} if
	 *         there is no vertex at this position.
	 */
	public DataVertex getVertexAt( final int x, final int y, final DataVertex ref )
	{
		synchronized ( entities )
		{
			double d2Best = Double.POSITIVE_INFINITY;
			int iBest = -1;
			for ( final ScreenVertex v : entities.getVertices() )
			{
				if ( paintGraph.isInsidePaintedVertex( x, y, v ) )
				{
					final int i = v.getDataVertexId();
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

			return ( iBest >= 0 )
					? graph.getVertexPool().getObjectIfExists( iBest, ref )
					: null;
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

	public void setXLabel( final String label )
	{
		paintDecorations.setXLabel( label );
	}

	public void setYLabel( final String label )
	{
		paintDecorations.setYLabel( label );
	}

	@Override
	public void updateAxesSize( final int width, final int height )
	{
		axesWidth = width;
		axesHeight = height;
	}

	/**
	 * Returns the width of this overlay.
	 *
	 * @return the width.
	 */
	protected int getWidth()
	{
		return width;
	}

	/**
	 * Returns the hight of this overlay.
	 *
	 * @return the height.
	 */
	protected int getHeight()
	{
		return height;
	}

	/**
	 * Set the {@link ScreenEntities} to paint.
	 *
	 * @param entities
	 *            {@link ScreenEntities} to paint.
	 * @return the previous {@link ScreenEntities}.
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

	public static class DataDisplayOverlayFactory
	{
		public DataDisplayOverlay create(
				final DataGraph< ?, ? > graph,
				final HighlightModel< DataVertex, DataEdge > highlight,
				final FocusModel< DataVertex, DataEdge > focus,
				final DataDisplayOptions options )
		{
			return new DataDisplayOverlay(
					graph,
					highlight,
					focus,
					new PaintDecorations(),
					new PaintGraph(),
					options );
		}
	}

	/**
	 * Returns the distance from a <b>screen</b> position to a specified edge.
	 *
	 * @param x
	 *            the x screen coordinate
	 * @param y
	 *            the y screen coordinate
	 * @param edge
	 *            the edge.
	 * @param source
	 *            the edge source vertex.
	 * @param target
	 *            the edge target vertex.
	 * @return the distance from the specified position to the edge.
	 */
	private static final double distanceToPaintedEdge(
			final double x,
			final double y,
			final ScreenEdge edge,
			final ScreenVertex source,
			final ScreenVertex target )
	{
		final double x1 = source.getX();
		final double y1 = source.getY();
		final double x2 = target.getX();
		final double y2 = target.getY();
		final double d = GeometryUtil.segmentDist( x, y, x1, y1, x2, y2 );
		return d;
	}
}
