package net.trackmate.graph.traversal;

import net.trackmate.graph.Edge;
import net.trackmate.graph.Vertex;

public interface SearchListener< V extends Vertex< E >, E extends Edge< V > >
{
	public void processVertexLate( final V vertex, GraphSearch< V, E > search );

	public void processVertexEarly( final V vertex, GraphSearch< V, E > search );

	public void processEdge( final E edge, final V from, final V to, GraphSearch< V, E > search );
}
