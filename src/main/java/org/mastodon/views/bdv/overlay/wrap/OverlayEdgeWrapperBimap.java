package org.mastodon.views.bdv.overlay.wrap;

import org.mastodon.adapter.RefBimap;
import org.mastodon.collection.RefCollection;
import org.mastodon.graph.Edge;
import org.mastodon.graph.Vertex;

public class OverlayEdgeWrapperBimap< V extends Vertex< E >, E extends Edge< V > >
	implements RefBimap< E, OverlayEdgeWrapper< V, E > >
{
	private final RefCollection< OverlayEdgeWrapper< V, E > > edges;

	public OverlayEdgeWrapperBimap( final OverlayGraphWrapper< V, E > graph )
	{
		this.edges = graph.edges();
	}

	@Override
	public E getLeft( final OverlayEdgeWrapper< V, E > right )
	{
		return right.we;
	}

	@Override
	public OverlayEdgeWrapper< V, E > getRight( final E left, final OverlayEdgeWrapper< V, E > ref )
	{
		ref.we = left;
		return ref.orNull();
	}

	@Override
	public E reusableLeftRef( final OverlayEdgeWrapper< V, E > right )
	{
		return right.ref;
	}

	@Override
	public OverlayEdgeWrapper< V, E > reusableRightRef()
	{
		return edges.createRef();
	}

	@Override
	public void releaseRef( final OverlayEdgeWrapper< V, E > ref )
	{
		edges.releaseRef( ref );
	}
}
