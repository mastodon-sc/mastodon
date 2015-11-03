package net.trackmate.revised.trackscheme;

import net.trackmate.revised.ui.selection.HighlightListener;

public interface ModelHighlightProperties
{
	public int getHighlightedVertexId();

	public void highlightVertex( final int id );

	public boolean addHighlightListener( final HighlightListener l );

	public boolean removeHighlightListener( final HighlightListener l );
}
