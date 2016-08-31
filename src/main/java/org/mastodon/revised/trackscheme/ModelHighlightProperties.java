package org.mastodon.revised.trackscheme;

import org.mastodon.revised.ui.selection.HighlightListener;

public interface ModelHighlightProperties
{
	public int getHighlightedVertexId();

	public int getHighlightedEdgeId();

	public void highlightVertex( final int id );

	public void highlightEdge( final int id );

	public void clearHighlight();

	public boolean addHighlightListener( final HighlightListener l );

	public boolean removeHighlightListener( final HighlightListener l );
}
