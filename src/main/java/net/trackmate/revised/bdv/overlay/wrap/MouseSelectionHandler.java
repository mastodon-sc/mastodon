package net.trackmate.revised.bdv.overlay.wrap;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import net.trackmate.graph.Edge;
import net.trackmate.graph.Vertex;
import net.trackmate.revised.bdv.overlay.OverlayGraphRenderer;
import net.trackmate.revised.ui.selection.Selection;

public class MouseSelectionHandler< V extends Vertex< E >, E extends Edge< V > > implements MouseListener
{

	private final OverlayGraphRenderer< OverlayVertexWrapper< V, E >, OverlayEdgeWrapper< V, E > > renderer;

	private final OverlayGraphWrapper< V, E > overlayGraph;

	private final Selection< V, E > selection;

	public MouseSelectionHandler( final OverlayGraphWrapper< V, E > overlayGraph, final OverlayGraphRenderer< OverlayVertexWrapper< V, E >, OverlayEdgeWrapper< V, E > > renderer, final Selection< V, E > selection )
	{
		this.renderer = renderer;
		this.overlayGraph = overlayGraph;
		this.selection = selection;
	}

	@Override
	public void mouseClicked( final MouseEvent e )
	{
		final OverlayVertexWrapper< V, E > ref = overlayGraph.vertexRef();
		final OverlayVertexWrapper< V, E > v = renderer.getVertexAt( e.getX(), e.getY(), ref );
		if ( v == null )
		{
			if ( !e.isShiftDown() )
				selection.clearSelection();
			return;
		}

		if ( e.isShiftDown() )
		{
			selection.toggle( v.wv );
		}
		else
		{
			selection.clearSelection();
			selection.setSelected( v.wv, true );
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
