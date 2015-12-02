package net.trackmate.revised.trackscheme.display;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import net.trackmate.revised.trackscheme.TrackSchemeGraph;
import net.trackmate.revised.trackscheme.TrackSchemeHighlight;
import net.trackmate.revised.trackscheme.TrackSchemeVertex;

public class MouseHighlightHandler extends MouseAdapter
{
	private final AbstractTrackSchemeOverlay graphOverlay;

	private final TrackSchemeHighlight highlight;

	private final TrackSchemeGraph< ?, ? > graph;

	public MouseHighlightHandler(
			final AbstractTrackSchemeOverlay graphOverlay,
			final TrackSchemeHighlight highlight,
			final TrackSchemeGraph< ?, ? > graph )
	{
		this.graphOverlay = graphOverlay;
		this.highlight = highlight;
		this.graph = graph;
	}

	@Override
	public void mouseMoved( final MouseEvent e )
	{
		final int x = e.getX();
		final int y = e.getY();

		final int id = graphOverlay.getVertexIdAt( x, y );
		if ( id >= 0 )
		{
			final TrackSchemeVertex ref = graph.vertexRef();
			graph.getVertexPool().getByInternalPoolIndex( id, ref );
			highlight.highlightVertex( ref );
			graph.releaseRef( ref );
		}
		else
			highlight.highlightVertex( null );
	}
}
