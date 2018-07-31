package org.mastodon.revised.bvv.wrap;

import org.mastodon.graph.Edge;
import org.mastodon.graph.Vertex;
import org.mastodon.revised.bdv.overlay.wrap.OverlayProperties;
import org.mastodon.revised.bvv.BvvEdge;

public class BvvEdgeWrapper< V extends Vertex< E >, E extends Edge< V > >
	implements BvvEdge< BvvEdgeWrapper< V, E >, BvvVertexWrapper< V, E > >
{
	private final BvvGraphWrapper< V, E > wrapper;

	final E ref;

	E we;

	private final OverlayProperties< V, E > overlayProperties;

	BvvEdgeWrapper( final BvvGraphWrapper< V, E > wrapper )
	{
		this.wrapper = wrapper;
		ref = wrapper.wrappedGraph.edgeRef();
		overlayProperties = wrapper.overlayProperties;
	}

	@Override
	public int getInternalPoolIndex()
	{
		return wrapper.idmap.getEdgeId( we );
	}

	@Override
	public BvvEdgeWrapper< V, E > refTo( final BvvEdgeWrapper< V, E > obj )
	{
		we = wrapper.idmap.getEdge( obj.getInternalPoolIndex(), ref );
		return this;
	}

	@Override
	public BvvEdgeWrapper< V, E > init()
	{
		overlayProperties.initEdge( we );
		return this;
	}

	@Override
	public BvvVertexWrapper< V, E > getSource()
	{
		return getSource( wrapper.vertexRef() );
	}

	@Override
	public BvvVertexWrapper< V, E > getSource( final BvvVertexWrapper< V, E > vertex )
	{
		vertex.wv = we.getSource( vertex.ref );
		return vertex;
	}

	@Override
	public int getSourceOutIndex()
	{
		return we.getSourceOutIndex();
	}

	@Override
	public BvvVertexWrapper< V, E > getTarget()
	{
		return getTarget( wrapper.vertexRef() );
	}

	@Override
	public BvvVertexWrapper< V, E > getTarget( final BvvVertexWrapper< V, E > vertex )
	{
		vertex.wv = we.getTarget( vertex.ref );
		return vertex;
	}

	@Override
	public int getTargetInIndex()
	{
		return we.getTargetInIndex();
	}

	@Override
	public int hashCode()
	{
		return we.hashCode();
	}

	@Override
	public boolean equals( final Object obj )
	{
		return obj instanceof BvvEdgeWrapper< ?, ? > &&
				we.equals( ( ( BvvEdgeWrapper< ?, ? > ) obj ).we );
	}

	/**
	 * Returns {@code this} if this {@link BvvEdgeWrapper} currently wraps
	 * an {@code E}, or null otherwise.
	 *
	 * @return {@code this} if this {@link BvvEdgeWrapper} currently wraps
	 *         an {@code E}, or null otherwise.
	 */
	BvvEdgeWrapper< V, E > orNull()
	{
		return we == null ? null : this;
	}

	/**
	 * If called with a non-null {@link BvvEdgeWrapper} returns the
	 * currently wrapped {@code E}, otherwise null.
	 *
	 * @return {@code null} if {@code wrapper == null}, otherwise the {@code E}
	 *         wrapped by {@code wrapper}.
	 */
	static < E extends Edge< ? > > E wrappedOrNull( final BvvEdgeWrapper< ?, E > wrapper )
	{
		return wrapper == null ? null : wrapper.we;
	}
}
