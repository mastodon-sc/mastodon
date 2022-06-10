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
package org.mastodon.views.trackscheme.display;

import static org.mastodon.views.trackscheme.ScreenVertex.Transition.DISAPPEAR;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;

import org.mastodon.views.trackscheme.ScreenEntities;
import org.mastodon.views.trackscheme.ScreenTransform;
import org.mastodon.views.trackscheme.ScreenVertex;
import org.mastodon.views.trackscheme.ScreenVertex.Transition;
import org.mastodon.views.trackscheme.display.style.TrackSchemeStyle;

public class PaintHierarchicalGraph extends PaintBranchGraph
{

	private final float STROKE_WIDTH = 2.5f;

	private ScreenTransform transform = new ScreenTransform();

	private double averageLetterWidth = 5;

	private double spotRadius;

	private Color labelColor;

	public PaintHierarchicalGraph() {
		super();
		this.edgeStroke = new BasicStroke( STROKE_WIDTH );
	}

	@Override
	public void paintGraph( Graphics2D g2, ScreenEntities entities, int highlightedVertexId, int highlightedEdgeId, int focusedVertexId, TrackSchemeStyle style )
	{
		entities.getScreenTransform( this.transform );
		super.paintGraph( g2, entities, highlightedVertexId, highlightedEdgeId, focusedVertexId, style );
	}

	@Override
	protected void beforeDrawVertices()
	{
		g2.setStroke( edgeStroke );
		calculateAverageLetterWidth();
		spotRadius = Math.min(transform.getScaleX() * 0.3, transform.getScaleY() * 0.4);
		labelColor = textColorForBackground( style.getBackgroundColor() );
	}

	private void calculateAverageLetterWidth()
	{
		final FontRenderContext frc = g2.getFontRenderContext();
		String text = "0123456789abcdefghijklmnopqrstuvwxyz";
		final TextLayout layout = new TextLayout( text, style.getFont(), frc );
		averageLetterWidth = layout.getBounds().getWidth() / text.length();
	}

	@Override
	protected void drawVertex( ScreenVertex vertex )
	{
		if(spotRadius > 3)
			drawVertexFull( vertex );
		else
			drawVertexSimplified( vertex );
	}

	@Override
	protected void drawVertexFull( ScreenVertex vertex )
	{
		final Transition transition = vertex.getTransition();
		final boolean disappear = ( transition == DISAPPEAR );
		final double ratio = vertex.getInterpolationCompletionRatio();

		final boolean highlighted = ( highlightedVertexId >= 0 ) && ( vertex.getTrackSchemeVertexId() == highlightedVertexId );
		final boolean focused = ( focusedVertexId >= 0 ) && ( vertex.getTrackSchemeVertexId() == focusedVertexId );
		final boolean selected = vertex.isSelected();
		final boolean ghost = vertex.isGhost();
		final int specifiedColor = vertex.getColor();

		final Color fillColor = getColor( selected, ghost, transition, ratio, specifiedColor,
				style.getVertexFillColor(), style.getSelectedVertexFillColor(),
				style.getGhostVertexFillColor(), style.getGhostSelectedVertexFillColor() );
		final Color drawColor = getColor( selected, ghost, transition, ratio, 0,
				style.getVertexDrawColor(), style.getSelectedVertexDrawColor(),
				style.getGhostVertexDrawColor(), style.getGhostSelectedVertexDrawColor() );

		final double x = vertex.getX();
		final double y = vertex.getY();
		final int ox = ( int ) x - ( int ) spotRadius;
		final int oy = ( int ) y - ( int ) spotRadius;
		final int sd = 2 * ( int ) spotRadius;
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
			beforeDrawVertices();

		final int maxLabelLength = (int) (transform.getScaleX() * 0.8 / averageLetterWidth );
		if ( maxLabelLength > 2 && !disappear )
		{
			String label = vertex.getLabel();
			if ( label.length() > maxLabelLength )
				label = label.substring( 0, maxLabelLength - 2 ) + "...";

			if ( !label.isEmpty() )
			{
				g2.setColor( labelColor );
				final FontRenderContext frc = g2.getFontRenderContext();
				final TextLayout layout = new TextLayout( label, style.getFont(), frc );
				final Rectangle2D bounds = layout.getBounds();
				final float tx = ( float ) ( x - bounds.getCenterX() );
				final float ty = ( float ) ( y + bounds.getHeight() + 1.2 * spotRadius + STROKE_WIDTH );
				layout.draw( g2, tx, ty );
			}
		}
	}
}
