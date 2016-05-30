package net.trackmate.graph.ref;

import net.trackmate.collection.util.AbstractRefCollection;
import net.trackmate.graph.Graph;
import net.trackmate.pool.MappedElement;

public class GraphImp<
		VP extends AbstractVertexPool< V, E, T >,
		EP extends AbstractEdgePool< E, V, T >,
		V extends AbstractVertex< V, E, T >,
		E extends AbstractEdge< E, V, T >,
		T extends MappedElement >
	implements Graph< V, E >
{
	protected final VP vertexPool;

	protected final EP edgePool;

	private final AbstractRefCollection< V > vertices;

	private final AbstractRefCollection< E > edges;

	public GraphImp( final VP vertexPool, final EP edgePool )
	{
		this.vertexPool = vertexPool;
		this.edgePool = edgePool;
		vertexPool.linkEdgePool( edgePool );
		vertices = new AbstractRefCollection<>( vertexPool );
		edges = new AbstractRefCollection<>( edgePool );
	}

	@SuppressWarnings( "unchecked" )
	public GraphImp( final EP edgePool )
	{
		this.vertexPool = ( VP ) edgePool.vertexPool;
		this.edgePool = edgePool;
		vertexPool.linkEdgePool( edgePool );
		vertices = new AbstractRefCollection<>( vertexPool );
		edges = new AbstractRefCollection<>( edgePool );
	}

	@Override
	public V addVertex()
	{
		return vertexPool.create( vertexRef() );
	}

	@Override
	public V addVertex( final V vertex )
	{
		return vertexPool.create( vertex );
	}

	@Override
	public E addEdge( final V source, final V target )
	{
		return edgePool.addEdge( source, target, edgeRef() );
	}

	@Override
	public E addEdge( final V source, final V target, final E edge )
	{
		return edgePool.addEdge( source, target, edge );
	}

	@Override
	public E insertEdge( final V source, final int sourceOutIndex, final V target, final int targetInIndex )
	{
		return edgePool.insertEdge( source, sourceOutIndex, target, targetInIndex, edgeRef() );
	}

	@Override
	public E insertEdge( final V source, final int sourceOutIndex, final V target, final int targetInIndex, final E edge )
	{
		return edgePool.insertEdge( source, sourceOutIndex, target, targetInIndex, edge );
	}

	@Override
	public E getEdge( final V source, final V target )
	{
		return edgePool.getEdge( source, target, edgeRef() );
	}

	@Override
	public E getEdge( final V source, final V target, final E edge )
	{
		return edgePool.getEdge( source, target, edge );
	}

	@Override
	public AbstractRefCollection< V > vertices()
	{
		return vertices;
	}

	@Override
	public AbstractRefCollection< E > edges()
	{
		return edges;
	}

	@Override
	public void remove( final V vertex )
	{
		vertexPool.delete( vertex );
	}

	@Override
	public void remove( final E edge )
	{
		edgePool.delete( edge );
	}

	@Override
	public void removeAllLinkedEdges( final V vertex )
	{
		edgePool.deleteAllLinkedEdges( vertex );
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

	protected void clear()
	{
		vertexPool.clear();
		edgePool.clear();
	}
}
