/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2025 Tobias Pietzsch, Jean-Yves Tinevez
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

import static org.mastodon.views.trackscheme.ScreenVertex.Transition.APPEAR;
import static org.mastodon.views.trackscheme.ScreenVertex.Transition.DISAPPEAR;
import static org.mastodon.views.trackscheme.ScreenVertex.Transition.NONE;
import static org.mastodon.views.trackscheme.ScreenVertex.Transition.SELECTING;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;

import org.mastodon.collection.RefList;
import org.mastodon.views.grapher.datagraph.ScreenEdge;
import org.mastodon.views.grapher.datagraph.ScreenEntities;
import org.mastodon.views.grapher.datagraph.ScreenVertex;
import org.mastodon.views.grapher.display.style.DataDisplayStyle;
import org.mastodon.views.trackscheme.ScreenVertex.Transition;

import net.imglib2.type.numeric.ARGBType;

public class PaintGraph
{

	/*
	 * ENUM
	 */

	public enum VertexDrawShape
	{
		CIRCLE( "Circle", new Ellipse2D.Double() ), SQUARE( "Square", new Rectangle2D.Double() );

		private final String name;

		private final RectangularShape shape;

		private VertexDrawShape( final String name, final RectangularShape shape )
		{
			this.name = name;
			this.shape = shape;
		}

		@Override
		public String toString()
		{
			return name;
		}

		private Shape shape( final double ox, final double oy, final double diameter )
		{
			shape.setFrame( ox, oy, diameter, diameter );
			return shape;
		}
	}

	/*
	 * CONSTANTS
	 */

	private static final double simplifiedVertexRadius = 2.5;

	private static final double simplifiedVertexSelectTolerance = 3.5;

	private static final double minDisplayVertexDist = 17.0;

	private static final double maxDisplayVertexSize = 20.0;

	private static final double minDisplaySimplifiedVertexDist = 0.0;

	private static final double avgLabelLetterWidth = 5.0;

	/*
	 * FIELDS
	 */

	protected Graphics2D g2;

	protected int highlightedVertexId;

	protected int highlightedEdgeId;

	protected int focusedVertexId;

	protected DataDisplayStyle style;

	public void paintGraph(
			final Graphics2D g2,
			final ScreenEntities entities,
			final int highlightedVertexId,
			final int highlightedEdgeId,
			final int focusedVertexId,
			final DataDisplayStyle style )
	{
		this.g2 = g2;
		this.highlightedVertexId = highlightedVertexId;
		this.highlightedEdgeId = highlightedEdgeId;
		this.focusedVertexId = focusedVertexId;
		this.style = style;

		final RefList< ScreenEdge > edges = entities.getEdges();
		final RefList< ScreenVertex > vertices = entities.getVertices();

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
			drawVertex( vertex );

		vertices.releaseRef( vs );
		vertices.releaseRef( vt );
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
		if ( style.isAutoVertexSize() )
		{
			final double d = vertex.getVertexDist();
			if ( d >= minDisplayVertexDist )
				drawVertexFullAutoSize( vertex );
			else if ( d >= minDisplaySimplifiedVertexDist )
				drawVertexSimplified( vertex );
			else
				drawVertexSimplifiedIfHighlighted( vertex );
		}
		else
		{
			final double spotdiameter = style.getVertexFixedSize();
			drawVertexFull( vertex, spotdiameter );
		}
	}

	/**
	 * Configures the graphics object prior to drawing edges.
	 */
	public void beforeDrawEdges()
	{
		g2.setStroke( style.getEdgeStroke() );
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
		final boolean highlighted = ( highlightedEdgeId >= 0 ) && ( edge.getDataEdgeId() == highlightedEdgeId );
		final boolean selected = edge.isSelected();
		final int specifiedColor = edge.getColor();
		final Color drawColor = getColor( selected, transition, ratio, specifiedColor,
				style.getEdgeColor(), style.getSelectedEdgeColor() );
		g2.setColor( drawColor );
		if ( highlighted )
			g2.setStroke( style.getEdgeHighlightStroke() );
		g2.drawLine( ( int ) vs.getX(), ( int ) vs.getY(), ( int ) vt.getX(), ( int ) vt.getY() );
		if ( highlighted )
			g2.setStroke( style.getEdgeStroke() );
	}

