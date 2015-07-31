package net.trackmate.bdv.wrapper;

import net.trackmate.graph.Edge;
import net.trackmate.graph.Ref;

public interface OverlayEdge< O extends OverlayEdge< O, V >, V extends OverlayVertex< V, ? > >
		extends Edge< V >, Ref< O >
{
}
