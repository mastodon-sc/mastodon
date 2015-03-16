package net.trackmate.graph.algorithm;

import java.util.Iterator;

import net.trackmate.graph.Edge;
import net.trackmate.graph.Graph;
import net.trackmate.graph.Vertex;
import net.trackmate.graph.collection.CollectionUtils;
import net.trackmate.graph.collection.RefDeque;
import net.trackmate.graph.collection.RefList;
import net.trackmate.graph.collection.RefSet;
import net.trackmate.graph.collection.RefStack;

/**
 * Base class for graph algorithms. Provides helper functions aimed at
 * minimizing the pain of dealing with graphs that may or may not be ref based.
 *
 * @param <V>
 *            the {@link Vertex} type of the {@link Graph}.
 * @param <E>
 *            the {@link Edge} type of the {@link Graph}.
 *
 * @author Tobias Pietzsch <tobias.pietzsch@gmail.com>
 */
public abstract class AbstractGraphAlgorithm< V extends Vertex< E >, E extends Edge< V > >
{
	protected final Graph< V, E > graph;

	private final Assigner< V > vertexAssigner;

	private final Assigner< E > edgeAssigner;

	public AbstractGraphAlgorithm( final Graph< V, E > graph )
	{
		this.graph = graph;
		final V v = graph.vertexRef();
		vertexAssigner = Assigner.getFor( v );
		graph.releaseRef( v );
		final E e = graph.edgeRef();
		edgeAssigner = Assigner.getFor( e );
		graph.releaseRef( e );
	}

	protected V assign( final V value, final V target )
	{
		return vertexAssigner.assign( value, target );
	}

	protected E assign( final E value, final E target )
	{
		return edgeAssigner.assign( value, target );
	}

	protected V vertexRef()
	{
		return graph.vertexRef();
	}

	protected E edgeRef()
	{
		return graph.edgeRef();
	}

	protected void releaseRef( final V ref )
	{
		graph.releaseRef( ref );
	}

	protected void releaseRef( final E ref )
	{
		graph.releaseRef( ref );
	}

	protected void releaseRef( final V ... refs )
	{
		graph.releaseRef( refs );
	}

	protected void releaseRef( final E ... refs )
	{
		graph.releaseRef( refs );
	}

	protected RefSet< V > createVertexSet()
	{
		return CollectionUtils.createVertexSet( graph );
	}

	protected RefSet< V > createVertexSet( final int initialCapacity )
	{
		return CollectionUtils.createVertexSet( graph, initialCapacity );
	}

	protected RefSet< E > createEdgeSet()
	{
		return CollectionUtils.createEdgeSet( graph );
	}

	protected RefSet< E > createEdgeSet( final int initialCapacity )
	{
		return CollectionUtils.createEdgeSet( graph, initialCapacity );
	}

	protected RefList< V > createVertexList()
	{
		return CollectionUtils.createVertexList( graph );
	}

	protected RefList< V > createVertexList( final int initialCapacity )
	{
		return CollectionUtils.createVertexList( graph, initialCapacity );
	}

	protected RefList< E > createEdgeList()
	{
		return CollectionUtils.createEdgeList( graph );
	}

	protected RefList< E > createEdgeList( final int initialCapacity )
	{
		return CollectionUtils.createEdgeList( graph, initialCapacity );
	}

	protected RefDeque< V > createVertexDeque()
	{
		return CollectionUtils.createVertexDeque( graph );
	}

	protected RefDeque< V > createVertexDeque( final int initialCapacity )
	{
		return CollectionUtils.createVertexDeque( graph, initialCapacity );
	}

	protected RefDeque< E > createEdgeDeque()
	{
		return CollectionUtils.createEdgeDeque( graph );
	}

	protected RefDeque< E > createEdgeDeque( final int initialCapacity )
	{
		return CollectionUtils.createEdgeDeque( graph, initialCapacity );
	}

	protected RefStack< V > createVertexStack()
	{
		return CollectionUtils.createVertexStack( graph );
	}

	protected RefStack< V > createVertexStack( final int initialCapacity )
	{
		return CollectionUtils.createVertexStack( graph, initialCapacity );
	}

	protected RefStack< E > createEdgeStack()
	{
		return CollectionUtils.createEdgeStack( graph );
	}

	protected RefStack< E > createEdgeStack( final int initialCapacity )
	{
		return CollectionUtils.createEdgeStack( graph, initialCapacity );
	}

	protected static < O > Iterator< O > safeIterator( final Iterator< O > iterator )
	{
		return CollectionUtils.safeIterator( iterator );
	}
}
