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

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import org.mastodon.model.HighlightModel;
import org.mastodon.views.grapher.datagraph.DataEdge;
import org.mastodon.views.grapher.datagraph.DataGraph;
import org.mastodon.views.grapher.datagraph.DataVertex;
import org.mastodon.views.grapher.datagraph.ScreenTransform;
import org.mastodon.views.grapher.display.OffsetAxes.OffsetAxesListener;

import bdv.viewer.TransformListener;

public class MouseHighlightHandler
		implements MouseMotionListener, MouseListener, TransformListener< ScreenTransform >, OffsetAxesListener
{
	private final DataDisplayOverlay graphOverlay;

	private final HighlightModel< DataVertex, DataEdge > highlight;

	private final DataGraph< ?, ? > graph;

	private boolean mouseInside;

	private int x, y;

	/**
	 * Current width of vertical header.
	 */
	private int headerWidth;

	/**
	 * The height of the panel items are painted in.
	 */
	private int screenHeight;

	public MouseHighlightHandler(
			final DataDisplayOverlay graphOverlay,
			final HighlightModel< DataVertex, DataEdge > highlight,
			final DataGraph< ?, ? > graph )
	{
		this.graphOverlay = graphOverlay;
		this.highlight = highlight;
		this.graph = graph;
	}

	@Override
	public void mouseMoved( final MouseEvent e )
	{
		x = e.getX();
		y = e.getY();
		highlight();
	}

	@Override
	public void mouseDragged( final MouseEvent e )
	{
		x = e.getX();
		y = e.getY();
		highlight();
	}

	@Override
	public void transformChanged( final ScreenTransform t )
	{
		screenHeight = t.getScreenHeight();
		if ( mouseInside )
			highlight();
	}

	@Override
	public void updateAxesSize( final int width, final int height )
	{
		headerWidth = width;
	}

	private void highlight()
	{
		if ( x < headerWidth || y > screenHeight )
			highlight.clearHighlight();
		else
		{
			final DataVertex vertex = graph.vertexRef();
			final DataEdge edge = graph.edgeRef();

			// See if we can find a vertex.
			if ( graphOverlay.getVertexAt( x, y, vertex ) != null )
				highlight.highlightVertex( vertex );
			// See if we can find an edge.
			else if ( graphOverlay.getEdgeAt( x, y, DataDisplayNavigationBehaviours.EDGE_SELECT_DISTANCE_TOLERANCE,
					edge ) != null )
				highlight.highlightEdge( edge );
			else
				highlight.clearHighlight();

			graph.releaseRef( vertex );
			graph.releaseRef( edge );
		}
	}

	@Override
	public void mouseClicked( final MouseEvent e )
	{}

	@Override
	public void mousePressed( final MouseEvent e )
	{}

	@Override
	public void mouseReleased( final MouseEvent e )
	{}

	@Override
	public void mouseEntered( final MouseEvent e )
	{
		mouseInside = true;
	}

	@Override
	public void mouseExited( final MouseEvent e )
	{
		highlight.clearHighlight();
		mouseInside = false;
	}
}
