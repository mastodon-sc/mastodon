package org.mastodon.model.branch;

import org.mastodon.graph.Edge;
import org.mastodon.graph.GraphIdBimap;
import org.mastodon.graph.ReadOnlyGraph;
import org.mastodon.graph.Vertex;
import org.mastodon.graph.branch.BranchGraph;

/**
 * Base class for classes that adapt a model component of a core graph to a
 * branch graph.
 * 
 * @author Jean-Yves Tinevez
 */
public abstract class AbstractBranchGraphAdapter< 
	V extends Vertex< E >, 
	E extends Edge< V >, 
	BV extends Vertex< BE >, 
	BE extends Edge< BV > >
{

	protected final BranchGraph< BV, BE, V, E > branchGraph;

	protected final ReadOnlyGraph< V, E > graph;

	protected final GraphIdBimap< V, E > idmap;

	private final E eref;

	private final V vref;

	protected AbstractBranchGraphAdapter( 
			final BranchGraph< BV, BE, V, E > branchGraph,
			final ReadOnlyGraph< V, E > graph,
			final GraphIdBimap< V, E > idmap )
	{
		this.branchGraph = branchGraph;
		this.graph = graph;
		this.idmap = idmap;
		this.eref = graph.edgeRef();
		this.vref = graph.vertexRef();
	}
	

	protected final boolean isValid( final E e )
	{
		final int id = idmap.getEdgeId( e );
		return idmap.getEdgeIfExists( id, eref ) != null;
	}

	protected final boolean isValid( final V v )
	{
		final int id = idmap.getVertexId( v );
		return idmap.getVertexIfExists( id, vref ) != null;
	}
}
