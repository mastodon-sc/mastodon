package net.trackmate.graph.zzgraphinterfaces;

/**
 * A vertex in a {@link Graph}.
 *
 * @param <E>
 *            the {@link Edge} type of the {@link Graph}.
 *
 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
 */
public interface Vertex< E extends Edge< ? > >
{
	/**
	 * Get the list of all edges going to the vertex.
	 *
	 * @return the (iterable) list of edges to the vertex.
	 */
	public Edges< E > incomingEdges();

	/**
	 * Get the list of all edges leaving the vertex.
	 *
	 * @return the (iterable) list of edges from the vertex.
	 */
	public Edges< E > outgoingEdges();

	/**
	 * Get the list of all edges connected to the vertex.
	 *
	 * @return the (iterable) list of edges to and from the vertex.
	 */
	public Edges< E > edges();
}
