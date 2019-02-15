package org.mastodon.tomancak;

import org.mastodon.graph.ref.AbstractEdge;
import org.mastodon.pool.ByteMappedElement;
import org.mastodon.tomancak.MatchingGraph.MatchingEdgePool;

public class MatchingEdge extends AbstractEdge< MatchingEdge, MatchingVertex, MatchingEdgePool, ByteMappedElement >
{
	MatchingEdge( final MatchingEdgePool pool )
	{
		super( pool );
	}

	public double getDistSqu()
	{
		return pool.distSqu.get( this );
	}

	public void setDistSqu( final double d )
	{
		pool.distSqu.set( this, d );
	}

	public double getMahalDistSqu()
	{
		return pool.mahalDistSqu.get( this );
	}

	public void setMahalDistSqu( final double d )
	{
		pool.mahalDistSqu.set( this, d );
	}
}
