package org.mastodon.revised.bdv.overlay.wrap;

import org.mastodon.graph.Edge;
import org.mastodon.graph.Vertex;
import org.mastodon.revised.bdv.overlay.OverlayHighlight;
import org.mastodon.revised.ui.selection.HighlightListener;
import org.mastodon.revised.ui.selection.HighlightModel;

public class OverlayHighlightWrapper< V extends Vertex< E >, E extends Edge< V > >
	implements OverlayHighlight< OverlayVertexWrapper< V, E >, OverlayEdgeWrapper< V, E > >
{
	private final HighlightModel< V, E > wrappedHighlightModel;

	public OverlayHighlightWrapper( final HighlightModel< V, E > highlight )
	{
		this.wrappedHighlightModel = highlight;
	}

	@Override
	public OverlayVertexWrapper< V, E > getHighlightedVertex( final OverlayVertexWrapper< V, E > ref )
	{
		ref.wv = wrappedHighlightModel.getHighlightedVertex( ref.ref );
		return ref.orNull();
	}

	@Override
	public OverlayEdgeWrapper< V, E > getHighlightedEdge( final OverlayEdgeWrapper< V, E > ref )
	{
		ref.we = wrappedHighlightModel.getHighlightedEdge( ref.ref );
		return ref.orNull();
	}

	@Override
	public void highlightVertex( final OverlayVertexWrapper< V, E > vertex )
	{
		wrappedHighlightModel.highlightVertex( OverlayVertexWrapper.wrappedOrNull( vertex ) );
	}

	@Override
	public void highlightEdge( final OverlayEdgeWrapper< V, E > edge )
	{
		wrappedHighlightModel.highlightEdge( OverlayEdgeWrapper.wrappedOrNull( edge ) );
	}

	@Override
	public void clearHighlight()
	{
		wrappedHighlightModel.clearHighlight();
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
