package net.trackmate.graph;

import java.util.ArrayList;

import net.trackmate.graph.listenable.GraphListener;
import net.trackmate.graph.listenable.ListenableGraph;
import net.trackmate.graph.mempool.MappedElement;

public class ListenableGraphImp<
		VP extends AbstractVertexPool< V, E, T >,
		EP extends AbstractEdgePool< E, V, T >,
		V extends AbstractVertex< V, E, T >,
		E extends AbstractEdge< E, V, T >,
		T extends MappedElement >
	extends GraphImp< VP, EP, V, E, T >
	implements ListenableGraph< V, E >
{
	public static <
		VP extends AbstractVertexPool< V, E, T >,
		EP extends AbstractEdgePool< E, V, T >,
		V extends AbstractVertex< V, E, T >,
		E extends AbstractEdge< E, V, T >,
		T extends MappedElement >
	ListenableGraphImp< VP, EP, V, E, T > create( final VP vertexPool, final EP edgePool )
	{
		return new ListenableGraphImp< VP, EP, V, E, T >( vertexPool, edgePool );
	}

	public static <
		VP extends AbstractVertexPool< V, E, T >,
		EP extends AbstractEdgePool< E, V, T >,
		V extends AbstractVertex< V, E, T >,
		E extends AbstractEdge< E, V, T >,
		T extends MappedElement >
	ListenableGraphImp< VP, EP, V, E, T > create( final EP edgePool )
	{
		return new ListenableGraphImp< VP, EP, V, E, T >( edgePool );
	}

	protected final ArrayList< GraphListener< V, E > > listeners;

	public ListenableGraphImp( final VP vertexPool, final EP edgePool )
	{
		super( vertexPool, edgePool );
		listeners = new ArrayList< GraphListener<V,E> >();
	}

	@SuppressWarnings( "unchecked" )
	public ListenableGraphImp( final EP edgePool )
	{
		super( edgePool );
		listeners = new ArrayList< GraphListener<V,E> >();
	}

	@Override
	public V addVertex()
	{
		final V v = vertexPool.create( vertexRef() );
		for ( final GraphListener< V, E > listener : listeners )
			listener.vertexAdded( v );
		return v;
	}

	@Override
	public V addVertex( final V vertex )
	{
		vertexPool.create( vertex );
		for ( final GraphListener< V, E > listener : listeners )
			listener.vertexAdded( vertex );
		return vertex;
	}

	@Override
	public E addEdge( final V source, final V target )
	{
		final E edge = edgePool.addEdge( source, target, edgeRef() );
		for ( final GraphListener< V, E > listener : listeners )
			listener.edgeAdded( edge );
		return edge;
	}

	@Override
	public E addEdge( final V source, final V target, final E edge )
	{
		edgePool.addEdge( source, target, edge );
		for ( final GraphListener< V, E > listener : listeners )
			listener.edgeAdded( edge );
		return edge;
	}

	@Override
	public void remove( final V vertex )
	{
		for ( final E edge : vertex.edges() )
			for ( final GraphListener< V, E > listener : listeners )
				listener.edgeRemoved( edge );
		for ( final GraphListener< V, E > listener : listeners )
			listener.vertexRemoved( vertex );
		vertexPool.delete( vertex );
	}

	@Override
	public void remove( final E edge )
	{
		for ( final GraphListener< V, E > listener : listeners )
			listener.edgeRemoved( edge );
		edgePool.delete( edge );
	}

	@Override
	public void removeAllLinkedEdges( final V vertex )
	{
		for ( final E edge : vertex.edges() )
			for ( final GraphListener< V, E > listener : listeners )
				listener.edgeRemoved( edge );
		edgePool.deleteAllLinkedEdges( vertex );
	}

	@Override
	public boolean addGraphListener( final GraphListener< V, E > listener )
	{
		if ( ! listeners.contains( listener ) )
		{
			listeners.add( listener );
			return true;
		}
		return false;
	}

	@Override
	public boolean removeGraphListener( final GraphListener< V, E > listener )
	{
		return listeners.remove( listener );
	}
}
