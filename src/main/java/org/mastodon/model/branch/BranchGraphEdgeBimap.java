package org.mastodon.model.branch;

import org.mastodon.adapter.RefBimap;
import org.mastodon.graph.Edge;
import org.mastodon.graph.ReadOnlyGraph;
import org.mastodon.graph.branch.BranchGraph;

/**
 * Maps a graph vertices to the vertices in the branch graph they are linked to.
 * 
 * @author Jean-Yves Tinevez
 *
 * @param <E>
 *            the type of vertices in the core graph.
 * @param <BE>
 *            the type of vertices in the branch graph.
 */
public class BranchGraphEdgeBimap< E extends Edge< ? >, BE extends Edge< ? > > implements RefBimap<E, BE>
{

	private final BranchGraph< ?, BE, ?, E > branchGraph;

	private final ReadOnlyGraph< ?, E > graph;

	public BranchGraphEdgeBimap( final BranchGraph< ?, BE, ?, E > branchGraph, final ReadOnlyGraph< ?, E > graph )
	{
		this.branchGraph = branchGraph;
		this.graph = graph;
	}

	@Override
	public E getLeft( final BE right )
	{
		return right == null ? null : branchGraph.getLinkedEdge( right, reusableLeftRef( right ) );
	}

	@Override
	public BE getRight( final E left, final BE ref )
	{
		return left == null ? null : branchGraph.getBranchEdge( left, ref );
	}

	@Override
	public E reusableLeftRef( final BE ref )
	{
		return graph.edgeRef();
	}

	@Override
	public BE reusableRightRef()
	{
		return branchGraph.edgeRef();
	}

	@Override
	public void releaseRef( final BE ref )
	{
		branchGraph.releaseRef( ref );
	}
}
