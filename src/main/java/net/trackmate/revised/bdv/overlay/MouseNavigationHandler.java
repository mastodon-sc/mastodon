package net.trackmate.revised.bdv.overlay;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import net.trackmate.revised.ui.selection.NavigationGroupHandler;

public class MouseNavigationHandler< V extends OverlayVertex< V, E >, E extends OverlayEdge< E, V > > extends MouseAdapter
{

	private final OverlayGraphRenderer< V, E > renderer;

	private final OverlayGraph< V, E > overlayGraph;

	private final OverlayNavigation< V > navigation;

	private final NavigationGroupHandler groups;

	public MouseNavigationHandler(
			final OverlayGraph< V, E > overlayGraph,
			final OverlayGraphRenderer< V, E > renderer,
			final OverlayNavigation< V > navigation,
			final NavigationGroupHandler groups )
	{
		this.renderer = renderer;
		this.overlayGraph = overlayGraph;
		this.navigation = navigation;
		this.groups = groups;
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
			navigation.notifyListeners( groups, vertex );
		}
		overlayGraph.releaseRef( ref );
	}
}
