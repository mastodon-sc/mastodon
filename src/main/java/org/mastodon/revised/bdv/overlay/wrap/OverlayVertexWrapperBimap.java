package org.mastodon.revised.bdv.overlay.wrap;

import org.mastodon.adapter.RefBimap;
import org.mastodon.collection.RefCollection;
import org.mastodon.graph.Edge;
import org.mastodon.graph.Vertex;

public class OverlayVertexWrapperBimap< V extends Vertex< E >, E extends Edge< V > >
	implements RefBimap< V, OverlayVertexWrapper< V, E > >
{
	private final RefCollection< OverlayVertexWrapper< V, E > > vertices;

	public OverlayVertexWrapperBimap( final OverlayGraphWrapper< V, E > graph )
	{
		this.vertices = graph.vertices();
	}

	@Override
	public V getLeft( final OverlayVertexWrapper< V, E > right )
	{
		return right == null ? null : right.wv;
	}

	@Override
	public OverlayVertexWrapper< V, E > getRight( final V left, final OverlayVertexWrapper< V, E > ref )
	{
		ref.wv = left;
		return ref.orNull();
	}

	@Override
	public V reusableLeftRef( final OverlayVertexWrapper< V, E > right )
	{
		return right.ref;
	}

	@Override
	public OverlayVertexWrapper< V, E > reusableRightRef()
	{
		return vertices.createRef();
	}

	@Override
	public void releaseRef( final OverlayVertexWrapper< V, E > ref )
	{
		vertices.releaseRef( ref );
	}
}
