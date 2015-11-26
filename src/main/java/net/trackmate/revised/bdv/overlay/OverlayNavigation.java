package net.trackmate.revised.bdv.overlay;

import net.trackmate.revised.ui.selection.NavigationGroupHandler;

public interface OverlayNavigation< O extends OverlayVertex< ?, ? > >
{
	public void navigateToOverlayVertex( O vertex );

	public void notifyListeners( NavigationGroupHandler groups, O vertex );
}
