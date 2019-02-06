package org.mastodon.tomancak;

import org.mastodon.graph.ref.AbstractVertex;
import org.mastodon.pool.ByteMappedElement;
import org.mastodon.revised.model.mamut.Spot;
import org.mastodon.tomancak.MatchingGraph.MatchingVertexPool;

public class MatchingVertex extends AbstractVertex< MatchingVertex, MatchingEdge, MatchingVertexPool, ByteMappedElement >
{
	MatchingVertex( final MatchingVertexPool pool )
	{
		super( pool );
	}

	MatchingVertex init( final Spot spot )
	{
		pool.graphIndex.set( this, pool.modelGraphToIndex.get( spot.getModelGraph() ) );
		pool.graphVertexIndex.set( this, spot.getInternalPoolIndex() );
		return this;
	}
}
