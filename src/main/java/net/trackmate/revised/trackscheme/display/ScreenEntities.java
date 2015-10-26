package net.trackmate.revised.trackscheme.display;

import java.util.ArrayList;

import net.trackmate.graph.PoolObjectList;
import net.trackmate.graph.collection.RefList;
import net.trackmate.revised.trackscheme.TrackSchemeGraph;
import net.trackmate.revised.trackscheme.display.ScreenEdge.ScreenEdgePool;
import net.trackmate.revised.trackscheme.display.ScreenVertex.ScreenVertexPool;

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

	private final ScreenVertexPool screenVertexPool;

	private final ScreenEdgePool screenEdgePool;

	private final PoolObjectList< ScreenVertex > screenVertices;

	private final PoolObjectList< ScreenEdge > screenEdges;

	private final ArrayList< ScreenVertexRange > vertexRanges;

	public ScreenEntities( final TrackSchemeGraph< ?, ? > graph)
	{
		this( graph, DEFAULT_CAPACITY );
	}

	public ScreenEntities( final TrackSchemeGraph< ?, ? > graph, final int initialCapacity )
	{
		screenVertexPool = new ScreenVertex.ScreenVertexPool( initialCapacity, graph.getVertexPool() );
		screenVertices = new PoolObjectList< ScreenVertex >( screenVertexPool, initialCapacity );
		screenEdgePool = new ScreenEdge.ScreenEdgePool( initialCapacity );
		screenEdges = new PoolObjectList< ScreenEdge >( screenEdgePool, initialCapacity );
		vertexRanges = new ArrayList< ScreenVertexRange >( initialCapacity );
	}

	public RefList< ScreenVertex > getVertices()
	{
		return screenVertices;
	}

	public RefList< ScreenEdge > getEdges()
	{
		return screenEdges;
	}

	public ArrayList< ScreenVertexRange > getVertexRanges()
	{
		return vertexRanges;
	}

	ScreenVertexPool getVertexPool()
	{
		return screenVertexPool;
	}

	ScreenEdgePool getEdgePool()
	{
		return screenEdgePool;
	}

	public void clear()
	{
		screenVertexPool.clear();
		screenVertices.resetQuick();
		screenEdgePool.clear();
		screenEdges.resetQuick();
		vertexRanges.clear();
	}
}
