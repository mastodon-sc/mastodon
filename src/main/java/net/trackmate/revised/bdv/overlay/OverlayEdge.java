package net.trackmate.revised.bdv.overlay;

import net.trackmate.Ref;
import net.trackmate.graph.Edge;

public interface OverlayEdge< O extends OverlayEdge< O, V >, V extends OverlayVertex< V, ? > >
		extends Edge< V >, Ref< O >
{
	public boolean isSelected();
}
