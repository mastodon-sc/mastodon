package net.trackmate.revised.model;

import net.trackmate.graph.AbstractVertex;
import net.trackmate.graph.Edge;
import net.trackmate.graph.Graph;
import net.trackmate.graph.Vertex;
import net.trackmate.graph.listenable.ListenableGraph;

public interface ModelGraph_HACK_FIX_ME< V extends Vertex< E >, E extends Edge< V > > extends Graph< V, E >, ListenableGraph< V, E >
{
	/**
	 * Sends {@link GraphListener#vertexAdded(net.trackmate.graph.Vertex)} for
	 * the specified vertex. Must be called, after
	 * {@link #addVertex(AbstractVertex)} and {@code Vertex.init(...)} has been
	 * called.
	 *
	 * @param vertex
	 *            vertex for which to send
	 *            {@link GraphListener#vertexAdded(net.trackmate.graph.Vertex)}.
	 * @return the specified {@code vertex} argument.
	 */
	public V notifyVertexAdded( final V vertex );
}
