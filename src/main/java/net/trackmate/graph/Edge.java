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
	public V getSource();

	public V getSource( final V vertex );

	public V getTarget();

	public V getTarget( final V vertex );
}
