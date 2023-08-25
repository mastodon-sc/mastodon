/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2022 Tobias Pietzsch, Jean-Yves Tinevez
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.mastodon.graph.Edge;
import org.mastodon.graph.Graph;
import org.mastodon.graph.Vertex;
import org.mastodon.views.trackscheme.LineageTreeLayout;

import net.imglib2.util.Cast;

/**
 * This class allows to iterate over a {@link Graph} in depth first iteration
 * order.
 * <p>
 * "mastodon-graph" has another implementation of a
 * {@link org.mastodon.graph.algorithm.traversal.DepthFirstIterator depth first
 * iteration}. But this class provides additional features, necessary for
 * {@link LineageTreeLayout}.
 * <ol>
 * <li>It is possible the exclude subtrees from the iteration.</li>
 * <li>Children are visited in the order of the respective outgoing edges.</li>
 * <li>Nodes that have children (non leafs) are visited twice. Once before the
 * child nodes will be visited, and once after the child nodes have been
 * visited.</li>
 * </ol>
 * <p>
 * (Warning: The iteration may not terminate if the graph contains a directed
 * loop. Client code that should work with such graphs must detect this loops
 * and call {@link Step#truncate()})
 * <p>
 * Let us look at a small example. Lets consider this small graph:
 * 
 * <pre>
 *  a -&gt; b -&gt; c
 *   \    \
 *    \    -&gt; d
 *     \
 *      -&gt; e
 * </pre>
 *
 * An depth first iteration can be executed using this code:
 * 
 * <pre>
 * {@code
 * for ( DepthFirstIteration.Step< Spot > step : DepthFirstIteration.forRoot( graph, root ) )
 * {
 * 	Spot node = step.node();
 * 	if ( step.isFirstVisit() )
 * 		System.out.println( "first visit " + node.getLabel() );
 * 	if ( step.isSecondVisit() )
 * 		System.out.println( "second visit " + node.getLabel() );
 * 	if ( step.isLeaf() )
 * 		System.out.println( "leaf " + node.getLabel() );
 * }
 * }
 * </pre>
 * 
 * It will print the following on the console:
 * 
 * <pre>
 * first visit a
 * first visit b
 * leaf c
 * leaf d
 * second visit b
 * leaf e
 * second visit a
 * </pre>
 *
 * @author Matthias Arzt
 */
public class DepthFirstIteration
{

	public interface Step< V extends Vertex< ? > >
	{

		/*
		 * @returns the node that is currently visited by the iterator.
		 */
		V node();

		/**
		 * @return the length of the path from the root node the the
		 *         {@link #node() current node}.
		 */
		int depth();

		/**
		 * @return true if the {@link #node() node} has not outgoing edges / child nodes.
		 */
		boolean isLeaf();

		/**
		 * @return true if the {@link #node() node} is visited the first time
		 * (before visiting all the child nodes).
		 */
		boolean isFirstVisit();

		/**
		 * @return true if the {@link #node() node} is visited the second time
		 * (after visiting all the child nodes).
		 */
		boolean isSecondVisit();

		/**
		 * Calling this method when a {@link #node() node} is first visited,
		 * will cause the iterator to skip all child nodes (and the entire
		 * subtrees) of the current node.
		 */
		void truncate();
	}

	public static < V extends Vertex< ? > > Iterable< Step< V > > forRoot( final Graph< V, ? > graph, final V root )
	{
		return () -> new DFIterator<>( graph, root );
	}

	private DepthFirstIteration()
	{}

	private enum Stage
	{
		INIT, FIRST_VISIT, SECOND_VISIT, LEAF;
	}

	private static class DFIterator< V extends Vertex< ? > > implements Iterator< Step< V > >, Step< V >
	{
		private final Graph< V, ? > graph;

		private Stage stage = Stage.INIT;

		private V node = null;

		private boolean truncate = false;

		private int depth = -1;

		private final List< Entry< V > > stack = new ArrayList<>();

		public DFIterator( final Graph< V, ? > graph, final V root )
		{
			this.graph = graph;
			push( root );
		}

		@Override
		public boolean hasNext()
		{
			return depth > 0 ||
					( stage == Stage.FIRST_VISIT && !truncate ) ||
					stage == Stage.INIT;
		}

		@Override
		public Step< V > next()
		{
			if ( stage == Stage.INIT )
			{
				final Entry< V > entry = stack.get( depth );
				this.node = entry.node;
				this.stage = entry.edges.hasNext() ? Stage.FIRST_VISIT : Stage.LEAF;
				this.truncate = false;
				return this;
			}

			if ( stage == Stage.FIRST_VISIT && !truncate )
			{
				gotoNextChild();
				return this;
			}
			graph.releaseRef( stack.get( depth ).node );
			depth--;
			final Entry< V > entry = stack.get( depth );
			if ( entry.edges.hasNext() )
			{
				gotoNextChild();
				return this;
			}

			this.stage = Stage.SECOND_VISIT;
			this.node = entry.node;
			return this;
		}

		private DFIterator< V > gotoNextChild()
		{
			Entry< V > entry = stack.get( depth );
			final Edge< V > edge = entry.edges.next();
			final V child = edge.getTarget( graph.vertexRef() );
			push( child );
			entry = stack.get( depth );
			this.node = entry.node;
			this.stage = entry.edges.hasNext() ? Stage.FIRST_VISIT : Stage.LEAF;
			this.truncate = false;
			return this;
		}

		@Override
		public boolean isLeaf()
		{
			return stage == Stage.LEAF;
		}

		@Override
		public boolean isFirstVisit()
		{
			return stage == Stage.FIRST_VISIT;
		}

		@Override
		public boolean isSecondVisit()
		{
			return stage == Stage.SECOND_VISIT;
		}

		@Override
		public V node()
		{
			return node;
		}

		@Override
		public int depth()
		{
			return depth;
		}

		@Override
		public void truncate()
		{
			this.truncate = true;
		}

		private void push( final V root )
		{
			Entry< V > entry;
			depth++;
			if ( stack.size() <= depth )
			{
				entry = new Entry< V >();
				stack.add( entry );
			}
			entry = stack.get( depth );
			entry.node = root;
			entry.edges = Cast.unchecked( root.outgoingEdges().iterator() );
		}

		private static class Entry< V extends Vertex< ? > >
		{
			private V node;

			private Iterator< Edge< V > > edges;
		}

	}

}
