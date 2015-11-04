package net.trackmate.revised.bdv.overlay.wrap;

import net.trackmate.graph.Edge;
import net.trackmate.graph.GraphIdBimap;
import net.trackmate.graph.ReadOnlyGraph;
import net.trackmate.graph.Vertex;
import net.trackmate.revised.bdv.overlay.OverlayHighlight;
import net.trackmate.revised.ui.selection.HighlightListener;
import net.trackmate.revised.ui.selection.HighlightModel;

public class OverlayHighlightWrapper< V extends Vertex< E >, E extends Edge< V > >
	implements OverlayHighlight< OverlayVertexWrapper< V, E >, OverlayEdgeWrapper< V, E > >
{
	final ReadOnlyGraph< V, E > wrappedGraph;

	final GraphIdBimap< V, E > idmap;

	final HighlightModel< V, E > wrappedHighlightModel;

	public OverlayHighlightWrapper(
			final ReadOnlyGraph< V, E > graph,
			final GraphIdBimap< V, E > idmap,
			final HighlightModel< V, E > highlight )
	{
		this.wrappedGraph = graph;
		this.idmap = idmap;
		this.wrappedHighlightModel = highlight;
	}

	@Override
	public OverlayVertexWrapper< V, E > getHighlightedVertex( final OverlayVertexWrapper< V, E > ref )
	{
		final V h = wrappedHighlightModel.getHighlightedVertex( ref.wv );
		if ( h == null )
			return null;
		else
		{
			ref.wv = idmap.getVertex( idmap.getVertexId( h ), ref.wv );
			return ref;
		}
	}

	@Override
	public void highlightVertex( final OverlayVertexWrapper< V, E > vertex )
	{
		if ( vertex == null )
			wrappedHighlightModel.highlightVertex( null );
		else
			wrappedHighlightModel.highlightVertex( vertex.wv );
	}

	@Override
	public boolean addHighlightListener( final HighlightListener l )
	{
		return wrappedHighlightModel.addHighlightListener( l );
	}

	@Override
	public boolean removeHighlightListener( final HighlightListener l )
	{
		return wrappedHighlightModel.removeHighlightListener( l );
	}
}
