package net.trackmate.graph.ref;

public interface NotifyPostInit< V, E >
{
	public void notifyVertexAdded( final V vertex );

	public void notifyEdgeAdded( final E edge );
}