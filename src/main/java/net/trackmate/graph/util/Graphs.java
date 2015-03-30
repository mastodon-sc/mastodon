package net.trackmate.graph.util;

import net.trackmate.graph.Edge;
import net.trackmate.graph.Vertex;

/**
 * A collection of utilities to assist with graph manipulation.
 */
public class Graphs
{
	/**
	 * Gets the vertex opposite another vertex across an edge.
	 * <p>
	 * If the specified vertex does not have the specified edge in its list of
	 * edges, one or the other vertex of the specified edge is returned.
	 * 
	 * @param edge
	 *            the edge to inspect.
	 * @param vertex
	 *            the vertex on the undesired side of the edge.
	 * @param tmp
	 *            an object reference that can be used for retrieval. Depending
	 *            on concrete implementation, this object can be cleared,
	 *            ignored or re-used.
	 * @return the vertex opposite to the specified vertex across the specified
	 *         edge.
	 */
	public static final < V extends Vertex< E >, E extends Edge< V > > V getOppositeVertex( final E edge, final V vertex, final V tmp )
	{
		final V other = edge.getSource( tmp );
		if ( other.equals( vertex ) ) { return edge.getTarget( tmp ); }
		return other;
	}

	private Graphs()
	{}

}
