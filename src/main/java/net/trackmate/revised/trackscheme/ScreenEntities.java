package net.trackmate.revised.trackscheme;

import net.trackmate.graph.PoolObjectList;
import net.trackmate.graph.collection.RefList;
import net.trackmate.revised.trackscheme.ScreenEdge.ScreenEdgePool;
import net.trackmate.revised.trackscheme.ScreenVertex.ScreenVertexPool;
import net.trackmate.revised.trackscheme.ScreenVertexRange.ScreenVertexRangePool;

/**
 * A collection of layouted screen objects to paint. Comprises lists of
 * {@link ScreenVertex}, {@link ScreenEdge}, and {@link ScreenVertexRange}.
 *
 * @author Tobias Pietzsch <tobias.pietzsch@gmail.com>
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

	/**
	 * transform used to generate these {@link ScreenEntities}
	 */
	private final ScreenTransform screenTransform;

	public ScreenEntities( final TrackSchemeGraph< ?, ? > graph)
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
	}
}
