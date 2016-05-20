package net.trackmate.undo;

import net.trackmate.graph.Edge;
import net.trackmate.graph.Graph;
import net.trackmate.graph.Vertex;

/**
 * Provides serialization of vertices and edges to a byte array, for a specific {@link Graph} class.
 *
 * @param <V>
 * @param <E>
 *
 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
 */
public interface UndoSerializer< V extends Vertex< E >, E extends Edge< V > >
{
	public int getVertexNumBytes();

	/**
	 * Store data from {@code vertex} into {@code bytes}.
	 */
	public void getBytes( final V vertex, final byte[] bytes );

	/**
	 * Restore data from {@code bytes} into {@code vertex}.
	 */
	public void setBytes( final V vertex, final byte[] bytes );

	/**
	 * TODO javadoc
	 *
	 * @param vertex
	 */
	public void notifyVertexAdded( final V vertex );

	public int getEdgeNumBytes();

	/**
	 * Store data from {@code edge} into {@code bytes}.
	 */
	public void getBytes( final E edge, final byte[] bytes );

	/**
	 * Restore data from {@code bytes} into {@code edge}.
	 */
	public void setBytes( final E edge, final byte[] bytes );

	/**
	 * TODO javadoc
	 *
	 * @param edge
	 */
	public void notifyEdgeAdded( final E edge );
}
