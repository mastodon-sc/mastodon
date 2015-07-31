package net.trackmate.bdv.wrapper;

import net.trackmate.graph.Edge;
import net.trackmate.graph.Vertex;
import net.trackmate.trackscheme.TrackSchemeEdge;

public class OverlayEdgeWrapper< V extends Vertex< E >, E extends Edge< V > >
	implements OverlayEdge< OverlayEdgeWrapper< V, E >, OverlayVertexWrapper< V, E > >
{
	private final OverlayGraphWrapper< V, E > wrapper;

	final TrackSchemeEdge tse;

	OverlayEdgeWrapper( final OverlayGraphWrapper< V, E > wrapper )
	{
		this.wrapper = wrapper;
		this.tse = wrapper.trackSchemeGraph.edgeRef();
	}

	@Override
	public int getInternalPoolIndex()
	{
		return tse.getInternalPoolIndex();
	}

	@Override
	public OverlayEdgeWrapper< V, E > refTo( final OverlayEdgeWrapper< V, E > obj )
	{
		tse.refTo( obj.tse );
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
		tse.getSource( vertex.tsv );
		vertex.updateModelVertexRef();
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
		tse.getTarget( vertex.tsv );
		vertex.updateModelVertexRef();
		return vertex;
	}
}
