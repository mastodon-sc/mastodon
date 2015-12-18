package net.trackmate.graph.listenable;

import net.trackmate.graph.Edge;
import net.trackmate.graph.Vertex;

public interface GraphListener< V extends Vertex< E >, E extends Edge< V > >
{
	/**
	 * Called when the graph has been changed completely, for example, when it is loaded from a file.
	 * This should lead to a re-initialization of the listener.
	 */
	public void graphRebuilt();

	/**
	 * Called when a vertex was added to the graph.
	 *
	 * @param vertex
	 *            the vertex added.
	 */
	public void vertexAdded( V vertex );

	/**
	 * Called before a vertex is removed from the graph.
	 *
	 * @param vertex
	 *            the vertex removed.
	 */

	public void vertexRemoved( V vertex );

	/**
	 * Call when an edge was added to the graph.
	 *
	 * @param edge
	 *            the edge added.
	 */
	public void edgeAdded( E edge );

	/**
	 * Called before an edge is removed from the graph.
	 *
	 * @param edge
	 *            the edge removed.
	 */
	public void edgeRemoved( E edge );

//	/**
//	 * Called when a {@code beginUpdate()} event is called on the graph.
//	 */
//	public void updateBegun();
//
//	/**
//	 * Called when a {@code endUpdate()} event is called on the graph.
//	 */
//	public void updateEnded();
//
//	/**
//	 * Called when a {@code pauseUpdate()} event is called on the graph.
//	 */
//	public void updatePaused();
//
//	/**
//	 * Called when a {@code resumeUpdate()} event is called on the graph.
//	 */
//	public void updateResumed();
}
