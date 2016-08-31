package org.mastodon.graph.algorithm.traversal;

import org.mastodon.graph.Edge;
import org.mastodon.graph.Vertex;

public interface SearchListener< V extends Vertex< E >, E extends Edge< V >, G extends GraphSearch< G, V, E > >
{
	public void processVertexLate( final V vertex, G search );

	public void processVertexEarly( final V vertex, G search );

	public void processEdge( final E edge, final V from, final V to, G search );
}
