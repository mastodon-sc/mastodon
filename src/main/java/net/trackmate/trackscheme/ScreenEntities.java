package net.trackmate.trackscheme;

import java.util.ArrayList;

import net.trackmate.trackscheme.ScreenEdge.ScreenEdgePool;
import net.trackmate.trackscheme.ScreenVertex.ScreenVertexPool;

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

	private final ScreenVertexList screenVertices;

	private final ScreenEdgeList screenEdges;

	private final ArrayList< ScreenVertexRange > vertexRanges;

	public ScreenEntities( final TrackSchemeGraph graph)
	{
		this( graph, DEFAULT_CAPACITY );
	}

	public ScreenEntities( final TrackSchemeGraph graph, final int initialCapacity )
	{
		screenVertexPool = new ScreenVertex.ScreenVertexPool( initialCapacity, graph.getVertexPool() );
		screenVertices = new ScreenVertexList( screenVertexPool, initialCapacity );
		screenEdgePool = new ScreenEdge.ScreenEdgePool( initialCapacity );
		screenEdges = new ScreenEdgeList( screenEdgePool, initialCapacity );
		vertexRanges = new ArrayList< ScreenVertexRange >( initialCapacity );
	}

	public ScreenVertexList getVertices()
	{
		return screenVertices;
	}

	public ScreenEdgeList getEdges()
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
