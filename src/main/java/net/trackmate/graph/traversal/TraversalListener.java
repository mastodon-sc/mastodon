package net.trackmate.graph.traversal;

public interface TraversalListener< V, E >
{
	public void processVertexLate( final V vertex, final int time );

	public void processVertexEarly( final V vertex, final int time );

	public void processEdge( final E edge, final V from, final V to, final int time );
}
