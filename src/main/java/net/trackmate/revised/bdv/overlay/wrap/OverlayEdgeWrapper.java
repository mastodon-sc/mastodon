package net.trackmate.revised.bdv.overlay.wrap;

import net.trackmate.graph.Edge;
import net.trackmate.graph.Vertex;
import net.trackmate.revised.bdv.overlay.OverlayEdge;
import net.trackmate.spatial.HasTimepoint;

public class OverlayEdgeWrapper< V extends Vertex< E > & HasTimepoint, E extends Edge< V > >
	implements OverlayEdge< OverlayEdgeWrapper< V, E >, OverlayVertexWrapper< V, E > >
{
	private final OverlayGraphWrapper< V, E > wrapper;

	E we;

	OverlayEdgeWrapper( final OverlayGraphWrapper< V, E > wrapper )
	{
		this.wrapper = wrapper;
		we = wrapper.wrappedEdgeRef();
	}

	@Override
	public int getInternalPoolIndex()
	{
		return wrapper.idmap.getEdgeId( we );
	}

	@Override
	public OverlayEdgeWrapper< V, E > refTo( final OverlayEdgeWrapper< V, E > obj )
	{
		we = wrapper.idmap.getEdge( obj.getInternalPoolIndex(), we );
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
		vertex.wv = we.getSource( vertex.wv );
		return vertex;
	}

	@Override
	public OverlayVertexWrapper< V, E > getTarget()
	{
		return getTarget( wrapper.vertexRef() );
	}

	@Override
	public OverlayVertexWrapper< V, E > getTarget( final OverlayVertexWrapper< V, E > vertex )
	{
		vertex.wv = we.getTarget( vertex.wv );
		return vertex;
	}
}
