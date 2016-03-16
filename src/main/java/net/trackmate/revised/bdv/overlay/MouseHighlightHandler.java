package net.trackmate.revised.bdv.overlay;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

public class MouseHighlightHandler< V extends OverlayVertex< V, E >, E extends OverlayEdge< E, V > > extends MouseAdapter
{
	private final OverlayGraph< V, E > overlayGraph;

	private final OverlayGraphRenderer< V, E > renderer;

	private final OverlayHighlight< V, E > highlight;

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
	public void mouseExited( final MouseEvent e )
	{}

	@Override
	public void mouseMoved( final MouseEvent e )
	{
		final int x = e.getX();
		final int y = e.getY();
		final V ref = overlayGraph.vertexRef();
		final V v = renderer.getVertexAt( x, y, ref );
		highlight.highlightVertex( v );
		overlayGraph.releaseRef( ref );
	}

	@Override
	public void mouseWheelMoved( final MouseWheelEvent e )
	{
		final int x = e.getX();
		final int y = e.getY();
		final V ref = overlayGraph.vertexRef();
		final V v = renderer.getVertexAt( x, y, ref );
		highlight.highlightVertex( v );
		overlayGraph.releaseRef( ref );
	}
}
