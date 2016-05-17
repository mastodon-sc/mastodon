package net.trackmate.graph;

import java.util.Collection;
import java.util.Iterator;

import net.trackmate.graph.collection.IntRefMap;
import net.trackmate.graph.collection.RefCollection;
import net.trackmate.graph.collection.pool.IntPoolObjectMap;
import net.trackmate.graph.collection.pool.PoolObjectDeque;
import net.trackmate.graph.collection.pool.PoolObjectIntMap;
import net.trackmate.graph.collection.pool.PoolObjectList;
import net.trackmate.graph.collection.pool.PoolObjectObjectMap;
import net.trackmate.graph.collection.pool.PoolObjectPoolObjectMap;
import net.trackmate.graph.collection.pool.PoolObjectSet;
import net.trackmate.graph.collection.pool.PoolObjectStack;
import net.trackmate.graph.mempool.MappedElement;
import net.trackmate.graph.zzgraphinterfaces.Graph;
import net.trackmate.graph.zzgraphinterfaces.CollectionUtils.CollectionCreator;

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

	private final RefCollection< V > vertices = new MyAbstractCollection< V >()
	{
		@Override
		public Iterator< V > iterator()
		{
			return vertexPool.iterator();
		}

		@Override
		public int size()
		{
			return vertexPool.size();
		}

		@Override
		public V createRef()
		{
			return vertexPool.createRef();
		}

		@Override
		public void releaseRef( final V obj )
		{
			vertexPool.releaseRef( obj );
		}
	};

	private final RefCollection< E > edges = new MyAbstractCollection< E >()
	{
		@Override
		public Iterator< E > iterator()
		{
			return edgePool.iterator();
		}

		@Override
		public int size()
		{
			return edgePool.size();
		}

		@Override
		public E createRef()
		{
			return edgePool.createRef();
		}

		@Override
		public void releaseRef( final E obj )
		{
			edgePool.releaseRef( obj );
		}
	};

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
	public RefCollection< V > vertices()
	{
		return vertices;
	}

	@Override
	public RefCollection< E > edges()
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

	@Override
	public < O > PoolObjectObjectMap< V, O > createVertexObjectMap()
	{
		return new PoolObjectObjectMap<>( vertexPool );
	}

	@Override
	public < O > PoolObjectObjectMap< E, O > createEdgeObjectMap()
	{
		return new PoolObjectObjectMap<>( edgePool );
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

	@Override
	public IntRefMap< V > createIntVertexMap( final int noEntryKey )
	{
		return new IntPoolObjectMap< V >( vertexPool, noEntryKey );
	}

	@Override
	public IntRefMap< V > createIntVertexMap( final int noEntryKey, final int initialCapacity )
	{
		return new IntPoolObjectMap< V >( vertexPool, noEntryKey, initialCapacity );
	}

	// TODO: move to new file? may be generally useful.
	private static abstract class MyAbstractCollection< O > implements RefCollection< O >
	{
		@Override
		public boolean isEmpty()
		{
			return size() == 0;
		}

		@Override
		public boolean contains( final Object o )
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public Object[] toArray()
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public < T > T[] toArray( final T[] a )
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean add( final O e )
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean remove( final Object o )
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean containsAll( final Collection< ? > c )
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean addAll( final Collection< ? extends O > c )
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean removeAll( final Collection< ? > c )
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean retainAll( final Collection< ? > c )
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public void clear()
		{
			throw new UnsupportedOperationException();
		}
	}
}
