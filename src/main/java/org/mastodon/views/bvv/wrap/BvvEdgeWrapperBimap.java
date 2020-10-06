package org.mastodon.views.bvv.wrap;

import org.mastodon.adapter.RefBimap;
import org.mastodon.collection.RefCollection;
import org.mastodon.graph.Edge;
import org.mastodon.graph.Vertex;

public class BvvEdgeWrapperBimap< V extends Vertex< E >, E extends Edge< V > >
	implements RefBimap< E, BvvEdgeWrapper< V, E > >
{
	private final RefCollection< BvvEdgeWrapper< V, E > > edges;

	public BvvEdgeWrapperBimap( final BvvGraphWrapper< V, E > graph )
	{
		this.edges = graph.edges();
	}

	@Override
	public E getLeft( final BvvEdgeWrapper< V, E > right )
	{
		return right.we;
	}

	@Override
	public BvvEdgeWrapper< V, E > getRight( final E left, final BvvEdgeWrapper< V, E > ref )
	{
		ref.we = left;
		return ref.orNull();
	}

	@Override
	public E reusableLeftRef( final BvvEdgeWrapper< V, E > right )
	{
		return right.ref;
	}

	@Override
	public BvvEdgeWrapper< V, E > reusableRightRef()
	{
		return edges.createRef();
	}

	@Override
	public void releaseRef( final BvvEdgeWrapper< V, E > ref )
	{
		edges.releaseRef( ref );
	}
}
