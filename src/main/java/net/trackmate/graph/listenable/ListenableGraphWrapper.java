package net.trackmate.graph.listenable;

import java.util.Iterator;
import java.util.Set;
import java.util.WeakHashMap;

import net.trackmate.graph.Edge;
import net.trackmate.graph.Graph;
import net.trackmate.graph.Vertex;
import net.trackmate.graph.collection.CollectionUtils;
import net.trackmate.graph.collection.CollectionUtils.CollectionCreator;
import net.trackmate.graph.collection.RefDeque;
import net.trackmate.graph.collection.RefIntMap;
import net.trackmate.graph.collection.RefList;
import net.trackmate.graph.collection.RefObjectMap;
import net.trackmate.graph.collection.RefRefMap;
import net.trackmate.graph.collection.RefSet;
import net.trackmate.graph.collection.RefStack;

/**
 * Transaction model inspired by JGraphX.
 *
 * @author Jean-Yves Tinevez
 *
 * @param <V>
 *            the type of the vertex of the graph wrapped.
 * @param <E>
 *            the type of the edge of the graph wrapper.
 * @param <G>
 *            the type of the graph wrapped.
 */
public class ListenableGraphWrapper< V extends Vertex< E >, E extends Edge< V >, G extends Graph< V, E > > implements ListenableGraph< V, E >, Graph< V, E >, CollectionCreator< V, E >
{
	/*
	 * STATIC ACCESSOR
	 */

	public static final < V extends Vertex< E >, E extends Edge< V >, G extends Graph< V, E > > ListenableGraphWrapper< V, E, G > wrap( G graph )
	{
		return new ListenableGraphWrapper< V, E, G >( graph );
	}

	/*
	 * FIELDS
	 */

	private final G graph;

	/**
	 * Counter for the depth of nested transactions. Each call to beginUpdate
	 * increments this counter and each call to endUpdate decrements it. When
	 * the counter reaches 0, the transaction is closed and the respective
	 * events are fired. Initial value is 0.
	 */
	private transient int updateLevel = 0;

	private transient boolean endingUpdate = false;

	private GraphChangeEvent< V, E > currentEdit;

	private final WeakHashMap< GraphListener< V, E >, Boolean > listeners;

	/*
	 * CONSTRUCTOR
	 */

	public ListenableGraphWrapper( final G graph )
	{
		this.graph = graph;
		this.currentEdit = new GraphChangeEvent< V, E >( graph );
		this.listeners = new WeakHashMap< GraphListener< V, E >, Boolean >();
	}

	/*
	 * METHODS EVENT HANDLING
	 */

	public void beginUpdate()
	{
		updateLevel++;
	}

	public void endUpdate()
	{
		updateLevel--;

		if ( !endingUpdate )
		{
			endingUpdate = updateLevel == 0;
			try
			{
				if ( endingUpdate && !currentEdit.isEmpty() )
				{
					fireEvent();
					currentEdit = new GraphChangeEvent< V, E >( graph );
				}
			}
			finally
			{
				endingUpdate = false;
			}
		}
	}


	private void fireEvent()
	{
		for ( final GraphListener< V, E > listener : listeners.keySet() )
		{
			listener.graphChanged( currentEdit );
		}
	}

	/*
	 * EXPOSE WRAPPED GRAPH. With the right type.
	 */

	public G getGraph()
	{
		return graph;
	}

	/*
	 * LISTENER MANAGEMENT
	 */

	@Override
	public boolean addGraphListener( final GraphListener< V, E > listener )
	{
		return null != listeners.put( listener, Boolean.TRUE );
	}

	@Override
	public boolean removeGraphListener( final GraphListener< V, E > listener )
	{
		return null != listeners.remove( listener );
	}

	@Override
	public Set< GraphListener< V, E >> getGraphListeners()
	{
		return listeners.keySet();
	}

	/*
	 * GRAPH METHODS TO LISTEN TO
	 */

	@Override
	public V addVertex()
	{
		final V v = graph.addVertex();
		currentEdit.vertexAdded( v );
		return v;
	}

	@Override
	public V addVertex( final V vertex )
	{
		final V v = graph.addVertex( vertex );
		currentEdit.vertexAdded( v );
		return v;
	}

	@Override
	public E addEdge( final V source, final V target )
	{
		final E e = graph.addEdge( source, target );
		currentEdit.edgeAdded( e );
		return e;
	}

	@Override
	public E addEdge( final V source, final V target, final E edge )
	{
		final E e = graph.addEdge( source, target, edge );
		currentEdit.edgeAdded( e );
		return e;
	}

	@Override
	public void remove( final V vertex )
	{
		for ( final E edge : vertex.edges() )
		{
			currentEdit.edgeRemoved( edge );
		}
		currentEdit.vertexRemoved( vertex );
		graph.remove( vertex );
	}

	@Override
	public void remove( final E edge )
	{
		currentEdit.edgeRemoved( edge );
		graph.remove( edge );
	}

	@Override
	public void removeAllLinkedEdges( final V vertex )
	{
		for ( final E edge : vertex.edges() )
		{
			currentEdit.edgeRemoved( edge );
		}
		graph.removeAllLinkedEdges( vertex );
	}

	/*
	 * OTHER GRAPH METHODS
	 */

	@Override
	public E getEdge( final V source, final V target )
	{
		return graph.getEdge( source, target );
	}

	@Override
	public E getEdge( final V source, final V target, final E edge )
	{
		return graph.getEdge( source, target, edge );
	}

	@Override
	public V vertexRef()
	{
		return graph.vertexRef();
	}

	@Override
	public E edgeRef()
	{
		return graph.edgeRef();
	}

	@Override
	public void releaseRef( final V ref )
	{
		graph.releaseRef( ref );
	}

