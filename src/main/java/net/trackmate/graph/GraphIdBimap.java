package net.trackmate.graph;


/**
 * Bidirectional mappings between integer IDs and vertices and integer IDs
 * and edges.
 *
 * <p>
 * TODO: in which package should this be?
 *
 * @param <V>
 *            the {@link Vertex} type.
 * @param <E>
 *            the {@link Edge} type.
 *
 * @author Tobias Pietzsch <tobias.pietzsch@gmail.com>
 */
public class GraphIdBimap< V, E >
{
	private final IdBimap< V > vertexBimap;
	private final IdBimap< E > edgeBimap;

	public GraphIdBimap( final IdBimap< V > vertexBimap, final IdBimap< E > edgeBimap )
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

	public IdBimap< V > vertexIdBimap()
	{
		return vertexBimap;
	}

	public IdBimap< E > edgeIdBimap()
	{
		return edgeBimap;
	}
}