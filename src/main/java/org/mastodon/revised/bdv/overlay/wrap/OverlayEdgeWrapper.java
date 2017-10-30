package org.mastodon.revised.bdv.overlay.wrap;

import org.mastodon.graph.Edge;
import org.mastodon.graph.Vertex;
import org.mastodon.revised.bdv.overlay.OverlayEdge;

public class OverlayEdgeWrapper< V extends Vertex< E >, E extends Edge< V > >
	implements OverlayEdge< OverlayEdgeWrapper< V, E >, OverlayVertexWrapper< V, E > >
{
	private final OverlayGraphWrapper< V, E > wrapper;

	final E ref;

	E we;

	private final OverlayProperties< V, E > overlayProperties;

	OverlayEdgeWrapper( final OverlayGraphWrapper< V, E > wrapper )
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
	public OverlayEdgeWrapper< V, E > refTo( final OverlayEdgeWrapper< V, E > obj )
	{
		we = wrapper.idmap.getEdge( obj.getInternalPoolIndex(), ref );
		return this;
	}

	@Override
	public OverlayEdgeWrapper< V, E > init()
	{
		overlayProperties.initEdge( we );
		return this;
	}

	@Override
	public OverlayVertexWrapper< V, E > getSource()
	{
		return getSource( wrapper.vertexRef() );
	}

	@Override
	public OverlayVertexWrapper< V, E > getSource( final OverlayVertexWrapper< V, E > vertex )
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
	public OverlayVertexWrapper< V, E > getTarget()
	{
		return getTarget( wrapper.vertexRef() );
	}

	@Override
	public OverlayVertexWrapper< V, E > getTarget( final OverlayVertexWrapper< V, E > vertex )
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
		return obj instanceof OverlayEdgeWrapper< ?, ? > &&
				we.equals( ( ( OverlayEdgeWrapper< ?, ? > ) obj ).we );
	}

	/**
	 * Returns {@code this} if this {@link OverlayEdgeWrapper} currently wraps
	 * an {@code E}, or null otherwise.
	 *
	 * @return {@code this} if this {@link OverlayEdgeWrapper} currently wraps
	 *         an {@code E}, or null otherwise.
	 */
	OverlayEdgeWrapper< V, E > orNull()
	{
		return we == null ? null : this;
	}

	/**
	 * If called with a non-null {@link OverlayEdgeWrapper} returns the
	 * currently wrapped {@code E}, otherwise null.
	 *
	 * @return {@code null} if {@code wrapper == null}, otherwise the {@code E}
	 *         wrapped by {@code wrapper}.
	 */
	static < E extends Edge< ? > > E wrappedOrNull( final OverlayEdgeWrapper< ?, E > wrapper )
	{
		return wrapper == null ? null : wrapper.we;
	}
}
