package net.trackmate.revised.bdv.overlay.wrap;

import net.trackmate.graph.Edge;
import net.trackmate.graph.GraphIdBimap;
import net.trackmate.graph.Vertex;
import net.trackmate.revised.bdv.overlay.OverlayFocus;
import net.trackmate.revised.ui.selection.FocusListener;
import net.trackmate.revised.ui.selection.FocusModel;

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
