package net.trackmate.graph;

import net.trackmate.graph.collection.CollectionUtils.CollectionCreator;
import net.trackmate.graph.collection.RefDeque;
import net.trackmate.graph.collection.RefList;
import net.trackmate.graph.collection.RefSet;
import net.trackmate.graph.collection.RefStack;
import net.trackmate.graph.mempool.MappedElement;

public class GraphImp<
		VP extends AbstractVertexPool< V, E, T >,
		EP extends AbstractEdgePool< E, V, T >,
		V extends AbstractVertex< V, E, T >,
		E extends AbstractEdge< E, V, T >,
		T extends MappedElement >
	implements Graph< V, E >, CollectionCreator< V, E >
{
	public static <
			VP extends AbstractVertexPool< V, E, T >,
			EP extends AbstractEdgePool< E, V, T >,
			V extends AbstractVertex< V, E, T >,
			E extends AbstractEdge< E, V, T >,
			T extends MappedElement >
	GraphImp< VP, EP, V, E, T > create( final VP vertexPool, final EP edgePool )
	{
		return new GraphImp< VP, EP, V, E, T >( vertexPool, edgePool );
	}

	public static <
			VP extends AbstractVertexPool< V, E, T >,
			EP extends AbstractEdgePool< E, V, T >,
			V extends AbstractVertex< V, E, T >,
			E extends AbstractEdge< E, V, T >,
			T extends MappedElement >
	GraphImp< VP, EP, V, E, T > create( final EP edgePool )
	{
		return new GraphImp< VP, EP, V, E, T >( edgePool );
	}

	protected final VP vertexPool;

	protected final EP edgePool;

	public GraphImp( final VP vertexPool, final EP edgePool )
	{
		this.vertexPool = vertexPool;
		this.edgePool = edgePool;
		vertexPool.linkEdgePool( edgePool );
	}

	@SuppressWarnings( "unchecked" )
	public GraphImp( final EP edgePool )
	{
		this.vertexPool = ( VP ) edgePool.vertexPool;
		this.edgePool = edgePool;
		vertexPool.linkEdgePool( edgePool );
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

	@Override
	public RefSet< V > createVertexSet()
	{
		return new PoolObjectSet< V, T >( vertexPool );
	}

	@Override
	public RefSet< V > createVertexSet( final int initialCapacity )
	{
		return new PoolObjectSet< V, T >( vertexPool, initialCapacity );
	}

	@Override
	public RefSet< E > createEdgeSet()
	{
		return new PoolObjectSet< E, T >( edgePool );
	}

	@Override
	public RefSet< E > createEdgeSet( final int initialCapacity )
	{
		return new PoolObjectSet< E, T >( edgePool, initialCapacity );
	}

	@Override
	public RefList< V > createVertexList()
	{
		return new PoolObjectList< V, T >( vertexPool );
	}

	@Override
	public RefList< V > createVertexList( final int initialCapacity )
	{
		return new PoolObjectList< V, T >( vertexPool, initialCapacity );
	}

	@Override
	public RefList< E > createEdgeList()
	{
		return new PoolObjectList< E, T >( edgePool );
	}

	@Override
	public RefList< E > createEdgeList( final int initialCapacity )
	{
		return new PoolObjectList< E, T >( edgePool, initialCapacity );
	}

	@Override
	public RefDeque< V > createVertexDeque()
	{
		return new PoolObjectDeque< V, T >( vertexPool );
	}

	@Override
	public RefDeque< V > createVertexDeque( final int initialCapacity )
	{
		return new PoolObjectDeque< V, T >( vertexPool, initialCapacity );
	}

	@Override
	public RefDeque< E > createEdgeDeque()
	{
		return new PoolObjectDeque< E, T >( edgePool );
	}

	@Override
	public RefDeque< E > createEdgeDeque( final int initialCapacity )
	{
		return new PoolObjectDeque< E, T >( edgePool, initialCapacity );
	}

	@Override
	public RefStack< V > createVertexStack()
	{
		return new PoolObjectStack< V, T >( vertexPool );
	}

	@Override
	public RefStack< V > createVertexStack( final int initialCapacity )
	{
		return new PoolObjectStack< V, T >( vertexPool, initialCapacity );
	}

	@Override
	public RefStack< E > createEdgeStack()
	{
		return new PoolObjectStack< E, T >( edgePool );
	}

	@Override
	public RefStack< E > createEdgeStack( final int initialCapacity )
	{
		return new PoolObjectStack< E, T >( edgePool, initialCapacity );
	}
}
