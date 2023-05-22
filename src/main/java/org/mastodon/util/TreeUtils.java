package org.mastodon.util;

import java.util.Collection;

import org.mastodon.collection.RefCollections;
import org.mastodon.collection.RefList;
import org.mastodon.collection.RefSet;
import org.mastodon.collection.RefStack;
import org.mastodon.graph.Edge;
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
	 * Example {@code graph}:
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

		for ( final V root : roots )
			for ( final DepthFirstIteration.Step< V > step : DepthFirstIteration.forRoot( graph, root ) )
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

	/**
	 * Returns those roots of the given {@code graph} that are predecessors of
	 * the given {@code nodes}. (A node is considered a root if it has no
	 * incoming edges.)
	 * <p>
	 * Example {@code graph}:
	 * <pre>
	 *   A                B
	 *  / \             /   \
	 * a1  a2          b1    b2
	 *     |          / \   / \
	 *     a3        b3 b4 b5 b6
	 *    / \
	 *   a4 a5
	 * </pre>
	 * If {@code node} contains: {@code {a2}},
	 * then the method returns a set {@code {A}}.
	 */
	public static <V extends Vertex<E>, E extends Edge< V > > RefSet< V > findRootsOfTheGivenNodes( Graph< V, E > graph, Collection< V > nodes )
	{
		return filterRoots( graph, computePredecessors( graph, nodes ) );
	}

	/**
	 * @return the set of predecessors of the given {@code nodes}. Please note
	 * that the set contains the given {@code nodes} as well.
	 */
	private static < V extends Vertex<E>, E extends Edge< V > > RefSet< V > computePredecessors( Graph< V, E > graph, Collection< V > nodes )
	{
		// The following code performs an inverse depth first search starting
		// from the given nodes. The set of visited nodes is returned.
		V ref = graph.vertexRef();
		V ref2 = graph.vertexRef();
		try
		{
			final RefSet< V > visited = RefCollections.createRefSet( graph.vertices() );
			visited.addAll( nodes );
			final RefStack< V > stack = RefCollections.createRefStack( graph.vertices() );
			stack.addAll( visited );
			while ( ! stack.isEmpty() ) {
				V node = stack.pop( ref );
				for ( E edge : node.incomingEdges() ) {
					V previousNode = edge.getSource( ref2 );
					boolean firstVisit = visited.add( previousNode );
					if ( firstVisit )
						stack.add( previousNode );
				}
			}
			return visited;
		}
		finally
		{
			graph.releaseRef( ref );
			graph.releaseRef( ref2 );
		}
	}

	/**
	 * @return a subset of the given {@code nodes} that contains only those
	 * nodes that are roots of the {@code graph}. (A note is considered a root
	 * if it has no incoming edges.)
	 */
	private static < V extends Vertex<E>, E extends Edge< V > > RefSet< V > filterRoots( Graph< V, E > graph, Collection< V > nodes )
	{
		final RefSet< V > roots = RefCollections.createRefSet( graph.vertices() );
		for ( V node : nodes )
			if ( node.incomingEdges().isEmpty() )
				roots.add( node );
		return roots;
	}
}
