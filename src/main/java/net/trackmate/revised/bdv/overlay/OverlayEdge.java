package net.trackmate.revised.bdv.overlay;

import net.trackmate.graph.zzgraphinterfaces.Edge;
import net.trackmate.graph.zzrefcollections.Ref;

public interface OverlayEdge< O extends OverlayEdge< O, V >, V extends OverlayVertex< V, ? > >
		extends Edge< V >, Ref< O >
{
	public boolean isSelected();
}
