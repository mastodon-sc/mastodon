package net.trackmate.revised.model;

import net.trackmate.graph.Edge;
import net.trackmate.graph.Graph;
import net.trackmate.graph.GraphListener;
import net.trackmate.graph.ListenableGraph;
import net.trackmate.graph.Vertex;

public interface ModelGraph_HACK_FIX_ME< V extends Vertex< E >, E extends Edge< V > > extends Graph< V, E >, ListenableGraph< V, E >
{
	/**
	 * Sends {@link GraphListener#vertexAdded(Vertex)} for the specified vertex.
	 * Must be called, after {code addVertex(...)} and {@code Vertex.init(...)}
	 * have been called.
	 *
	 * @param vertex
	 *            vertex for which to send
	 *            {@link GraphListener#vertexAdded(Vertex)}.
	 * @return the specified {@code vertex} argument.
	 */
	public V notifyVertexAdded( final V vertex );
}
