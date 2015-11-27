package net.trackmate.revised.bdv.overlay;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class MouseNavigationHandler< V extends OverlayVertex< V, E >, E extends OverlayEdge< E, V > > extends MouseAdapter
{

	private final OverlayGraphRenderer< V, E > renderer;

	private final OverlayGraph< V, E > overlayGraph;

	private final OverlayNavigation< V > navigation;

	public MouseNavigationHandler(
			final OverlayGraph< V, E > overlayGraph,
			final OverlayGraphRenderer< V, E > renderer,
			final OverlayNavigation< V > navigation )
	{
		this.renderer = renderer;
		this.overlayGraph = overlayGraph;
		this.navigation = navigation;
	}

	@Override
	public void mouseClicked( final MouseEvent e )
	{
		if ( e.getClickCount() != 2 )
			return;

		final V ref = overlayGraph.vertexRef();
		final V vertex = renderer.getVertexAt( e.getX(), e.getY(), ref );
		if ( vertex != null )
		{
			navigation.notifyListeners( vertex );
		}
		overlayGraph.releaseRef( ref );
	}
}
