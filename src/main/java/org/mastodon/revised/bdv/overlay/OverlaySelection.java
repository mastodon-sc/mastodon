package org.mastodon.revised.bdv.overlay;

import org.mastodon.revised.ui.selection.SelectionListener;

public interface OverlaySelection< O extends OverlayVertex< ?, ? >, E extends OverlayEdge< ?, ? > >
{
	public void setSelected( O vertex, boolean selected );

	public void setSelected( E edge, boolean selected );

	public void toggleSelected( O vertex );

	public void toggleSelected( E edge );

	public void clearSelection();

	public boolean addSelectionListener( final SelectionListener l );

	public boolean removeSelectionListener( final SelectionListener l );

	public void pauseListeners();

	public void resumeListeners();
}
