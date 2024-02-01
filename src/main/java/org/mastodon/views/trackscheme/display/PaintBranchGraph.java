/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2024 Tobias Pietzsch, Jean-Yves Tinevez
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

import org.mastodon.util.GeometryUtil;
import org.mastodon.views.trackscheme.ScreenEdge;
import org.mastodon.views.trackscheme.ScreenVertex;

import java.awt.Color;

public class PaintBranchGraph extends PaintGraph
{

	@Override
	public void beforeDrawEdges()
	{
		edgeStroke = style.getBranchGraphEdgeStroke();
		edgeHighlightStroke = style.getBranchGraphEdgeHighlightStroke();
		edgeGhostStroke = style.getEdgeGhostStroke();
		g2.setStroke( edgeStroke );
	}

	@Override
	protected void drawEdgeLine( ScreenVertex vs, ScreenVertex vt )
	{
		final int sx = ( int ) vs.getX();
		final int sy = ( int ) vs.getY();
		final int tx = ( int ) vt.getX();
		final int ty = ( int ) vt.getYStart();
		g2.drawLine( sx, sy, tx, sy );
		g2.drawLine( tx, sy, tx, ty );
	}

	@Override
	public double distanceToPaintedEdge( final double x0, final double y0, final ScreenEdge edge,
			final ScreenVertex source, final ScreenVertex target )
	{
		final double xs = source.getX();
		final double xt = target.getX();
		final double ys = source.getY();
		final double yt = target.getYStart();
		if ( xs == xt )
			return GeometryUtil.segmentDist( x0, y0, xs, ys, xt, yt );

		final double d1 = GeometryUtil.segmentDist( x0, y0, xs, ys, xt, ys );
		final double d2 = GeometryUtil.segmentDist( x0, y0, xt, ys, xt, yt );
		return Math.min( d1, d2 );
	}

	@Override
	public boolean isInsidePaintedVertex( double x, double y, ScreenVertex vertex )
	{
		if ( y >= vertex.getYStart() && y <= vertex.getY() && Math.abs( x - vertex.getX() ) <= 2 )
			return true;
		return super.isInsidePaintedVertex( x, y, vertex );
	}

	@Override
	protected void drawVertex( ScreenVertex vertex )
	{
		boolean resetStroke = setStroke( vertex );
		g2.drawLine( ( int ) vertex.getX(), ( int ) vertex.getYStart(), ( int ) vertex.getX(), ( int ) vertex.getY() );
		resetStroke( resetStroke );

		super.drawVertex( vertex );
	}

	private boolean setStroke( ScreenVertex vertex )
	{
		final ScreenVertex.Transition transition = vertex.getTransition();
		final double ratio = vertex.getInterpolationCompletionRatio();

		final boolean highlighted =
				( highlightedVertexId >= 0 ) && ( vertex.getTrackSchemeVertexId() == highlightedVertexId );
		final boolean focused = ( focusedVertexId >= 0 ) && ( vertex.getTrackSchemeVertexId() == focusedVertexId );
		final boolean selected = vertex.isSelected();
		final boolean ghost = vertex.isGhost();
		final int specifiedColor = vertex.getColor();

		final Color drawColor = getColor( selected, ghost, transition, ratio, specifiedColor,
				style.getVertexDrawColor(), style.getSelectedVertexDrawColor(),
				style.getGhostVertexDrawColor(), style.getGhostSelectedVertexDrawColor() );

		g2.setColor( drawColor );
		if ( highlighted )
			g2.setStroke( style.getVertexHighlightStroke() );
		else if ( focused )
			// An animation might be better for the focus, but for now this is it.
			g2.setStroke( style.getFocusStroke() );
		else if ( ghost )
			g2.setStroke( style.getVertexGhostStroke() );
		boolean resetStroke = highlighted || focused || ghost;
		return resetStroke;
	}

	private void resetStroke( boolean resetStroke )
	{
		if ( resetStroke )
			g2.setStroke( style.getVertexStroke() );
	}

}
