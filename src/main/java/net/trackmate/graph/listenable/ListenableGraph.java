package net.trackmate.graph.listenable;

import java.util.Iterator;
import java.util.Set;
import java.util.WeakHashMap;

import net.trackmate.graph.Edge;
import net.trackmate.graph.Graph;
import net.trackmate.graph.Vertex;

/**
 * Transaction model inspired by JGraphX.
 * 
 * @author tinevez
 *
 * @param <V>
 * @param <E>
 */
public class ListenableGraph< V extends Vertex< E >, E extends Edge< V > > implements Graph< V, E >
{
	private final Graph< V, E > graph;

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

	public ListenableGraph( final Graph< V, E > graph )
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
	 * LISTENER MANAGEMENT
	 */

	public boolean addGraphListener( final GraphListener< V, E > listener )
	{
		return null != listeners.put( listener, Boolean.TRUE );
	}

	public boolean removeGraphListener( final GraphListener< V, E > listener )
	{
		return null != listeners.remove( listener );
	}

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

}
