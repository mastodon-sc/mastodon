package net.trackmate.revised.trackscheme.display;

import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import net.trackmate.revised.trackscheme.TrackSchemeFocus;
import net.trackmate.revised.trackscheme.TrackSchemeGraph;
import net.trackmate.revised.trackscheme.TrackSchemeNavigation;
import net.trackmate.revised.trackscheme.TrackSchemeVertex;

public class FocusHandler extends MouseAdapter
{
	private static final int MOUSE_MASK_CLICK = InputEvent.BUTTON1_MASK;

	private final TrackSchemeFocus focus;

	private final AbstractTrackSchemeOverlay graphOverlay;

	private final TrackSchemeNavigation navigation;

	private final TrackSchemeGraph< ?, ? > graph;

	public FocusHandler(
			final TrackSchemeNavigation navigation,
			final TrackSchemeFocus focus,
			final TrackSchemeGraph< ?, ? > graph,
			final AbstractTrackSchemeOverlay graphOverlay )
	{
		this.navigation = navigation;
		this.focus = focus;
		this.graph = graph;
		this.graphOverlay = graphOverlay;
	}

	@Override
	public void mouseClicked( final MouseEvent e )
	{
		if ( e.getModifiers() == MOUSE_MASK_CLICK )
		{
			final TrackSchemeVertex ref = graph.vertexRef();
			final TrackSchemeVertex vertex = graphOverlay.getVertexAt( e.getX(), e.getY(), ref );

			// Single click: We set the focus to the clicked vertex but do not navigate.
			focus.focusVertex( vertex );

			if ( vertex != null && e.getClickCount() == 2 )
			{
				// Double click: We navigate to the clicked vertex.
				navigation.notifyNavigateToVertex( ref );
			}

			graph.releaseRef( ref );
		}
	}
}
