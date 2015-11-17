package net.trackmate.revised.bdv.overlay;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

public class MouseOverListener< V extends OverlayVertex< V, E >, E extends OverlayEdge< E, V > > extends MouseAdapter
{
	private final OverlayGraphRenderer< V, E > graphOverlay;

	private final OverlayGraph< V, E > graph;

	private final OverlayHighlight< V, E > highlight;

	public MouseOverListener(
			final OverlayHighlight< V, E > highlight,
			final OverlayGraphRenderer< V, E > graphOverlay,
			final OverlayGraph< V, E > graph )
	{
		this.highlight = highlight;
		this.graphOverlay = graphOverlay;
		this.graph = graph;
	}

	@Override
	public void mouseExited( final MouseEvent e )
	{}

	@Override
	public void mouseMoved( final MouseEvent e )
	{
		final int x = e.getX();
		final int y = e.getY();
		final V ref = graph.vertexRef();
		final V v = graphOverlay.getVertexAt( x, y, ref );
		highlight.highlightVertex( v );
		graph.releaseRef( ref );
	}

	@Override
	public void mouseWheelMoved( final MouseWheelEvent e )
	{
		final int x = e.getX();
		final int y = e.getY();
		final V ref = graph.vertexRef();
		final V v = graphOverlay.getVertexAt( x, y, ref );
		highlight.highlightVertex( v );
		graph.releaseRef( ref );
	}
}
