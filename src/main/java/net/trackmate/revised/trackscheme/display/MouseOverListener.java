package net.trackmate.revised.trackscheme.display;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import net.trackmate.revised.trackscheme.TrackSchemeHighlight;

public class MouseOverListener extends MouseAdapter
{
	private final AbstractTrackSchemeOverlay graphOverlay;

	private final TrackSchemeHighlight< ?, ? > highlight;

	public MouseOverListener( final AbstractTrackSchemeOverlay graphOverlay, final TrackSchemeHighlight< ?, ? > highlight )
	{
		this.graphOverlay = graphOverlay;
		this.highlight = highlight;
	}

	@Override
	public void mouseMoved( final MouseEvent e )
	{
		final int x = e.getX();
		final int y = e.getY();

		final int id = graphOverlay.getVertexIdAt( x, y );
		if ( id >= 0 )
		{
			highlight.highlightVertex( id );
			return;
		}
		highlight.highlightVertex( -1 );
	}
}
