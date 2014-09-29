package net.trackmate.graph;

/**
 * A vertex in a {@link Graph}.
 *
 * @param <E>
 *            the {@link Edge} type of the {@link Graph}.
 *
 * @author Tobias Pietzsch <tobias.pietzsch@gmail.com>
 */
public interface Vertex< E extends Edge< ? > >
{
	public Edges< E > incomingEdges();

	public Edges< E > outgoingEdges();

	public Edges< E > edges();
}
