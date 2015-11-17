package net.trackmate.revised.bdv.overlay;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class MouseSelectionHandler< V extends OverlayVertex< V, E >, E extends OverlayEdge< E, V > > implements MouseListener
{
	private static final double SELECT_DISTANCE_TOLERANCE = 5.0;

	private final OverlayGraph< V, E > overlayGraph;

	private final OverlayGraphRenderer< V, E > renderer;

	private final OverlaySelection< V, E > selection;

	public MouseSelectionHandler(
			final OverlayGraph< V, E > overlayGraph,
			final OverlayGraphRenderer< V, E > renderer,
			final OverlaySelection< V, E > selection )
	{
		this.overlayGraph = overlayGraph;
		this.renderer = renderer;
		this.selection = selection;
	}

	@Override
	public void mouseClicked( final MouseEvent e )
	{
		final V ref = overlayGraph.vertexRef();
		final V vertex = renderer.getVertexAt( e.getX(), e.getY(), ref );
		if ( vertex == null )
		{
			// Let's see if we can select an edge.
			final E edgeRef = overlayGraph.edgeRef();
			final E edge = renderer.getEdgeAt( e.getX(), e.getY(), SELECT_DISTANCE_TOLERANCE, edgeRef );
			if ( edge == null )
			{
				if ( !e.isShiftDown() )
					selection.clearSelection();
				overlayGraph.releaseRef( ref );
				overlayGraph.releaseRef( edgeRef );
				return;
			}

			if ( e.isShiftDown() )
			{

				selection.toggleSelected( edge );
			}
			else
			{
				selection.clearSelection();
				selection.setSelected( edge, true );
			}
			overlayGraph.releaseRef( edgeRef );

		}
		else
		{
			if ( e.isShiftDown() )
			{

				selection.toggleSelected( vertex );
			}
			else
			{
				selection.clearSelection();
				selection.setSelected( vertex, true );
			}

		}
		overlayGraph.releaseRef( ref );
	}

	@Override
	public void mouseEntered( final MouseEvent e )
	{}

	@Override
	public void mouseExited( final MouseEvent e )
	{}

	@Override
	public void mousePressed( final MouseEvent e )
	{}

	@Override
	public void mouseReleased( final MouseEvent e )
	{}
}