	@Override
	public void releaseRef( final E ref )
	{
		graph.releaseRef( ref );
	}

	@Override
	public void releaseRef( final V... refs )
	{
		graph.releaseRef( refs );
	}

	@Override
	public void releaseRef( final E... refs )
	{
		graph.releaseRef( refs );
	}

	@Override
	public Iterator< V > vertexIterator()
	{
		return graph.vertexIterator();
	}

	@Override
	public Iterator< E > edgeIterator()
	{
		return graph.edgeIterator();
	}

	/*
	 * COLLECTION METHODS
	 */

	@Override
	public RefSet< V > createVertexSet()
	{
		return CollectionUtils.createVertexSet( graph );
	}

	@Override
	public RefSet< V > createVertexSet( final int initialCapacity )
	{
		return CollectionUtils.createVertexSet( graph, initialCapacity );
	}

	@Override
	public RefSet< E > createEdgeSet()
	{
		return CollectionUtils.createEdgeSet( graph );
	}

	@Override
	public RefSet< E > createEdgeSet( final int initialCapacity )
	{
		return CollectionUtils.createEdgeSet( graph, initialCapacity );
	}

	@Override
	public RefList< V > createVertexList()
	{
		return CollectionUtils.createVertexList( graph );
	}

	@Override
	public RefList< V > createVertexList( final int initialCapacity )
	{
		return CollectionUtils.createVertexList( graph, initialCapacity );
	}

	@Override
	public RefList< E > createEdgeList()
	{
		return CollectionUtils.createEdgeList( graph );
	}

	@Override
	public RefList< E > createEdgeList( final int initialCapacity )
	{
		return CollectionUtils.createEdgeList( graph, initialCapacity );
	}

	@Override
	public RefDeque< V > createVertexDeque()
	{
		return CollectionUtils.createVertexDeque( graph );
	}

	@Override
	public RefDeque< V > createVertexDeque( final int initialCapacity )
	{
		return CollectionUtils.createVertexDeque( graph, initialCapacity );
	}

	@Override
	public RefDeque< E > createEdgeDeque()
	{
		return CollectionUtils.createEdgeDeque( graph );
	}

	@Override
	public RefDeque< E > createEdgeDeque( final int initialCapacity )
	{
		return CollectionUtils.createEdgeDeque( graph, initialCapacity );
	}

	@Override
	public RefStack< V > createVertexStack()
	{
		return CollectionUtils.createVertexStack( graph );
	}

	@Override
	public RefStack< V > createVertexStack( final int initialCapacity )
	{
		return CollectionUtils.createVertexStack( graph, initialCapacity );
	}

	@Override
	public RefStack< E > createEdgeStack()
	{
		return CollectionUtils.createEdgeStack( graph );
	}

	@Override
	public RefStack< E > createEdgeStack( final int initialCapacity )
	{
		return CollectionUtils.createEdgeStack( graph, initialCapacity );
	}

	@Override
	public < O > RefObjectMap< V, O > createVertexObjectMap( final Class< ? extends O > valueClass )
	{
		return CollectionUtils.createVertexObjectMap( graph, valueClass );
	}

	@Override
	public < O > RefObjectMap< E, O > createEdgeObjectMap( final Class< ? extends O > valueClass )
	{
		return CollectionUtils.createEdgeObjectMap( graph, valueClass );
	}

	@Override
	public RefRefMap< V, E > createVertexEdgeMap()
	{
		return CollectionUtils.createVertexEdgeMap( graph );
	}

	@Override
	public RefRefMap< V, E > createVertexEdgeMap( final int initialCapacity )
	{
		return CollectionUtils.createVertexEdgeMap( graph, initialCapacity );
	}

	@Override
	public RefRefMap< E, V > createEdgeVertexMap()
	{
		return CollectionUtils.createEdgeVertexMap( graph );
	}

	@Override
	public RefRefMap< E, V > createEdgeVertexMap( final int initialCapacity )
	{
		return CollectionUtils.createEdgeVertexMap( graph, initialCapacity );
	}

	@Override
	public RefRefMap< V, V > createVertexVertexMap()
	{
		return CollectionUtils.createVertexVertexMap( graph );
	}

	@Override
	public RefRefMap< V, V > createVertexVertexMap( final int initialCapacity )
	{
		return CollectionUtils.createVertexVertexMap( graph, initialCapacity );
	}

	@Override
	public RefRefMap< E, E > createEdgeEdgeMap()
	{
		return CollectionUtils.createEdgeEdgeMap( graph );
	}

	@Override
	public RefRefMap< E, E > createEdgeEdgeMap( final int initialCapacity )
	{
		return CollectionUtils.createEdgeEdgeMap( graph, initialCapacity );
	}

	protected static < O > Iterator< O > safeIterator( final Iterator< O > iterator )
	{
		return CollectionUtils.safeIterator( iterator );
	}

	@Override
	public RefIntMap< V > createVertexIntMap( final int noEntryValue )
	{
		return CollectionUtils.createVertexIntMap( graph, noEntryValue );
	}

	@Override
	public RefIntMap< V > createVertexIntMap( final int noEntryValue, final int initialCapacity )
	{
		return CollectionUtils.createVertexIntMap( graph, noEntryValue, initialCapacity );
	}

	@Override
	public RefIntMap< E > createEdgeIntMap( final int noEntryValue )
	{
		return CollectionUtils.createEdgeIntMap( graph, noEntryValue );
	}

	@Override
	public RefIntMap< E > createEdgeIntMap( final int noEntryValue, final int initialCapacity )
	{
		return CollectionUtils.createEdgeIntMap( graph, noEntryValue, initialCapacity );
	}
}
