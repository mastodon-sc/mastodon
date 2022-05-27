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

import static org.mastodon.views.trackscheme.ScreenVertex.Transition.APPEAR;
import static org.mastodon.views.trackscheme.ScreenVertex.Transition.DISAPPEAR;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Stroke;

import org.mastodon.views.trackscheme.ScreenEdge;
import org.mastodon.views.trackscheme.ScreenVertex;
import org.mastodon.views.trackscheme.ScreenVertex.Transition;

public class PaintBranchGraphRound extends PaintBranchGraph
{

	private static final int ARC_RADIUS = 15;

	private final Stroke edgeStroke = new BasicStroke( 1f );

	@Override
	public void beforeDrawEdges()
	{
		g2.setStroke( edgeStroke );
	}

	@Override
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
		final boolean ghost = vs.isGhost() && vt.isGhost();
		final int specifiedColor = edge.getColor();
		final Color drawColor = getColor( selected, ghost, transition, ratio, specifiedColor,
				style.getEdgeColor(), style.getSelectedEdgeColor(),
				style.getGhostEdgeColor(), style.getGhostSelectedEdgeColor() );
		g2.setColor( drawColor );
		if ( highlighted )
			g2.setStroke( style.getEdgeHighlightStroke() );
		else if ( ghost )
			g2.setStroke( style.getEdgeGhostStroke() );

		final int sx = ( int ) vs.getX();
		final int sy = ( int ) vs.getY();
		final int tx = ( int ) vt.getX();
		final int ty = ( int ) vt.getY();

		final int dx = tx - sx;
		final int dy = ty - sy;

		if ( dx == 0 )
		{
			g2.drawLine( sx, sy, tx, ty );
		}
		else if ( dx > 0 )
		{
			if ( dx > ARC_RADIUS )
				g2.drawLine( sx, sy, tx - ARC_RADIUS, sy );
			if ( dy > ARC_RADIUS )
				g2.drawLine( tx, sy + ARC_RADIUS, tx, ty );

			g2.drawArc(
					tx - 2 * Math.min( dx, ARC_RADIUS ),
					sy,
					2 * Math.min( ARC_RADIUS, dx ),
					2 * Math.min( ARC_RADIUS, dy ),
					90, -90 );
		}
		else
		{
			if ( -dx > ARC_RADIUS )
				g2.drawLine( tx + ARC_RADIUS, sy, sx, sy );
			if ( dy > ARC_RADIUS )
				g2.drawLine( tx, sy + ARC_RADIUS, tx, ty );

			g2.drawArc(
					tx,
					sy,
					2 * Math.min( ARC_RADIUS, -dx ),
					2 * Math.min( ARC_RADIUS, dy ),
					90, 90 );
		}

		if ( highlighted || ghost )
			g2.setStroke( edgeStroke );
	}
}
