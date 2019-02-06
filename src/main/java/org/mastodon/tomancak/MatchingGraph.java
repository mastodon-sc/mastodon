package org.mastodon.tomancak;

import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import java.util.List;
import org.mastodon.graph.ref.AbstractEdgePool;
import org.mastodon.graph.ref.AbstractEdgePool.AbstractEdgeLayout;
import org.mastodon.graph.ref.AbstractVertexPool;
import org.mastodon.graph.ref.AbstractVertexPool.AbstractVertexLayout;
import org.mastodon.graph.ref.GraphImp;
import org.mastodon.pool.ByteMappedElement;
import org.mastodon.pool.ByteMappedElementArray;
import org.mastodon.pool.SingleArrayMemPool;
import org.mastodon.pool.attributes.IndexAttribute;
import org.mastodon.revised.model.mamut.ModelGraph;

public class MatchingGraph extends GraphImp<
		MatchingGraph.MatchingVertexPool,
		MatchingGraph.MatchingEdgePool,
		MatchingVertex, MatchingEdge, ByteMappedElement >
{
	public MatchingGraph(
			final List< ModelGraph > modelGraphs,
			final int initialCapacity )
	{
		super( new MatchingEdgePool(
				initialCapacity,
				new MatchingVertexPool(
						initialCapacity,
						modelGraphs ) ) );
	}

	/*
	 * vertex and edge pools
	 */

	static class MatchingVertexLayout extends AbstractVertexLayout
	{
		final IndexField graphIndex = indexField();
		final IndexField graphVertexIndex = indexField();
	}

	static MatchingVertexLayout vertexLayout = new MatchingVertexLayout();

	static class MatchingVertexPool extends AbstractVertexPool< MatchingVertex, MatchingEdge, ByteMappedElement >
	{
		final List< ModelGraph > modelGraphs;
		final TObjectIntMap< ModelGraph > modelGraphToIndex;

		final IndexAttribute< MatchingVertex > graphIndex = new IndexAttribute<>( vertexLayout.graphIndex, this );
		final IndexAttribute< MatchingVertex > graphVertexIndex = new IndexAttribute<>( vertexLayout.graphVertexIndex, this );

		private MatchingVertexPool( final int initialCapacity, final List< ModelGraph > modelGraphs )
		{
			super( initialCapacity, vertexLayout, MatchingVertex.class, SingleArrayMemPool.factory( ByteMappedElementArray.factory ) );
			this.modelGraphs = modelGraphs;
			modelGraphToIndex = new TObjectIntHashMap< ModelGraph >();
			for ( int i = 0; i < modelGraphs.size(); i++ )
				modelGraphToIndex.put( modelGraphs.get( i ), i );
		}

		@Override
		protected MatchingVertex createEmptyRef()
		{
			return new MatchingVertex( this );
		}
	}

	static class MatchingEdgeLayout extends AbstractEdgeLayout {}

	static MatchingEdgeLayout edgeLayout = new MatchingEdgeLayout();

	static class MatchingEdgePool extends AbstractEdgePool< MatchingEdge, MatchingVertex, ByteMappedElement >
	{
		private MatchingEdgePool( final int initialCapacity, final MatchingVertexPool vertexPool )
		{
			super( initialCapacity, edgeLayout, MatchingEdge.class, SingleArrayMemPool.factory( ByteMappedElementArray.factory ), vertexPool );
			vertexPool.linkEdgePool( this );
		}

		@Override
		protected MatchingEdge createEmptyRef()
		{
			return new MatchingEdge( this );
		}
	}

}
