package net.trackmate.graph;

import net.trackmate.graph.mempool.MappedElement;

public class GraphImp<
		VP extends AbstractVertexPool< V, T, E >,
		EP extends AbstractEdgePool< E, T, V >,
		V extends AbstractVertex< T, E >,
		E extends AbstractEdge< T, V >,
		T extends MappedElement >
	implements Graph< V, E >
{
	public static <
			VP extends AbstractVertexPool< V, T, E >,
			EP extends AbstractEdgePool< E, T, V >,
			V extends AbstractVertex< T, E >,
			E extends AbstractEdge< T, V >,
			T extends MappedElement >
	GraphImp< VP, EP, V, E, T > create( final VP vertexPool, final EP edgePool )
	{
		return new GraphImp< VP, EP, V, E, T >( vertexPool, edgePool );
	}

	protected final VP vertexPool;

	protected final EP edgePool;

	public GraphImp( final VP vertexPool, final EP edgePool )
	{
		this.vertexPool = vertexPool;
		this.edgePool = edgePool;
	}

	@Override
	public V addVertex()
	{
		return vertexPool.create();
	}

	@Override
	public V addVertex( final V vertex )
	{
		return vertexPool.create( vertex );
	}

	@Override
	public E addEdge( final V source, final V target )
	{
		return edgePool.addEdge( source, target );
	}

	@Override
	public E addEdge( final V source, final V target, final E edge )
	{
		return edgePool.addEdge( source, target, edge );
	}

	@Override
	public E getEdge( final V source, final V target )
	{
		return edgePool.getEdge( source, target );
	}

	@Override
	public E getEdge( final V source, final V target, final E edge )
	{
		return edgePool.getEdge( source, target, edge );
	}

	@Override
	public void remove( final V vertex )
	{
		vertexPool.release( vertex );
	}

	@Override
	public void remove( final E edge )
	{
		edgePool.release( edge );
	}

	@Override
	public void removeAllLinkedEdges( final V vertex )
	{
		edgePool.releaseAllLinkedEdges( vertex );
	}

	@Override
	public V vertexRef()
	{
		return vertexPool.createRef();
	}

	@Override
	public E edgeRef()
	{
		return edgePool.createRef();
	}

	@Override
	public void releaseRef( final V ref )
	{
		vertexPool.releaseRef( ref );
	}

	@Override
	public void releaseRef( final E ref )
	{
		edgePool.releaseRef( ref );
	}

	@Override
	public void releaseRef( final V... refs )
	{
		for ( final V ref : refs )
			vertexPool.releaseRef( ref );
	}

	@Override
	public void releaseRef( final E... refs )
	{
		for ( final E ref : refs )
			edgePool.releaseRef( ref );
	}
}
