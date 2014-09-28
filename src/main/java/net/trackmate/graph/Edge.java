package net.trackmate.graph;

public interface Edge< V extends Vertex< ? > >
{
	public V getSource();

	public V getSource( final V vertex );

	public V getTarget();

	public V getTarget( final V vertex );
}
