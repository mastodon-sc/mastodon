package org.mastodon.mamut.model.branch;

import org.mastodon.graph.branch.BranchGraphImp;
import org.mastodon.mamut.model.Link;
import org.mastodon.mamut.model.ModelGraph;
import org.mastodon.mamut.model.Spot;
import org.mastodon.pool.ByteMappedElement;
import org.mastodon.spatial.HasTimepoint;

import net.imglib2.RealLocalizable;

/**
 * A branch-graph specific for {@link ModelGraph}, whose vertices implements the
 * {@link RealLocalizable} and {@link HasTimepoint} interfaces, exposing the
 * {@link Spot} they are linked to.
 *
 * @author Jean-Yves Tinevez.
 *
 */
public class ModelBranchGraph extends BranchGraphImp< Spot, Link, BranchVertex, BranchEdge, BranchVertexPool, BranchEdgePool, ByteMappedElement >
{

	public ModelBranchGraph( final ModelGraph graph )
	{
		super( graph, new BranchEdgePool( 1024, new BranchVertexPool( 1024, graph.vertices().getRefPool() ) ) );
	}

	public ModelBranchGraph( final ModelGraph graph, final int initialCapacity )
	{
		super( graph, new BranchEdgePool( initialCapacity, new BranchVertexPool( initialCapacity, graph.vertices().getRefPool() ) ) );
	}

	@Override
	public BranchVertex init( final BranchVertex bv, final Spot v )
	{
		return bv.init( v );
	}

	@Override
	public BranchEdge init( final BranchEdge be, final Link e )
	{
		return be.init();
	}
}
