package net.trackmate.revised.bdv.overlay;

import net.trackmate.revised.ui.selection.FocusListener;

public interface OverlayFocus< O extends OverlayVertex< ?, ? >, E extends OverlayEdge< ?, ? > >
{
	public O getFocusedVertex( O ref );

	public void focusVertex( O vertex );

	public boolean addFocusListener( final FocusListener l );

	public boolean removeFocusListener( final FocusListener l );
}
