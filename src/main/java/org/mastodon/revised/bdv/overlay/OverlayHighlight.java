package org.mastodon.revised.bdv.overlay;

import org.mastodon.revised.ui.selection.HighlightListener;

public interface OverlayHighlight< V extends OverlayVertex< ?, ? >, E extends OverlayEdge< ?, ? > >
{
	public V getHighlightedVertex( V ref );

	public E getHighlightedEdge( E ref );

	public void highlightVertex( V vertex );

	public void highlightEdge( E edge );

	public void clearHighlight();

	public boolean addHighlightListener( final HighlightListener l );

	public boolean removeHighlightListener( final HighlightListener l );
}
