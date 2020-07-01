package org.mastodon.views.trackscheme.display;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import org.mastodon.model.HighlightModel;
import org.mastodon.views.trackscheme.ScreenTransform;
import org.mastodon.views.trackscheme.TrackSchemeEdge;
import org.mastodon.views.trackscheme.TrackSchemeGraph;
import org.mastodon.views.trackscheme.TrackSchemeVertex;
import org.mastodon.views.trackscheme.display.OffsetHeaders.OffsetHeadersListener;

import net.imglib2.ui.TransformListener;

public class MouseHighlightHandler implements MouseMotionListener, MouseListener, TransformListener< ScreenTransform >, OffsetHeadersListener
{
	private final TrackSchemeOverlay graphOverlay;

	private final HighlightModel< TrackSchemeVertex, TrackSchemeEdge > highlight;

	private final TrackSchemeGraph< ?, ? > graph;

	private boolean mouseInside;

	private int x, y;

	/**
	 * current width of vertical header.
	 */
	private int headerWidth;

	/**
	 * current height of horizontal header.
	 */
	private int headerHeight;

	public MouseHighlightHandler(
			final TrackSchemeOverlay graphOverlay,
			final HighlightModel< TrackSchemeVertex, TrackSchemeEdge > highlight,
			final TrackSchemeGraph< ?, ? > graph )
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
		if ( mouseInside )
			highlight();
	}

	@Override
	public void updateHeaderSize( final int width, final int height )
	{
		headerWidth = width;
		headerHeight = height;
	}

	private void highlight()
	{
		if ( x < headerWidth || y < headerHeight )
			highlight.clearHighlight();
		else
		{
			final TrackSchemeVertex vertex = graph.vertexRef();
			final TrackSchemeEdge edge = graph.edgeRef();

			// See if we can find a vertex.
			if ( graphOverlay.getVertexAt( x, y, vertex ) != null )
				highlight.highlightVertex( vertex );
			// See if we can find an edge.
			else if ( graphOverlay.getEdgeAt( x, y, TrackSchemeNavigationBehaviours.EDGE_SELECT_DISTANCE_TOLERANCE, edge ) != null )
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
