package net.trackmate.bdv.wrapper;

import net.trackmate.graph.Ref;
import net.trackmate.graph.Vertex;
import net.trackmate.trackscheme.HasTimepoint;

public interface OverlayVertex< O extends OverlayVertex< O, E >, E extends OverlayEdge< E, ? > >
		extends Vertex< E >, Ref< O >, HasTimepoint
{
	public boolean isSelected();
}
