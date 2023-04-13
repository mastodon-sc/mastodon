package org.mastodon.util;

import java.util.Collection;

import org.mastodon.collection.RefCollections;
import org.mastodon.collection.RefList;
import org.mastodon.collection.RefSet;
import org.mastodon.graph.Edge;
import org.mastodon.graph.Graph;
import org.mastodon.graph.Vertex;
import org.mastodon.graph.algorithm.traversal.InverseDepthFirstIterator;

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
		final RefList< V > selectedSubtreeRoots = RefCollections.createRefList( graph.vertices() );
		final RefSet< V > visitedNodes = RefCollections.createRefSet( graph.vertices() );

		for ( final V realRoot : roots )
			for ( final DepthFirstIteration.Step< V > step : DepthFirstIteration.forRoot( graph, realRoot ) )
				if ( ensureNoLoop( step, visitedNodes ) && !step.isSecondVisit() ) {

					final V node = step.node();
					if ( selectedVertices.contains( node ) )
					{
						selectedSubtreeRoots.add( node );
						step.truncate(); // don't visit the child nodes
					}
				}

		return selectedSubtreeRoots;
	}

	private static < V extends Vertex< ? > > boolean ensureNoLoop( DepthFirstIteration.Step< V > step, RefSet< V > visitedNodes )
	{
		if ( !step.isFirstVisit() )
			return true;

		boolean isLoop = !visitedNodes.add( step.node() ); // The depth first iteration enters a node for the second time. -> there's a loop.
		if ( isLoop )
			step.truncate(); // Break the loop by not visiting the child nodes.

		return !isLoop;
	}

	public static <V extends Vertex<E>, E extends Edge< V > > RefSet< V > findRealRoots( Graph< V, E > graph, Collection< V > selectedNodes )
	{
		final RefSet< V > visited = RefCollections.createRefSet( graph.vertices() );

		InverseDepthFirstIterator< V, E > it = new InverseDepthFirstIterator<>( graph );
		for( V node : selectedNodes )
		{
			it.reset( node );
			while ( it.hasNext() )
			{
				final V previousNode = it.next();
				boolean visitedBefore = ( !visited.add( previousNode ) );
				if ( visitedBefore )
					break;
			}
		}

		final RefSet< V > realRoots = RefCollections.createRefSet( graph.vertices() );
		for( V node : visited )
			if ( node.incomingEdges().isEmpty() )
				realRoots.add( node );
		return realRoots;
	}
}
