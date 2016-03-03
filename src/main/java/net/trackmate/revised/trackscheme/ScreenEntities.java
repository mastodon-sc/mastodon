package net.trackmate.revised.trackscheme;

import java.util.ArrayList;
import java.util.List;

import net.trackmate.graph.PoolObjectList;
import net.trackmate.graph.collection.RefList;
import net.trackmate.revised.trackscheme.ScreenEdge.ScreenEdgePool;
import net.trackmate.revised.trackscheme.ScreenVertex.ScreenVertexPool;
import net.trackmate.revised.trackscheme.ScreenVertexRange.ScreenVertexRangePool;

/**
 * A collection of layouted screen objects to paint. Comprises lists of
 * {@link ScreenVertex}, {@link ScreenEdge}, and {@link ScreenVertexRange}.
 *
 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
 */
public class ScreenEntities
{
	/**
	 * Initial capacity value to use when instantiating the screen pools.
	 */
	private static final int DEFAULT_CAPACITY = 1000;

	private final ScreenVertexPool vertexPool;

	private final ScreenEdgePool edgePool;

	private final ScreenVertexRangePool rangePool;

	private final PoolObjectList< ScreenVertex > vertices;

	private final PoolObjectList< ScreenEdge > edges;

	private final PoolObjectList< ScreenVertexRange > ranges;

	private final ArrayList< ScreenColumn > columns;

	/**
	 * transform used to generate these {@link ScreenEntities}
	 */
	private final ScreenTransform screenTransform;

	public ScreenEntities( final TrackSchemeGraph< ?, ? > graph )
	{
		this( graph, DEFAULT_CAPACITY );
	}

	public ScreenEntities( final TrackSchemeGraph< ?, ? > graph, final int initialCapacity )
	{
		vertexPool = new ScreenVertexPool( initialCapacity, graph.getVertexPool() );
		vertices = new PoolObjectList< ScreenVertex >( vertexPool, initialCapacity );
		edgePool = new ScreenEdgePool( initialCapacity );
		edges = new PoolObjectList< ScreenEdge >( edgePool, initialCapacity );
		rangePool = new ScreenVertexRangePool( initialCapacity );
		ranges = new PoolObjectList< ScreenVertexRange >( rangePool, initialCapacity );
		columns = new ArrayList<>( initialCapacity );
		screenTransform = new ScreenTransform();
	}

	public RefList< ScreenVertex > getVertices()
	{
		return vertices;
	}

	public RefList< ScreenEdge > getEdges()
	{
		return edges;
	}

	public RefList< ScreenVertexRange > getRanges()
	{
		return ranges;
	}

	public List< ScreenColumn > getColumns()
	{
		return columns;
	}

	public void getScreenTransform( final ScreenTransform t )
	{
		t.set( screenTransform );
	}

	ScreenVertexPool getVertexPool()
	{
		return vertexPool;
	}

	ScreenEdgePool getEdgePool()
	{
		return edgePool;
	}

	ScreenVertexRangePool getRangePool()
	{
		return rangePool;
	}

	ScreenTransform screenTransform()
	{
		return screenTransform;
	}

	public void clear()
	{
		vertexPool.clear();
		vertices.resetQuick();
		edgePool.clear();
		edges.resetQuick();
		rangePool.clear();
		ranges.resetQuick();
		columns.clear();
	}

	public void set( final ScreenEntities ent )
	{
		clear();

		final ScreenVertex vRef = vertexPool.createRef();
		for ( final ScreenVertex v : ent.getVertices() )
			vertices.add( vertexPool.create( vRef ).cloneFrom( v ) );
		vertexPool.releaseRef( vRef );

		final ScreenEdge eRef = edgePool.createRef();
		for ( final ScreenEdge e : ent.getEdges() )
			edges.add( edgePool.create( eRef ).cloneFrom( e ) );
		edgePool.releaseRef( eRef );

		final ScreenVertexRange rRef = rangePool.createRef();
		for ( final ScreenVertexRange r : ent.getRanges() )
			ranges.add( rangePool.create( rRef ).cloneFrom( r ) );
		rangePool.releaseRef( rRef );

		columns.addAll( ent.getColumns() );

		screenTransform().set( ent.screenTransform );
	}
}
