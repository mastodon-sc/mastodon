package org.mastodon.revised.bdv.overlay.wrap;

import org.mastodon.adapter.RefBimap;
import org.mastodon.graph.Edge;
import org.mastodon.graph.Vertex;

public class OverlayVertexWrapperBimap< V extends Vertex< E >, E extends Edge< V > >
	implements RefBimap< V, OverlayVertexWrapper< V, E > >
{
	// TODO: don't need ref ???
	@Override
	public V getLeft( final OverlayVertexWrapper< V, E > right /*, final V ref*/ )
	{
		return right.wv;
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

//	@Override
//	public OverlayVertexWrapper< V, E > extractRightRef( final V ref )
//	{
//		return null;
//	}
}
