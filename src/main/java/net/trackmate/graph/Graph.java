package net.trackmate.graph;

public interface Graph< V extends Vertex< E >, E extends Edge< V > >
{
	public V addVertex();

	public V addVertex( final V vertex );

	public E addEdge( final V source, final V target );

	public E addEdge( final V source, final V target, final E edge );

	public E getEdge( final V source, final V target );

	public E getEdge( final V source, final V target, final E edge );

	public void remove( final V vertex );

	public void remove( final E edge );

	public void removeAllLinkedEdges( final V vertex );

	public V vertexRef();

	public E edgeRef();

	public void releaseRef( final V ref );

	public void releaseRef( final E ref );

	public void releaseRef( final V ... refs );

	public void releaseRef( final E ... refs );
}
