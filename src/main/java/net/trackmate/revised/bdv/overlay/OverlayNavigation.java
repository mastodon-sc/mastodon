package net.trackmate.revised.bdv.overlay;

import net.trackmate.graph.Vertex;
import net.trackmate.revised.ui.selection.NavigationListener;

public interface OverlayNavigation< V extends Vertex< ? >, O extends OverlayVertex< ?, ? > > extends NavigationListener< V >
{
	public void navigateToOverlayVertex( O vertex );
}
