package org.mastodon.revised.bdv.overlay.wrap;

import org.mastodon.graph.Edge;
import org.mastodon.graph.GraphIdBimap;
import org.mastodon.graph.Vertex;
import org.mastodon.revised.bdv.overlay.OverlayFocus;
import org.mastodon.revised.ui.selection.FocusListener;
import org.mastodon.revised.ui.selection.FocusModel;

public class OverlayFocusWrapper< V extends Vertex< E >, E extends Edge< V > >
	implements OverlayFocus< OverlayVertexWrapper< V, E >, OverlayEdgeWrapper< V, E > >
{
	private final GraphIdBimap< V, E > idmap;

	private final FocusModel< V, E > wrappedFocusModel;

	public OverlayFocusWrapper(
			final GraphIdBimap< V, E > idmap,
			final FocusModel< V, E > focus )
	{
		this.idmap = idmap;
		this.wrappedFocusModel = focus;
	}

	@Override
	public OverlayVertexWrapper< V, E > getFocusedVertex( final OverlayVertexWrapper< V, E > ref )
	{
		ref.wv = wrappedFocusModel.getFocusedVertex( ref.ref );
		return ref.orNull();
	}

	@Override
	public void focusVertex( final OverlayVertexWrapper< V, E > vertex )
	{
		wrappedFocusModel.focusVertex( OverlayVertexWrapper.wrappedOrNull( vertex ) );
	}

	@Override
	public boolean addFocusListener( final FocusListener l )
	{
		return wrappedFocusModel.addFocusListener( l );
	}

	@Override
	public boolean removeFocusListener( final FocusListener l )
	{
		return wrappedFocusModel.removeFocusListener( l );
	}
}
