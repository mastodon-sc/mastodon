package org.mastodon.mamut.model.branch;

import org.mastodon.graph.branch_v2.BranchGraphV2Imp;
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
public class ModelBranchGraph extends BranchGraphV2Imp< Spot, Link, BranchSpot, BranchLink, BranchSpotPool, BranchLinkPool, ByteMappedElement >
{

	public ModelBranchGraph( final ModelGraph graph )
	{
		super( graph, new BranchLinkPool( 1024, new BranchSpotPool( 1024, graph.vertices().getRefPool() ) ) );
	}

	public ModelBranchGraph( final ModelGraph graph, final int initialCapacity )
	{
		super( graph, new BranchLinkPool( initialCapacity, new BranchSpotPool( initialCapacity, graph.vertices().getRefPool() ) ) );
	}

	@Override
	public BranchSpot init( final BranchSpot branchVertex, final Spot branchStart, final Spot branchEnd )
	{
		return branchVertex.init( branchStart, branchEnd );
	}

	@Override
	public BranchLink init( final BranchLink branchEdge, final Link edge )
	{
		return branchEdge.init();
	}
}
