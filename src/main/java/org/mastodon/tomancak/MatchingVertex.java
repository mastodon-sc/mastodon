package org.mastodon.tomancak;

import org.mastodon.graph.ref.AbstractVertex;
import org.mastodon.pool.ByteMappedElement;
import org.mastodon.tomancak.MatchingGraph.MatchingVertexPool;

public class MatchingVertex extends AbstractVertex< MatchingVertex, MatchingEdge, MatchingVertexPool, ByteMappedElement >
{
	MatchingVertex( final MatchingVertexPool pool )
	{
		super( pool );
	}
}
