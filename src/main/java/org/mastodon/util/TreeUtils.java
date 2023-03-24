package org.mastodon.util;

import org.mastodon.collection.RefCollections;
import org.mastodon.collection.RefList;
import org.mastodon.collection.RefSet;
import org.mastodon.graph.Graph;
import org.mastodon.graph.Vertex;

public class TreeUtils
{

	private TreeUtils()
	{
		// prevent instantiation of utility class
	}

	/**
	 * This method finds a subset of the given {@code selectedVertices} that
	 * contains only the roots of the subtrees that are selected. The order
	 * of the returned list follows the order of the {@code roots} list and
	 * the order of the outgoing edges of the graphs vertices.
	 * <p>
	 * Example:
	 * <pre>
	 *   A                B
	 *  / \             /   \
	 * a1  a2          b1    b2
	 *     |          / \   / \
	 *     a3        b3 b4 b5 b6
	 *    / \
	 *   a4 a5
	 * </pre>
	 *
	 * If {@code selectedVertices} contains: {@code {a2, a4, a5, b1, b3, b4, b6}},
	 * then the returned list will be: {@code [a2, b1, b6]}.
	 */
	public static < V extends Vertex< ? > > RefList< V > findSelectedSubtreeRoots(
			final Graph< V, ? > graph,
			final RefList< V > roots,
			final RefSet< V > selectedVertices )
	{
		// Note: The method iterates over the graph in depth-first order starting from each root.
		// Whenever the iteration hits a node that is in the selectedVertices set, it adds
		// it to the returned list, and then skips all its descendants. This ensures that
		// the returned list contains only the root nodes of the selected subtrees.

		// Note: The DepthFirstIteration class does not detect loops in the graph. It
		// can therefore get stuck in an infinite loop. The code below keeps track of the
		// nodes that have been visited, skipping them if they are visited again.

		final RefList< V > selectedSubtreeRoots = RefCollections.createRefList( graph.vertices() );
		final RefSet< V > visited = RefCollections.createRefSet( graph.vertices() );

		for ( final V realRoot : roots )
			for ( final DepthFirstIteration.Step< V > step : DepthFirstIteration.forRoot( graph, realRoot ) )
			{
				if ( step.isSecondVisit() )
					continue;

				final V node = step.node();

				// loop detection
				if ( !visited.add( node ) )  // The depth first iteration enters a node for the second time. -> there's a loop.
				{
					step.truncate(); // Don't visit the child nodes
					continue; // and skip this node.
				}

				if ( selectedVertices.contains( node ) )
				{
					selectedSubtreeRoots.add( node );
					step.truncate(); // don't visit the child nodes
				}
			}

		return selectedSubtreeRoots;
	}
}
