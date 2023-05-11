/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2022 Tobias Pietzsch, Jean-Yves Tinevez
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
package org.mastodon.views.trackscheme.display;

import static org.mastodon.views.trackscheme.ScreenVertex.Transition.APPEAR;
import static org.mastodon.views.trackscheme.ScreenVertex.Transition.DISAPPEAR;
import static org.mastodon.views.trackscheme.ScreenVertex.Transition.NONE;
import static org.mastodon.views.trackscheme.ScreenVertex.Transition.SELECTING;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;

import org.mastodon.collection.RefList;
import org.mastodon.util.ColorUtils;
import org.mastodon.util.GeometryUtil;
import org.mastodon.views.trackscheme.ScreenEdge;
import org.mastodon.views.trackscheme.ScreenEntities;
import org.mastodon.views.trackscheme.ScreenVertex;
import org.mastodon.views.trackscheme.ScreenVertex.Transition;
import org.mastodon.views.trackscheme.ScreenVertexRange;
import org.mastodon.views.trackscheme.display.style.TrackSchemeStyle;

/**
 * Painting the TrackScheme graph.
 * <p>
 * In particular, this class
 * </p>
 * <ul>
 * <li>draws vertex as circles with the label inside,</li>
 * <li>offers two sizes of vertices (full and simplified),</li>
 * <li>draws edges as lines.</li>
 * </ul>
 * <p>
 * Colors and strokes are chosen according to a {@link TrackSchemeStyle}.
 * </p>
 * <p>
 * When the
 * {@link #paintGraph(Graphics2D, ScreenEntities, int, int, int, TrackSchemeStyle)}
 * method is called, the following sequence of methods is executed:
 * </p>
 * <ol>
 * <li>{@link #beforeDrawEdges()} to configure the Graphics2D object prior to
 * painting edges.
 * <li>{@link #drawEdge(ScreenEdge, ScreenVertex, ScreenVertex)} for each edge.
 * <li>{@code #beforeDrawVertices()} to configure the Graphics2D object prior to
 * painting vertices.
 * <li>{@code #drawVertex(ScreenVertex)} for each vertex.
 * <li>{@code #beforeDrawVertexRanges()} to configure the Graphics2D object
 * prior to painting vertex ranges.
 * <li>{@code #drawVertexRange(ScreenVertexRange)} for each vertex range.
 * </ol>
 * <p>
 * Subclasses can override some or all of these methods to influence how the
 * graph is drawn.
 * </p>
 *
 * @author Tobias Pietzsch
 */
public class PaintGraph
{
	/*
	 * CONSTANTS
	 */

	private static final double simplifiedVertexRadius = 2.5;

	private static final double simplifiedVertexSelectTolerance = 3.5;

	private static final double minDisplayVertexDist = 17.0;

	private static final double maxDisplayVertexSize = 100.0;

	private static final double minDisplaySimplifiedVertexDist = 5.0;

	private static final double avgLabelLetterWidth = 5.0;

	protected static final float fadeRatio = 0.9f;

	/*
	 * FIELDS
	 */

	protected Graphics2D g2;

	protected int highlightedVertexId;

	protected int highlightedEdgeId;

	protected int focusedVertexId;

	protected TrackSchemeStyle style;

	protected Stroke edgeStroke;

	protected Stroke edgeHighlightStroke;

	protected Stroke edgeGhostStroke;

	private final Rectangle tmpRectangle = new Rectangle();

