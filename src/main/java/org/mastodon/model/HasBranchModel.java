package org.mastodon.model;

import org.mastodon.graph.Edge;
import org.mastodon.graph.Vertex;
import org.mastodon.graph.branch.BranchGraph;

public interface HasBranchModel<
		BG extends BranchGraph< BV, BE, ?, ? >,
		BV extends Vertex< BE >, 
		BE extends Edge< BV > >
{

	/**
	 * Returns a view of this model as a branch-graph model.
	 * <p>
	 * A branch-graph is a view of the core graph where linear chains of edges
	 * are grouped into single edges called branches. This view is dynamically
	 * updated as the core graph changes.
	 * 
	 * @return the branch-graph model.
	 */
	MastodonModel< BG, BV, BE > branchModel();

}
