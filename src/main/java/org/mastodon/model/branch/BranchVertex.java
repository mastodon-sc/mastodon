package org.mastodon.model.branch;

/**
 * Interface that extends a vertex class wiwth methods specific to the vertices
 * of a branch graph.
 *
 * @author Jean-Yves Tinevez
 *
 */
public interface BranchVertex
{

	/**
	 * Gets the label of the first vertex in the branch represented by this
	 * vertex.
	 * <p>
	 * In the branch graph of Mastodon, a branch vertex represents the series of
	 * vertices in the core graph that are connected by edges and have no
	 * branching. The first vertex is the vertex at the beginning of this
	 * series.
	 *
	 * @return the label of the first vertex in the branch.
	 */
	String getFirstLabel();

	/**
	 * Gets the time point of the first vertex in the branch represented by this
	 * vertex.
	 * <p>
	 * In the branch graph of Mastodon, a branch vertex represents the series of
	 * vertices in the core graph that are connected by edges and have no
	 * branching. The first vertex is the vertex at the beginning of this
	 * series.
	 *
	 * @return the time point of the first vertex in the branch.
	 */
	int getFirstTimePoint();

}
