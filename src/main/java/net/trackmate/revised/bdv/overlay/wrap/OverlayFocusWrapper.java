package net.trackmate.revised.bdv.overlay.wrap;

import net.trackmate.graph.zzgraphinterfaces.Edge;
import net.trackmate.graph.zzgraphinterfaces.GraphIdBimap;
import net.trackmate.graph.zzgraphinterfaces.Vertex;
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
		final V h = wrappedFocusModel.getFocusedVertex( ref.wv );
		if ( h == null )
			return null;
		else
		{
			ref.wv = idmap.getVertex( idmap.getVertexId( h ), ref.wv );
			return ref;
		}
	}

	@Override
	public void focusVertex( final OverlayVertexWrapper< V, E > vertex )
	{
		if ( vertex == null )
			wrappedFocusModel.focusVertex( null );
		else
			wrappedFocusModel.focusVertex( vertex.wv );
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
