package net.trackmate.revised.bdv.overlay;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class MouseSelectionHandler< V extends OverlayVertex< V, E >, E extends OverlayEdge< E, V > > implements MouseListener
{
	private final OverlayGraphRenderer< V, E > renderer;

	private final OverlayGraph< V, E > overlayGraph;

	private final OverlaySelection< V, E > selection;

	public MouseSelectionHandler( final OverlayGraph< V, E > overlayGraph, final OverlayGraphRenderer< V, E > renderer, final OverlaySelection< V, E > selection )
	{
		this.renderer = renderer;
		this.overlayGraph = overlayGraph;
		this.selection = selection;
	}

	@Override
	public void mouseClicked( final MouseEvent e )
	{
		final V ref = overlayGraph.vertexRef();
		final V v = renderer.getVertexAt( e.getX(), e.getY(), ref );
		if ( v == null )
		{
			if ( !e.isShiftDown() )
				selection.clearSelection();
			return;
		}

		if ( e.isShiftDown() )
		{

			selection.toggle( v );
		}
		else
		{
			selection.clearSelection();
			selection.setSelected( v, true );
		}
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
