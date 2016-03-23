package net.trackmate.revised.bdv.overlay;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.ui.TransformListener;

public class MouseHighlightHandler< V extends OverlayVertex< V, E >, E extends OverlayEdge< E, V > > implements MouseMotionListener, MouseListener, TransformListener< AffineTransform3D >
{
	private final OverlayGraph< V, E > overlayGraph;

	private final OverlayGraphRenderer< V, E > renderer;

	private final OverlayHighlight< V, E > highlight;

	private boolean mouseInside;

	private int x, y;

	public MouseHighlightHandler(
			final OverlayGraph< V, E > overlayGraph,
			final OverlayGraphRenderer< V, E > renderer,
			final OverlayHighlight< V, E > highlight )
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

		// See if we can find a vertex.
		if ( renderer.getVertexAt( x, y, SelectionBehaviours.POINT_SELECT_DISTANCE_TOLERANCE, vertex ) != null )
			highlight.highlightVertex( vertex );
		// See if we can find an edge.
		else if ( renderer.getEdgeAt( x, y, SelectionBehaviours.EDGE_SELECT_DISTANCE_TOLERANCE, edge ) != null )
			highlight.highlightEdge( edge );
		else
			highlight.clearHighlight();

		overlayGraph.releaseRef( vertex );
		overlayGraph.releaseRef( edge );
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
