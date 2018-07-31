package org.mastodon.revised.bvv.wrap;

import org.mastodon.adapter.RefBimap;
import org.mastodon.collection.RefCollection;
import org.mastodon.graph.Edge;
import org.mastodon.graph.Vertex;

public class BvvVertexWrapperBimap< V extends Vertex< E >, E extends Edge< V > >
	implements RefBimap< V, BvvVertexWrapper< V, E > >
{
	private final RefCollection< BvvVertexWrapper< V, E > > vertices;

	public BvvVertexWrapperBimap( final BvvGraphWrapper< V, E > graph )
	{
		this.vertices = graph.vertices();
	}

	@Override
	public V getLeft( final BvvVertexWrapper< V, E > right )
	{
		return right == null ? null : right.wv;
	}

	@Override
	public BvvVertexWrapper< V, E > getRight( final V left, final BvvVertexWrapper< V, E > ref )
	{
		ref.wv = left;
		return ref.orNull();
	}

	@Override
	public V reusableLeftRef( final BvvVertexWrapper< V, E > right )
	{
		return right.ref;
	}

	@Override
	public BvvVertexWrapper< V, E > reusableRightRef()
	{
		return vertices.createRef();
	}

	@Override
	public void releaseRef( final BvvVertexWrapper< V, E > ref )
	{
		vertices.releaseRef( ref );
	}
}
