/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2021 Tobias Pietzsch, Jean-Yves Tinevez
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
package org.mastodon.views.trackscheme;

import net.imglib2.util.Cast;
import org.mastodon.graph.Edge;
import org.mastodon.graph.Graph;
import org.mastodon.graph.Vertex;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * This class allows to iterate over a {@link Graph} in depth first iteration order.
 * <p>
 * (Warning: The graph must be a tree, the code won't terminate otherwise.)
 * <p>
 * "mastodon-graph" has another implementation of a
 * {@link org.mastodon.graph.algorithm.traversal.DepthFirstIterator depth first iteration}.
 * But this class provides additional features, necessary for {@link LineageTreeLayout}.
 * <ol>
 *    <li>It is possible the {@link #excludeNodeAction exclude subtrees} from the iteration.</li>
 *    <li>Children are visited in the order of the respective outgoing edges.</li>
 *    <li>
 *        Non leaf nodes are visited twice. Once {@link #setVisitNodeBeforeChildrenAction before the child nodes}
 *        and once {@link #setVisitNodeAfterChildrenAction after the child nodes}.
 *    </li>
 * </ol>
 * Let's, as an example, consider this small graph:
 * <pre>
 *  a -> b -> c
 *   \    \
 *    \    -> d
 *     \
 *    	->e
 * </pre>
 *
 * An depth first iteration can be executed using this code:
 * <pre>
 * {@code
 * DepthFirstIteration<Spot> df = new DepthFirstIteration<>( graph );
 * df.setVisitNodeBeforeChildrenAction(
 *     node -> System.out.println( "before node " + node.getLabel() )
 * );
 * df.setVisitLeafAction(
 *     leaf -> System.out.println( "leaf " + leaf.getLabel() )
 * );
 * df.setVisitNodeAfterChildrenAction(
 *     ( node, children ) -> System.out.println( "after node " + node.getLabel() )
 * );
 * df.runForRoot( a );
 * }
 * </pre>
 *
 * It will print the following on the console:
 * <pre>
 * before node a
 * before node b
 * leaf c
 * leaf d
 * after node b
 * leaf e
 * after node a
 * </pre>
 *
 * @author Matthias Arzt
 */
public class DepthFirstIteration<V extends Vertex<?>>
{
	static{
	}

	private final Graph<V, ?> graph;
	private Predicate<V> excludeNodeAction = node -> false;
	private Consumer<V> visitNodeBeforeChildrenAction = node -> {};
	private Consumer<V> visitLeafAction = leaf -> {};
	private BiConsumer<V, List<V>> visitNodeAfterChildrenAction = (node, children) -> {};

	public DepthFirstIteration( Graph<V, ?> graph ) {
		this.graph = graph;
	}

	public void setExcludeNodeAction( Predicate<V> excludeNodeAction ) {
		this.excludeNodeAction = excludeNodeAction;
	}

	public void setVisitNodeBeforeChildrenAction( Consumer<V> visitNodeBeforeChildrenAction )
	{
		this.visitNodeBeforeChildrenAction = visitNodeBeforeChildrenAction;
	}

	public void setVisitLeafAction( Consumer<V> visitLeafAction )
	{
		this.visitLeafAction = visitLeafAction;
	}

	public void setVisitNodeAfterChildrenAction( BiConsumer<V, List<V>> visitNodeAfterChildrenAction )
	{
		this.visitNodeAfterChildrenAction = visitNodeAfterChildrenAction;
	}

	public void runForRoot(V root) {
		push(root);
		while(depth > 0) {
			Entry<V> entry = next.get( depth - 1 );
			V node = entry.node;
			if( entry.first ) {
				entry.first = false;
				if( excludeNodeAction.test( node ))
				{
					lastVisited.add( node );
					depth--;
					continue;
				}
				if(!entry.edges.hasNext()) {
					visitLeafAction.accept( node );
					lastVisited.add( node );
					depth--;
					continue;
				}
				visitNodeBeforeChildrenAction.accept( node );
			}
			if( entry.edges.hasNext() ) {
				entry.numberOfChildren++;
				Edge<V> edge = entry.edges.next();
				V child = edge.getTarget(graph.vertexRef());
				push(child);
			}
			else {
				int n = entry.numberOfChildren;
				int size = lastVisited.size();
				subList.setOffsetAndSize( size - n, n );
				visitNodeAfterChildrenAction.accept( node, subList );
				for ( int i = size - 1; i >= size - n; i-- )
					graph.releaseRef( lastVisited.remove( i ) );
				lastVisited.add( node );
				depth--;
			}
		}
		lastVisited.clear();
	}

	private void push( V root )
	{
		Entry<V> entry;
		if(next.size() <= depth ) {
			entry = new Entry<V>();
			next.add( entry );
		}
		entry = next.get(depth);
		entry.node = root;
		entry.edges = Cast.unchecked( root.outgoingEdges().iterator() );
		entry.first = true;
		entry.numberOfChildren = 0;
		depth++;
	}

	private static class Entry<V extends Vertex<?>> {
		private V node;
		private boolean first;
		private Iterator<Edge<V>> edges;
		private int numberOfChildren;
	}

	private int depth = 0;
	private final List<Entry<V>> next = new ArrayList<>();
	private final List<V> lastVisited = new ArrayList<>();
	private final SubList<V> subList = new SubList<>( lastVisited );

	private static class SubList<T> extends AbstractList<T>
	{
		private final List<T> source;

		private int offset;

		private int size;

		public SubList( List<T> source )
		{
			this.source = source;
			this.size = source.size();
			this.offset = 0;
		}

		public void setOffsetAndSize( int offset, int size )
		{
			this.offset = offset;
			this.size = size;
		}

		@Override
		public T get( int index )
		{
			if ( index < 0 || index >= size )
				throw new NoSuchElementException();
			return source.get( index + offset );
		}

		@Override
		public int size()
		{
			return size;
		}
	}
}
