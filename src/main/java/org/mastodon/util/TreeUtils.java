/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2025 Tobias Pietzsch, Jean-Yves Tinevez
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package org.mastodon.util;

import java.util.Collection;

import org.mastodon.collection.RefCollections;
import org.mastodon.collection.RefList;
import org.mastodon.collection.RefSet;
import org.mastodon.collection.RefStack;
import org.mastodon.graph.Edge;
import org.mastodon.graph.Graph;
import org.mastodon.graph.Vertex;
import org.mastodon.mamut.model.Model;
import org.mastodon.mamut.model.Spot;
import org.mastodon.mamut.model.branch.BranchSpot;

public class TreeUtils
{

	private TreeUtils()
	{
		// prevent instantiation of utility class
	}

	/**
	 * This method finds a subset of the given {@code selectedVertices} that
	 * contains only the roots of the subtrees that are selected. The order of
	 * the returned list follows the order of the {@code roots} list and the
	 * order of the outgoing edges of the graphs vertices.
	 * <p>
	 * Example {@code graph}:
	 * 
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
	 * If {@code selectedVertices} contains: {@code {a2, a4, a5, b1, b3, b4,
	 * b6}}, then the returned list will be: {@code [a2, b1, b6]}.
	 * 
	 * @param <V>
	 *            the type vertices in the graph.
	 * @param graph
	 *            the graph.
	 * @param roots
	 *            the roots of the graph, that is vertices with no incoming
	 *            edges.
	 * @param selectedVertices
	 *            the selected vertices.
	 * @return the subset of selected vertices that contains only the roots of
	 *         the subtrees.
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

	private static < V extends Vertex< ? > > boolean ensureNoLoop( final DepthFirstIteration.Step< V > step, final RefSet< V > visitedNodes )
	{
		if ( !step.isFirstVisit() )
			return true;

		final boolean isLoop = !visitedNodes.add( step.node() ); // The depth first iteration enters a node for the second time. -> there's a loop.
		if ( isLoop )
			step.truncate(); // Break the loop by not visiting the child nodes.

		return !isLoop;
	}

	/**
	 * This method returns the root nodes of the tracks (connected components)
	 * that contain any of the given {@code nodes}.
	 * <p>
	 * Example:
	 * 
	 * <pre>
	 *   A                B      C
	 *  / \             /   \  /
	 * a1  a2          b1    b2
	 *     |          / \   / \
	 *     a3        b3 b4 b5 b6
	 *    / \
	 *   a4 a5
	 * </pre>
	 * <p>
	 * If {@code nodes} contains {@code {a2, a4}} then the method will return
	 * {@code {A}}.
	 * <p>
	 * If {@code nodes} contains {@code {a2, a4, b4}} then the method will
	 * return {@code {A, B, C}}.
	 * 
	 * @param <V>
	 *            the type of vertices in the graph.
	 * @param <E>
	 *            the type of edges in the graph.
	 * @param graph
	 *            the graph.
	 * @param nodes
	 *            the nodes.
	 * @return the filtered roots.
	 */
	public static <V extends Vertex<E>, E extends Edge< V > > RefSet< V > findRootsOfTheGivenNodes( final Graph< V, E > graph, final Collection< V > nodes )
	{
		return filterRoots( graph, findAllConnectedNodes( graph, nodes ) );
	}

	/**
	 * Returns the set of predecessors of the given {@code nodes}. Please note
	 * that returned set also contains all the given {@code nodes}.
	 * 
	 * @param <V>
	 *            the type of vertices in the graph.
	 * @param <E>
	 *            the type of edges in the graph.
	 * @param graph
	 *            the graph.
	 * @param nodes
	 *            the nodes.
	 * @return the set of predecessors.
	 */
	private static < V extends Vertex<E>, E extends Edge< V > > RefSet< V > findAllConnectedNodes( final Graph< V, E > graph, final Collection< V > nodes )
	{
		// The following code performs an inverse depth first search starting
		// from the given nodes. The set of visited nodes is returned.
		final V ref = graph.vertexRef();
		final V ref2 = graph.vertexRef();
		try
		{
			final RefSet< V > visited = RefCollections.createRefSet( graph.vertices() );
			visited.addAll( nodes );
			final RefStack< V > stack = RefCollections.createRefStack( graph.vertices() );
			stack.addAll( visited );
			while ( ! stack.isEmpty() ) {
				final V node = stack.pop( ref );
				for ( final E edge : node.incomingEdges() ) {
					final V parentNode = edge.getSource( ref2 );
					addNode( visited, stack, parentNode );
				}
				for ( final E edge : node.outgoingEdges() ) {
					final V childNode = edge.getTarget( ref2 );
					addNode( visited, stack, childNode );
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

	private static < V extends Vertex<E>, E extends Edge< V > > void addNode( final RefSet< V > visited, final RefStack< V > stack, final V parentNode )
	{
		final boolean firstVisit = visited.add( parentNode );
		if ( firstVisit )
			stack.add( parentNode );
	}

	/**
	 * Returns a subset of the given {@code nodes} that contains only those
	 * nodes that are roots of the {@code graph}. (A note is considered a root
	 * if it has no incoming edges.)
	 * 
	 * @param <V>
	 *            the type of vertices in the graph.
	 * @param <E>
	 *            the type of edges in the graph.
	 * @param graph
	 *            the graph.
	 * @param nodes
	 *            the nodes.
	 * @return a subset of the nodes.
	 */
	private static < V extends Vertex<E>, E extends Edge< V > > RefSet< V > filterRoots( final Graph< V, E > graph, final Collection< V > nodes )
	{
		final RefSet< V > roots = RefCollections.createRefSet( graph.vertices() );
		for ( final V node : nodes )
			if ( node.incomingEdges().isEmpty() )
				roots.add( node );
		return roots;
	}

	/**
	 * Gets the minimum time point in the given {@link Model} at which at least one {@link Spot} exists in the Model.
	 * <br>
	 * If the model is empty, returns 0.
	 * @param model the {@link Model}
	 * @return the timepoint
	 */
	public static int getMinTimepoint( final Model model )
	{
		if ( model.getGraph().vertices().isEmpty() )
			return 0;
		int minTimepoint = Integer.MAX_VALUE;
		for ( final Spot spot : model.getGraph().vertices() )
			minTimepoint = Math.min( minTimepoint, spot.getTimepoint() );
		return minTimepoint;
	}

	/**
	 * Gets the maximum time point in the given {@link Model} at which at least one {@link Spot} exists in the Model.
	 * <br>
	 * If the model is empty, returns 0.
	 * @param model the {@link Model}
	 * @return the timepoint
	 */
	public static int getMaxTimepoint( final Model model )
	{
		int max = 0;
		for ( final Spot spot : model.getGraph().vertices() )
			max = Math.max( max, spot.getTimepoint() );
		return max;
	}

	/**
	 * Gets the first {@link Spot} within the given {@link BranchSpot}.
	 * @param model the {@link Model} to which the {@link BranchSpot} belongs
	 * @param branchSpot the {@link BranchSpot} to query
	 * @return the first {@link Spot}
	 */
	public static Spot getFirstSpot( final Model model, final BranchSpot branchSpot, final Spot ref )
	{
		return model.getBranchGraph().getFirstLinkedVertex( branchSpot, ref );
	}
}
