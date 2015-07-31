package net.trackmate.graph.listenable;

import net.trackmate.graph.Edge;
import net.trackmate.graph.Vertex;

public interface GraphChangeEventListener< V extends Vertex< E >, E extends Edge< V > >
{
	public void graphChanged( GraphChangeEvent< V, E > changeEvent );

}
