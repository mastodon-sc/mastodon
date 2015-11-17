package net.trackmate.revised.bdv.overlay;

import net.trackmate.revised.ui.selection.SelectionListener;

public interface OverlaySelection< O extends OverlayVertex< ?, ? >, E extends OverlayEdge< ?, ? > >
{
	public void setSelected( O vertex, boolean selected );

	public void setSelected( E edge, boolean selected );

	public void toggleSelected( O vertex );

	public void toggleSelected( E edge );

	public boolean isVertexSelected( O vertex );

	public boolean isEdgeSelected( E edge );

	public void clearSelection();

	public boolean addSelectionListener( final SelectionListener l );

	public boolean removeSelectionListener( final SelectionListener l );
}
