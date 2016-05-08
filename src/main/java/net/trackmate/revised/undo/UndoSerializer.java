package net.trackmate.revised.undo;

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

	public void getBytes( final V vertex, final byte[] bytes );

	public void setBytes( final V vertex, final byte[] bytes );

	public int getEdgeNumBytes();

	public void getBytes( final E edge, final byte[] bytes );

	public void setBytes( final E edge, final byte[] bytes );
}