	public void paintGraph(
			final Graphics2D g2,
			final ScreenEntities entities,
			final int highlightedVertexId,
			final int highlightedEdgeId,
			final int focusedVertexId,
			final TrackSchemeStyle style )
	{
		this.g2 = g2;
		this.highlightedVertexId = highlightedVertexId;
		this.highlightedEdgeId = highlightedEdgeId;
		this.focusedVertexId = focusedVertexId;
		this.style = style;

		final RefList< ScreenEdge > edges = entities.getEdges();
		final RefList< ScreenVertex > vertices = entities.getVertices();
		final RefList< ScreenVertexRange > vertexRanges = entities.getRanges();

		final ScreenVertex vt = vertices.createRef();
		final ScreenVertex vs = vertices.createRef();

		beforeDrawEdges();
		for ( final ScreenEdge edge : edges )
		{
			vertices.get( edge.getSourceScreenVertexIndex(), vs );
			vertices.get( edge.getTargetScreenVertexIndex(), vt );
			drawEdge( edge, vs, vt );
		}

		beforeDrawVertices();
		for ( final ScreenVertex vertex : vertices )
		{
			drawVertex( vertex );
		}

		beforeDrawVertexRanges();
		for ( final ScreenVertexRange range : vertexRanges )
		{
			drawVertexRange( range );
		}

		vertices.releaseRef( vs );
		vertices.releaseRef( vt );
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
	public double distanceToPaintedEdge( final double x, final double y, final ScreenEdge edge,
			final ScreenVertex source, final ScreenVertex target )
	{
		final double x1 = source.getX();
		final double y1 = source.getY();
		final double x2 = target.getX();
		final double y2 = target.getY();
		final double d = GeometryUtil.segmentDist( x, y, x1, y1, x2, y2 );
		return d;
	}

	/**
	 * Returns {@code true} if the specified <b>screen</b> coordinates are
	 * inside a painted vertex. As the vertex painting shape is implemented by
	 * possibly different concrete classes, they should return whether a point
	 * is inside a vertex or not.
	 *
	 * @param x
	 *            the x screen coordinate
	 * @param y
	 *            the y screen coordinate
	 * @param vertex
	 *            the vertex.
	 * @return {@code true} if the position is inside the vertex painted.
	 */
	public boolean isInsidePaintedVertex( final double x, final double y, final ScreenVertex vertex )
	{
		final double d = vertex.getVertexDist();
		double radius = 0;
		if ( d >= minDisplayVertexDist )
		{
			final double spotdiameter = Math.min( vertex.getVertexDist() - 10.0, maxDisplayVertexSize );
			radius = spotdiameter / 2;
		}
		else if ( d >= minDisplaySimplifiedVertexDist )
		{
			radius = simplifiedVertexRadius + simplifiedVertexSelectTolerance;
		}
		final double dx = x - vertex.getX();
		final double dy = y - vertex.getY();
		return ( dx * dx + dy * dy < radius * radius );
	}

	/**
	 * Configures the graphics object prior to drawing vertices.
	 */
	protected void beforeDrawVertices()
	{
		g2.setStroke( style.getVertexStroke() );
	}

	/**
	 * Paints the specified vertex.
	 *
	 * @param vertex
	 *            the vertex to paint.
	 */
	protected void drawVertex( final ScreenVertex vertex )
	{
		final double d = vertex.getVertexDist();
		if ( d >= minDisplayVertexDist )
			drawVertexFull( vertex );
		else if ( d >= minDisplaySimplifiedVertexDist )
			drawVertexSimplified( vertex );
		else
			drawVertexSimplifiedIfHighlighted( vertex );
	}

	/**
	 * Configures the graphics object prior to drawing vertex ranges.
	 */
	protected void beforeDrawVertexRanges()
	{
		g2.setColor( style.getVertexRangeColor() );
	}

	/**
	 * Paints the specified vertex range.
	 *
	 * @param range
	 *            the vertex range to paint.
	 */
	protected void drawVertexRange( final ScreenVertexRange range )
	{
		final int x = ( int ) range.getMinX();
		final int y = ( int ) range.getMinY();
		final int w = ( int ) range.getMaxX() - x;
		final int h = ( int ) range.getMaxY() - y;
		g2.fillRect( x, y, w, h );
	}

	/**
	 * Configures the graphics object prior to drawing edges.
	 */
	public void beforeDrawEdges()
	{
		edgeStroke = style.getEdgeStroke();
		edgeHighlightStroke = style.getEdgeHighlightStroke();
		edgeGhostStroke = style.getEdgeGhostStroke();
		g2.setStroke( edgeStroke );
	}

	/**
	 * Paints the specified edge.
	 *
	 * @param edge
	 *            the edge to paint.
	 * @param vs
	 *            the edge source vertex.
	 * @param vt
	 *            the edge target vertex.
	 */
	public void drawEdge( final ScreenEdge edge, final ScreenVertex vs, final ScreenVertex vt )
	{
		Transition transition = edge.getTransition();
		double ratio = edge.getInterpolationCompletionRatio();
		if ( vt.getTransition() == APPEAR )
		{
			transition = APPEAR;
			ratio = vt.getInterpolationCompletionRatio();
		}
		if ( vs.getTransition() == APPEAR || vs.getTransition() == DISAPPEAR )
		{
			transition = vs.getTransition();
			ratio = vs.getInterpolationCompletionRatio();
		}
		final boolean highlighted = ( highlightedEdgeId >= 0 ) && ( edge.getTrackSchemeEdgeId() == highlightedEdgeId );
		final boolean selected = edge.isSelected();
		final boolean faded = style.isFadeFutureTimepoints() && edge.isFaded();
		final boolean ghost = vs.isGhost() && vt.isGhost();
		final int specifiedColor = edge.getColor();
		final Color drawColor = getColor( selected, ghost, faded, transition, ratio, specifiedColor,
				style.getEdgeColor(), style.getSelectedEdgeColor(),
				style.getGhostEdgeColor(), style.getGhostSelectedEdgeColor() );
		g2.setColor( drawColor );
		if ( highlighted )
			g2.setStroke( edgeHighlightStroke );
		else if ( ghost )
			g2.setStroke( edgeGhostStroke );
		drawEdgeLine( vs, vt );
		if ( highlighted || ghost )
			g2.setStroke( edgeStroke );
	}

	protected void drawEdgeLine( ScreenVertex vs, ScreenVertex vt )
	{
		g2.drawLine( ( int ) vs.getX(), ( int ) vs.getY(), ( int ) vt.getX(), ( int ) vt.getY() );
	}

	protected void drawVertexSimplified( final ScreenVertex vertex )
	{
		final boolean highlighted =
				( highlightedVertexId >= 0 ) && ( vertex.getTrackSchemeVertexId() == highlightedVertexId );
		final boolean focused = ( focusedVertexId >= 0 ) && ( vertex.getTrackSchemeVertexId() == focusedVertexId );
		drawVertexSimplified( vertex, highlighted, focused );
	}

	protected void drawVertexSimplifiedIfHighlighted( final ScreenVertex vertex )
	{
		final boolean highlighted =
				( highlightedVertexId >= 0 ) && ( vertex.getTrackSchemeVertexId() == highlightedVertexId );
		final boolean focused = ( focusedVertexId >= 0 ) && ( vertex.getTrackSchemeVertexId() == focusedVertexId );
		if ( highlighted || focused )
			drawVertexSimplified( vertex, false, focused );
	}

	private void drawVertexSimplified( ScreenVertex vertex, boolean highlighted, boolean focused )
	{
		final Transition transition = vertex.getTransition();
		final boolean disappear = ( transition == DISAPPEAR );
		final double ratio = vertex.getInterpolationCompletionRatio();

		final boolean selected = vertex.isSelected();
		final boolean ghost = vertex.isGhost();
		final int specifiedColor = vertex.getColor();

		double spotradius = simplifiedVertexRadius;
		if ( disappear )
			spotradius *= ( 1 + 3 * ratio );

		if ( highlighted || focused )
			spotradius *= 1.5;

		boolean faded = style.isFadeFutureTimepoints() && vertex.isFaded();
		final Color fillColor = getColor( selected, ghost, faded, transition, ratio, specifiedColor,
				disappear ? style.getSelectedSimplifiedVertexFillColor() : style.getSimplifiedVertexFillColor(),
				style.getSelectedSimplifiedVertexFillColor(),
				disappear ? style.getGhostSelectedSimplifiedVertexFillColor()
						: style.getGhostSimplifiedVertexFillColor(),
				style.getGhostSelectedSimplifiedVertexFillColor() );

		final double x = vertex.getX();
		final double y = vertex.getY();
		g2.setColor( fillColor );
		final int ox = ( int ) x - ( int ) spotradius;
		final int oy = ( int ) y - ( int ) spotradius;
		final int ow = 2 * ( int ) spotradius;

		if ( focused )
			g2.fillRect( ox, oy, ow, ow );
		else
			g2.fillOval( ox, oy, ow, ow );
	}

	protected void drawVertexFull( final ScreenVertex vertex )
	{
		final Transition transition = vertex.getTransition();
		final boolean disappear = ( transition == DISAPPEAR );
		final double ratio = vertex.getInterpolationCompletionRatio();

		final boolean highlighted =
				( highlightedVertexId >= 0 ) && ( vertex.getTrackSchemeVertexId() == highlightedVertexId );
		final boolean focused = ( focusedVertexId >= 0 ) && ( vertex.getTrackSchemeVertexId() == focusedVertexId );
		final boolean selected = vertex.isSelected();
		final boolean ghost = vertex.isGhost();
		final int specifiedColor = vertex.getColor();

		double spotdiameter = Math.min( vertex.getVertexDist() - 10.0, maxDisplayVertexSize );
		if ( highlighted )
			spotdiameter += 10.0;
		if ( disappear )
			spotdiameter *= ( 1 + ratio );
		final double spotradius = spotdiameter / 2;

		boolean faded = style.isFadeFutureTimepoints() && vertex.isFaded();
		final Color fillColor = getColor( selected, ghost, faded, transition, ratio, specifiedColor,
				style.getVertexFillColor(), style.getSelectedVertexFillColor(),
				style.getGhostVertexFillColor(), style.getGhostSelectedVertexFillColor() );
		final Color drawColor = getColor( selected, ghost, faded, transition, ratio, 0,
				style.getVertexDrawColor(), style.getSelectedVertexDrawColor(),
				style.getGhostVertexDrawColor(), style.getGhostSelectedVertexDrawColor() );

		final double x = vertex.getX();
		final double y = vertex.getY();
		final int ox = ( int ) x - ( int ) spotradius;
		final int oy = ( int ) y - ( int ) spotradius;
		final int sd = 2 * ( int ) spotradius;
		g2.setColor( fillColor );
		g2.fillOval( ox, oy, sd, sd );

		g2.setColor( drawColor );
		if ( highlighted )
			g2.setStroke( style.getVertexHighlightStroke() );
		else if ( focused )
			// An animation might be better for the focus, but for now this is it.
			g2.setStroke( style.getFocusStroke() );
		else if ( ghost )
			g2.setStroke( style.getVertexGhostStroke() );
		g2.drawOval( ox, oy, sd, sd );
		if ( highlighted || focused || ghost )
			g2.setStroke( style.getVertexStroke() );

		final int maxLabelLength = ( int ) ( spotdiameter / avgLabelLetterWidth );
		if ( maxLabelLength > 2 && !disappear )
		{
			String label = vertex.getLabel();
			if ( label.length() > maxLabelLength )
				label = label.substring( 0, maxLabelLength - 2 ) + "...";

			if ( !label.isEmpty() )
			{
				// Text color depend on the bg color for color schemes.
				if ( specifiedColor != 0 )
					g2.setColor( textColorForBackground( fillColor ) );

				drawTextCentered( x, y, label, style.getFont() );
			}
		}
	}

	private void drawTextCentered( double x, double y, String label, Font font )
	{
		if ( !g2.getClipBounds( tmpRectangle ).contains( x, y ) )
			return;

		final FontRenderContext frc = g2.getFontRenderContext();
		final TextLayout layout = new TextLayout( label, font, frc );
		final Rectangle2D bounds = layout.getBounds();
		final float tx = ( float ) ( x - bounds.getCenterX() );
		final float ty = ( float ) ( y - bounds.getCenterY() );
		layout.draw( g2, tx, ty );
	}

	protected Color getColor( final boolean isSelected, final boolean isGhost, final boolean isFaded, final Transition transition,
			final double completionRatio, final int specifiedColor, final Color normalColor, final Color selectedColor,
			final Color ghostNormalColor, final Color ghostSelectedColor )
	{
		Color color = getColor( isSelected, isGhost, transition, completionRatio, specifiedColor, normalColor, selectedColor,
				ghostNormalColor, ghostSelectedColor );
		if ( !isFaded )
			return color;
		return ColorUtils.mixColors( color, style.getBackgroundColor(), fadeRatio );
	}

	protected Color getColor(
			final boolean isSelected,
			final boolean isGhost,
			final Transition transition,
			final double completionRatio,
			final int specifiedColor,
			final Color normalColor,
			final Color selectedColor,
			final Color ghostNormalColor,
			final Color ghostSelectedColor )
	{
		if ( transition == NONE )
		{
			if ( isGhost )
				return isSelected ? ghostSelectedColor : ghostNormalColor;
			else if ( isSelected )
				return selectedColor;
			else if ( specifiedColor == 0 )
				return normalColor;
			else
				return new Color( specifiedColor, true );
		}
		else
		{
			final double ratio = ( transition == APPEAR || transition == SELECTING )
					? 1 - completionRatio
					: completionRatio;
			final boolean fade = ( transition == APPEAR || transition == DISAPPEAR );
			int rgb = specifiedColor == 0 ? normalColor.getRGB() : specifiedColor;
			if ( isSelected || !fade )
				rgb = ColorUtils.mixColors( selectedColor.getRGB(), rgb, ( float ) ratio );
			if ( fade )
				rgb = ColorUtils.scaleAlpha( rgb, (float) (1 - ratio) );
			final Color color = new Color( rgb, true );
			return isGhost
					? TrackSchemeStyle.mixGhostColor( color, style.getBackgroundColor() )
					: color;
		}
	}

	/**
	 * Returns the black color or white color depending on the specified
	 * background color, to ensure proper readability of the text on said
	 * background.
	 *
	 * @param backgroundColor
	 *            the background color.
	 * @return the black or white color.
	 */
	protected static Color textColorForBackground( final Color backgroundColor )
	{
		if ( ( backgroundColor.getRed() * 0.299
				+ backgroundColor.getGreen() * 0.587
				+ backgroundColor.getBlue() * 0.114 ) > 150 )
			return Color.BLACK;
		else
			return Color.WHITE;
	}
}
