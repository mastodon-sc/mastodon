package net.trackmate.graph.algorithm.traversal;

import net.trackmate.graph.Edge;
import net.trackmate.graph.Vertex;

public interface SearchListener< V extends Vertex< E >, E extends Edge< V >, G extends GraphSearch< G, V, E > >
{
	public void processVertexLate( final V vertex, G search );

	public void processVertexEarly( final V vertex, G search );

	public void processEdge( final E edge, final V from, final V to, G search );
}
