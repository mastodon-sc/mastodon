package org.mastodon.graph;

import org.mastodon.RefPool;

/**
 * Bidirectional mappings between integer IDs and vertices and integer IDs
 * and edges.
 *
 * @param <V>
 *            the {@link Vertex} type.
 * @param <E>
 *            the {@link Edge} type.
 *
 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
 */
// TOOD simplify and rename? This is just a pair of RefPools. "GraphRefPools", "GraphPools"? remove completely?
public class GraphIdBimap< V, E >
{
	private final RefPool< V > vertexBimap;
	private final RefPool< E > edgeBimap;

	public GraphIdBimap( final RefPool< V > vertexBimap, final RefPool< E > edgeBimap )
	{
		this.vertexBimap = vertexBimap;
		this.edgeBimap = edgeBimap;
	}

	public int getVertexId( final V v )
	{
		return vertexBimap.getId( v );
	}

	public V getVertex( final int id, final V ref )
	{
		return vertexBimap.getObject( id, ref );
	}

	public int getEdgeId( final E e )
	{
		return edgeBimap.getId( e );
	}

	public E getEdge( final int id, final E ref )
	{
		return edgeBimap.getObject( id, ref );
	}

	public RefPool< V > vertexIdBimap()
	{
		return vertexBimap;
	}

	public RefPool< E > edgeIdBimap()
	{
		return edgeBimap;
	}
}
