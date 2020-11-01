package org.mastodon.views.bdv.overlay;

import bdv.viewer.TransformListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import org.mastodon.model.HighlightModel;

import net.imglib2.realtransform.AffineTransform3D;

public class BdvHighlightHandler< V extends OverlayVertex< V, E >, E extends OverlayEdge< E, V > > implements MouseMotionListener, MouseListener, TransformListener< AffineTransform3D >
{
	private final OverlayGraph< V, E > overlayGraph;

	private final OverlayGraphRenderer< V, E > renderer;

	private final HighlightModel< V, E > highlight;

	private boolean mouseInside;

	private int x, y;

	public BdvHighlightHandler(
			final OverlayGraph< V, E > overlayGraph,
			final OverlayGraphRenderer< V, E > renderer,
			final HighlightModel< V, E > highlight )
	{
		this.highlight = highlight;
		this.renderer = renderer;
		this.overlayGraph = overlayGraph;
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
	public void transformChanged( final AffineTransform3D t )
	{
		if ( mouseInside )
			highlight();
	}

	private void highlight()
	{
		final V vertex = overlayGraph.vertexRef();
		final E edge = overlayGraph.edgeRef();
		overlayGraph.getLock().readLock().lock();
		try
		{
			// See if we can find an edge.
			if ( renderer.getEdgeAt( x, y, BdvSelectionBehaviours.EDGE_SELECT_DISTANCE_TOLERANCE, edge ) != null )
				highlight.highlightEdge( edge );
			// See if we can find a vertex.
			else if ( renderer.getVertexAt( x, y, BdvSelectionBehaviours.POINT_SELECT_DISTANCE_TOLERANCE, vertex ) != null )
				highlight.highlightVertex( vertex );
			else
				highlight.clearHighlight();
		}
		finally
		{
			overlayGraph.getLock().readLock().unlock();
			overlayGraph.releaseRef( vertex );
			overlayGraph.releaseRef( edge );
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
