package net.trackmate.graph;

import java.util.Iterator;

import net.trackmate.graph.collection.CollectionUtils.CollectionCreator;
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
	public Iterator< V > vertexIterator()
	{
		return vertexPool.iterator();
	}

	@Override
	public Iterator< E > edgeIterator()
	{
		return edgePool.iterator();
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
	public PoolObjectSet< V > createVertexSet()
	{
		return new PoolObjectSet< V >( vertexPool );
	}

	@Override
	public PoolObjectSet< V > createVertexSet( final int initialCapacity )
	{
		return new PoolObjectSet< V >( vertexPool, initialCapacity );
	}

	@Override
	public PoolObjectSet< E > createEdgeSet()
	{
		return new PoolObjectSet< E >( edgePool );
	}

	@Override
	public PoolObjectSet< E > createEdgeSet( final int initialCapacity )
	{
		return new PoolObjectSet< E >( edgePool, initialCapacity );
	}

	@Override
	public PoolObjectList< V > createVertexList()
	{
		return new PoolObjectList< V >( vertexPool );
	}

	@Override
	public PoolObjectList< V > createVertexList( final int initialCapacity )
	{
		return new PoolObjectList< V >( vertexPool, initialCapacity );
	}

	@Override
	public PoolObjectList< E > createEdgeList()
	{
		return new PoolObjectList< E >( edgePool );
	}

	@Override
	public PoolObjectList< E > createEdgeList( final int initialCapacity )
	{
		return new PoolObjectList< E >( edgePool, initialCapacity );
	}

	@Override
	public PoolObjectDeque< V > createVertexDeque()
	{
		return new PoolObjectDeque< V >( vertexPool );
	}

	@Override
	public PoolObjectDeque< V > createVertexDeque( final int initialCapacity )
	{
		return new PoolObjectDeque< V >( vertexPool, initialCapacity );
	}

	@Override
	public PoolObjectDeque< E > createEdgeDeque()
	{
		return new PoolObjectDeque< E >( edgePool );
	}

	@Override
	public PoolObjectDeque< E > createEdgeDeque( final int initialCapacity )
	{
		return new PoolObjectDeque< E >( edgePool, initialCapacity );
	}

	@Override
	public PoolObjectStack< V > createVertexStack()
	{
		return new PoolObjectStack< V >( vertexPool );
	}

	@Override
	public PoolObjectStack< V > createVertexStack( final int initialCapacity )
	{
		return new PoolObjectStack< V >( vertexPool, initialCapacity );
	}

	@Override
	public PoolObjectStack< E > createEdgeStack()
	{
		return new PoolObjectStack< E >( edgePool );
	}

	@Override
	public PoolObjectStack< E > createEdgeStack( final int initialCapacity )
	{
		return new PoolObjectStack< E >( edgePool, initialCapacity );
	}

	@SuppressWarnings( { "unchecked", "rawtypes" } )
	@Override
	public < O > PoolObjectObjectMap< V, O > createVertexObjectMap( final Class< ? extends O > valueClass )
	{
		return new PoolObjectObjectMap( vertexPool );
	}

	@SuppressWarnings( { "rawtypes", "unchecked" } )
	@Override
	public < O > PoolObjectObjectMap< E, O > createEdgeObjectMap( final Class< ? extends O > valueClass )
	{
		return new PoolObjectObjectMap( edgePool );
	}

	@Override
	public PoolObjectPoolObjectMap< E, E > createEdgeEdgeMap()
	{
		return new PoolObjectPoolObjectMap< E, E >( edgePool, edgePool );
	}

	@Override
	public PoolObjectPoolObjectMap< E, E > createEdgeEdgeMap( final int initialCapacity )
	{
		return new PoolObjectPoolObjectMap< E, E >( edgePool, edgePool, initialCapacity );
	}

	@Override
	public PoolObjectPoolObjectMap< V, V > createVertexVertexMap()
	{
		return new PoolObjectPoolObjectMap< V, V >( vertexPool, vertexPool );
	}

	@Override
	public PoolObjectPoolObjectMap< V, V > createVertexVertexMap( final int initialCapacity )
	{
		return new PoolObjectPoolObjectMap< V, V >( vertexPool, vertexPool, initialCapacity );
	}

	@Override
	public PoolObjectPoolObjectMap< V, E > createVertexEdgeMap()
	{
		return new PoolObjectPoolObjectMap< V, E >( vertexPool, edgePool );
	}

	@Override
	public PoolObjectPoolObjectMap< V, E > createVertexEdgeMap( final int initialCapacity )
	{
		return new PoolObjectPoolObjectMap< V, E >( vertexPool, edgePool, initialCapacity );
	}

	@Override
	public PoolObjectPoolObjectMap< E, V > createEdgeVertexMap()
	{
		return new PoolObjectPoolObjectMap< E, V >( edgePool, vertexPool );
	}

	@Override
	public PoolObjectPoolObjectMap< E, V > createEdgeVertexMap( final int initialCapacity )
	{
		return new PoolObjectPoolObjectMap< E, V >( edgePool, vertexPool, initialCapacity );
	}

	@Override
	public PoolObjectIntMap< V > createVertexIntMap(final int noEntryValue )
	{
		return new PoolObjectIntMap< V >( vertexPool, noEntryValue );
	}

	@Override
	public PoolObjectIntMap< V > createVertexIntMap( final int noEntryValue, final int initialCapacity )
	{
		return new PoolObjectIntMap< V >( vertexPool, noEntryValue, initialCapacity );
	}

	@Override
	public PoolObjectIntMap< E > createEdgeIntMap(final int noEntryValue)
	{
		return new PoolObjectIntMap< E >( edgePool, noEntryValue );
	}

	@Override
	public PoolObjectIntMap< E > createEdgeIntMap( final int noEntryValue, final int initialCapacity )
	{
		return new PoolObjectIntMap< E >( edgePool, noEntryValue, initialCapacity );
	}
}