	protected void drawVertexSimplified( final ScreenVertex vertex )
	{
		final Transition transition = vertex.getTransition();
		final boolean disappear = ( transition == DISAPPEAR );
		final double ratio = vertex.getInterpolationCompletionRatio();

		final boolean highlighted = ( highlightedVertexId >= 0 ) && ( vertex.getDataVertexId() == highlightedVertexId );
		final boolean focused = ( focusedVertexId >= 0 ) && ( vertex.getDataVertexId() == focusedVertexId );
		final boolean selected = vertex.isSelected();
		final int specifiedColor = vertex.getColor();

		double spotradius = simplifiedVertexRadius;
		if ( disappear )
			spotradius *= ( 1 + 3 * ratio );

		if ( highlighted || focused )
			spotradius *= 1.5;

		final Color fillColor = getColor( selected, transition, ratio, specifiedColor,
				disappear ? style.getSelectedSimplifiedVertexFillColor() : style.getSimplifiedVertexFillColor(),
				style.getSelectedSimplifiedVertexFillColor() );

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

	protected void drawVertexSimplifiedIfHighlighted( final ScreenVertex vertex )
	{
		final boolean highlighted = ( highlightedVertexId >= 0 ) && ( vertex.getDataVertexId() == highlightedVertexId );
		final boolean focused = ( focusedVertexId >= 0 ) && ( vertex.getDataVertexId() == focusedVertexId );
		if ( highlighted || focused )
		{
			final Transition transition = vertex.getTransition();
			final boolean disappear = ( transition == DISAPPEAR );
			final double ratio = vertex.getInterpolationCompletionRatio();

			final boolean selected = vertex.isSelected();
			final int specifiedColor = vertex.getColor();

			double spotradius = simplifiedVertexRadius;
			if ( disappear )
				spotradius *= ( 1 + 3 * ratio );

			final Color fillColor = getColor( selected, transition, ratio, specifiedColor,
					disappear ? style.getSelectedSimplifiedVertexFillColor() : style.getSimplifiedVertexFillColor(),
					style.getSelectedSimplifiedVertexFillColor() );

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
	}

	protected void drawVertexFullAutoSize( final ScreenVertex vertex )
	{
		final Transition transition = vertex.getTransition();
		final boolean disappear = ( transition == DISAPPEAR );
		final double ratio = vertex.getInterpolationCompletionRatio();
		final boolean highlighted = ( highlightedVertexId >= 0 ) && ( vertex.getDataVertexId() == highlightedVertexId );

		final double spotdiameter = getVertexDiameter( vertex.getVertexDist(), highlighted, disappear, ratio );
		drawVertexFull( vertex, spotdiameter );
	}

	protected void drawVertexFull( final ScreenVertex vertex, final double diameter )
	{
		final Transition transition = vertex.getTransition();
		final boolean disappear = ( transition == DISAPPEAR );
		final double ratio = vertex.getInterpolationCompletionRatio();

		final boolean highlighted = ( highlightedVertexId >= 0 ) && ( vertex.getDataVertexId() == highlightedVertexId );
		final boolean focused = ( focusedVertexId >= 0 ) && ( vertex.getDataVertexId() == focusedVertexId );
		final boolean selected = vertex.isSelected();
		final int specifiedColor = vertex.getColor();

		double spotdiameter = diameter;
		if ( highlighted )
			spotdiameter += 10.0;
		if ( disappear )
			spotdiameter *= ( 1 + ratio );
		final double spotradius = spotdiameter / 2;

		final Color fillColor = getColor( selected, transition, ratio, specifiedColor,
				style.getVertexFillColor(), style.getSelectedVertexFillColor() );
		final Color drawColor = getColor( selected, transition, ratio, 0,
				style.getVertexDrawColor(), style.getSelectedVertexDrawColor() );

		final double x = vertex.getX();
		final double y = vertex.getY();
		final double ox = x - spotradius;
		final double oy = y - spotradius;
		final double sd = 2 * spotradius;
		final Shape shape = style.getVertexDrawShape().shape( ox, oy, sd );
		g2.setColor( fillColor );
		g2.fill( shape );

		g2.setColor( drawColor );
		if ( highlighted )
			g2.setStroke( style.getVertexHighlightStroke() );
		else if ( focused )
			g2.setStroke( style.getFocusStroke() );
		g2.draw( shape );
		if ( highlighted || focused )
			g2.setStroke( style.getVertexStroke() );

		if ( style.isDrawVertexName() )
		{

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

					final FontRenderContext frc = g2.getFontRenderContext();
					final TextLayout layout = new TextLayout( label, style.getFont(), frc );
					final Rectangle2D bounds = layout.getBounds();
					final float tx = ( float ) ( x - bounds.getCenterX() );
					final float ty = ( float ) ( y - bounds.getCenterY() );
					layout.draw( g2, tx, ty );
				}
			}
		}
	}

	protected double getVertexDiameter( final double vertexDist, final boolean highlighted, final boolean disappear,
			final double ratio )
	{
		double spotdiameter = Math.min( vertexDist - 10.0, maxDisplayVertexSize );
		if ( highlighted )
			spotdiameter += 10.0;
		if ( disappear )
			spotdiameter *= ( 1 + ratio );
		return spotdiameter;
	}

	protected Color getColor(
			final boolean isSelected,
			final Transition transition,
			final double completionRatio,
			final int specifiedColor,
			final Color normalColor,
			final Color selectedColor )
	{
		if ( transition == NONE )
		{
			if ( isSelected )
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
			int r, g, b, a;
			if ( specifiedColor == 0 )
			{
				r = normalColor.getRed();
				g = normalColor.getGreen();
				b = normalColor.getBlue();
				a = normalColor.getAlpha();
			}
			else
			{
				r = ARGBType.red( specifiedColor );
				g = ARGBType.green( specifiedColor );
				b = ARGBType.blue( specifiedColor );
				a = ARGBType.alpha( specifiedColor );
			}
			if ( isSelected || !fade )
			{
				r = ( int ) ( ratio * r + ( 1 - ratio ) * selectedColor.getRed() );
				g = ( int ) ( ratio * g + ( 1 - ratio ) * selectedColor.getGreen() );
				b = ( int ) ( ratio * b + ( 1 - ratio ) * selectedColor.getBlue() );
				a = ( int ) ( ratio * a + ( 1 - ratio ) * selectedColor.getAlpha() );
			}
			if ( fade )
				a = ( int ) ( a * ( 1 - ratio ) );
			final Color color = new Color( r, g, b, a );
			return color;
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
	private static Color textColorForBackground( final Color backgroundColor )
	{
		if ( ( backgroundColor.getRed() * 0.299
				+ backgroundColor.getGreen() * 0.587
				+ backgroundColor.getBlue() * 0.114 ) > 150 )
			return Color.BLACK;
		else
			return Color.WHITE;
	}
}
