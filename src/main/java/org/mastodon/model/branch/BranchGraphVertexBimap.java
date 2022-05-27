package org.mastodon.model.branch;

import org.mastodon.adapter.RefBimap;
import org.mastodon.graph.ReadOnlyGraph;
import org.mastodon.graph.Vertex;
import org.mastodon.graph.branch.BranchGraph;

/**
 * Maps a graph vertices to the vertices in the branch graph they are linked to.
 * 
 * @author Jean-Yves Tinevez
 *
 * @param <V>
 *            the type of vertices in the core graph.
 * @param <BV>
 *            the type of vertices in the branch graph.
 */
public class BranchGraphVertexBimap< V extends Vertex< ? >, BV extends Vertex< ? > > implements RefBimap< V, BV >
{

	private final BranchGraph< BV, ?, V, ? > branchGraph;

	private final ReadOnlyGraph< V, ? > graph;

	public BranchGraphVertexBimap( final BranchGraph< BV, ?, V, ? > branchGraph, final ReadOnlyGraph< V, ? > graph )
	{
		this.branchGraph = branchGraph;
		this.graph = graph;
	}

	@Override
	public V getLeft( final BV right )
	{
		return right == null ? null : branchGraph.getLinkedVertex( right, reusableLeftRef( right ) );
	}

	@Override
	public BV getRight( final V left, final BV ref )
	{
		return left == null ? null : branchGraph.getBranchVertex( left, ref );
	}

	@Override
	public V reusableLeftRef( final BV ref )
	{
		return graph.vertexRef();
	}

	@Override
	public BV reusableRightRef()
	{
		return branchGraph.vertexRef();
	}

	@Override
	public void releaseRef( final BV ref )
	{
		branchGraph.releaseRef( ref );
	}
}
