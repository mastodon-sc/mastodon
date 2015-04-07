package net.trackmate.graph.traversal;

public interface GraphTraversalListener< V, E >
{
	public void edgeTraversed( E edge );

	public void vertexTraversed( V vertex );
}
