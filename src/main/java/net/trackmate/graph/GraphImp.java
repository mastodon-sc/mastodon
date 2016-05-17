package net.trackmate.graph;

import java.util.Collection;
import java.util.Iterator;

import net.trackmate.graph.collection.IntRefMap;
import net.trackmate.graph.collection.RefCollection;
import net.trackmate.graph.collection.pool.IntRefHashMap;
import net.trackmate.graph.collection.pool.RefArrayDeque;
import net.trackmate.graph.collection.pool.RefIntHashMap;
import net.trackmate.graph.collection.pool.RefArrayList;
import net.trackmate.graph.collection.pool.RefObjectHashMap;
import net.trackmate.graph.collection.pool.RefRefHashMap;
import net.trackmate.graph.collection.pool.RefSetImp;
import net.trackmate.graph.collection.pool.RefArrayStack;
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
	public RefSetImp< V > createVertexSet()
	{
		return new RefSetImp< V >( vertexPool );
	}

	@Override
	public RefSetImp< V > createVertexSet( final int initialCapacity )
	{
		return new RefSetImp< V >( vertexPool, initialCapacity );
	}

	@Override
	public RefSetImp< E > createEdgeSet()
	{
		return new RefSetImp< E >( edgePool );
	}

	@Override
	public RefSetImp< E > createEdgeSet( final int initialCapacity )
	{
		return new RefSetImp< E >( edgePool, initialCapacity );
	}

	@Override
	public RefArrayList< V > createVertexList()
	{
		return new RefArrayList< V >( vertexPool );
	}

	@Override
	public RefArrayList< V > createVertexList( final int initialCapacity )
	{
		return new RefArrayList< V >( vertexPool, initialCapacity );
	}

	@Override
	public RefArrayList< E > createEdgeList()
	{
		return new RefArrayList< E >( edgePool );
	}

	@Override
	public RefArrayList< E > createEdgeList( final int initialCapacity )
	{
		return new RefArrayList< E >( edgePool, initialCapacity );
	}

	@Override
	public RefArrayDeque< V > createVertexDeque()
	{
		return new RefArrayDeque< V >( vertexPool );
	}

	@Override
	public RefArrayDeque< V > createVertexDeque( final int initialCapacity )
	{
		return new RefArrayDeque< V >( vertexPool, initialCapacity );
	}

	@Override
	public RefArrayDeque< E > createEdgeDeque()
	{
		return new RefArrayDeque< E >( edgePool );
	}

	@Override
	public RefArrayDeque< E > createEdgeDeque( final int initialCapacity )
	{
		return new RefArrayDeque< E >( edgePool, initialCapacity );
	}

	@Override
	public RefArrayStack< V > createVertexStack()
	{
		return new RefArrayStack< V >( vertexPool );
	}

	@Override
	public RefArrayStack< V > createVertexStack( final int initialCapacity )
	{
		return new RefArrayStack< V >( vertexPool, initialCapacity );
	}

	@Override
	public RefArrayStack< E > createEdgeStack()
	{
		return new RefArrayStack< E >( edgePool );
	}

	@Override
	public RefArrayStack< E > createEdgeStack( final int initialCapacity )
	{
		return new RefArrayStack< E >( edgePool, initialCapacity );
	}

	@Override
	public < O > RefObjectHashMap< V, O > createVertexObjectMap()
	{
		return new RefObjectHashMap<>( vertexPool );
	}

	@Override
	public < O > RefObjectHashMap< E, O > createEdgeObjectMap()
	{
		return new RefObjectHashMap<>( edgePool );
	}

	@Override
	public RefRefHashMap< E, E > createEdgeEdgeMap()
	{
		return new RefRefHashMap< E, E >( edgePool, edgePool );
	}

	@Override
	public RefRefHashMap< E, E > createEdgeEdgeMap( final int initialCapacity )
	{
		return new RefRefHashMap< E, E >( edgePool, edgePool, initialCapacity );
	}

	@Override
	public RefRefHashMap< V, V > createVertexVertexMap()
	{
		return new RefRefHashMap< V, V >( vertexPool, vertexPool );
	}

	@Override
	public RefRefHashMap< V, V > createVertexVertexMap( final int initialCapacity )
	{
		return new RefRefHashMap< V, V >( vertexPool, vertexPool, initialCapacity );
	}

	@Override
	public RefRefHashMap< V, E > createVertexEdgeMap()
	{
		return new RefRefHashMap< V, E >( vertexPool, edgePool );
	}

	@Override
	public RefRefHashMap< V, E > createVertexEdgeMap( final int initialCapacity )
	{
		return new RefRefHashMap< V, E >( vertexPool, edgePool, initialCapacity );
	}

	@Override
	public RefRefHashMap< E, V > createEdgeVertexMap()
	{
		return new RefRefHashMap< E, V >( edgePool, vertexPool );
	}

	@Override
	public RefRefHashMap< E, V > createEdgeVertexMap( final int initialCapacity )
	{
		return new RefRefHashMap< E, V >( edgePool, vertexPool, initialCapacity );
	}

	@Override
	public RefIntHashMap< V > createVertexIntMap(final int noEntryValue )
	{
		return new RefIntHashMap< V >( vertexPool, noEntryValue );
	}

	@Override
	public RefIntHashMap< V > createVertexIntMap( final int noEntryValue, final int initialCapacity )
	{
		return new RefIntHashMap< V >( vertexPool, noEntryValue, initialCapacity );
	}

	@Override
	public RefIntHashMap< E > createEdgeIntMap(final int noEntryValue)
	{
		return new RefIntHashMap< E >( edgePool, noEntryValue );
	}

	@Override
	public RefIntHashMap< E > createEdgeIntMap( final int noEntryValue, final int initialCapacity )
	{
		return new RefIntHashMap< E >( edgePool, noEntryValue, initialCapacity );
	}

	@Override
	public IntRefMap< V > createIntVertexMap( final int noEntryKey )
	{
		return new IntRefHashMap< V >( vertexPool, noEntryKey );
	}

	@Override
	public IntRefMap< V > createIntVertexMap( final int noEntryKey, final int initialCapacity )
	{
		return new IntRefHashMap< V >( vertexPool, noEntryKey, initialCapacity );
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
