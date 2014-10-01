package net.trackmate.graph;

/**
 * An edge between {@link Vertex vertices} of type {@code V}.
 *
 * @param <V>
 *            the {@link Vertex} type of the {@link Graph}.
 *
 * @author Tobias Pietzsch <tobias.pietzsch@gmail.com>
 */
public interface Edge< V extends Vertex< ? > >
{
	/**
	 * Get the source ("from") vertex of the edge in a new proxy object.
	 *
	 * <p>
	 * This allocates a new proxy object to hold the reference to the source
	 * vertex. It is recommended to use the allocation-free
	 * {@link #getSource(Vertex)} instead.
	 *
	 * @return the source vertex
	 */
	public V getSource();

	/**
	 * Get the source ("from") vertex of the edge in the provided proxy object.
	 *
	 * @param vertex
	 *            proxy object that will be set to reference the source vertex
	 *            of the edge.
	 * @return {@code vertex} (the source vertex).
	 */
	public V getSource( final V vertex );

	/**
	 * Get the target ("to") vertex of the edge in a new proxy object.
	 *
	 * <p>
	 * This allocates a new proxy object to hold the reference to the target
	 * vertex. It is recommended to use the allocation-free
	 * {@link #getTarget(Vertex)} instead.
	 *
	 * @return the target vertex
	 */
	public V getTarget();

	/**
	 * Get the target ("to") vertex of the edge in the provided proxy object.
	 *
	 * @param vertex
	 *            proxy object that will be set to reference the target vertex
	 *            of the edge.
	 * @return {@code vertex} (the target vertex).
	 */
	public V getTarget( final V vertex );
}
