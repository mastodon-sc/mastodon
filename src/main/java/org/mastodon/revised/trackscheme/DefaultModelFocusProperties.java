package org.mastodon.revised.trackscheme;

import org.mastodon.graph.Edge;
import org.mastodon.graph.GraphIdBimap;
import org.mastodon.graph.ReadOnlyGraph;
import org.mastodon.graph.Vertex;
import org.mastodon.revised.ui.selection.FocusListener;
import org.mastodon.revised.ui.selection.FocusModel;
import org.mastodon.spatial.HasTimepoint;


public class DefaultModelFocusProperties<
		V extends Vertex< E > & HasTimepoint,
		E extends Edge< V > >
		implements ModelFocusProperties
{
	private final ReadOnlyGraph< V, E > graph;

	private final GraphIdBimap< V, E > idmap;

	private final FocusModel< V, E > focus;

	public DefaultModelFocusProperties(
			final ReadOnlyGraph< V, E > graph,
			final GraphIdBimap< V, E > idmap,
			final FocusModel< V, E > focus )
	{
		this.graph = graph;
		this.idmap = idmap;
		this.focus = focus;
	}

	@Override
	public int getFocusedVertexId()
	{
		final V ref = graph.vertexRef();
		final V focused = focus.getFocusedVertex( ref );
		final int id = ( focused == null ) ? -1 : idmap.getVertexId( focused );
		graph.releaseRef( ref );
		return id;
	}

	@Override
	public void focusVertex( final int id )
	{
		if ( id < 0 )
			focus.focusVertex( null );
		else
		{
			final V ref = graph.vertexRef();
			focus.focusVertex( idmap.getVertex( id, ref ) );
			graph.releaseRef( ref );
		}
	}

	@Override
	public boolean addFocusListener( final FocusListener l )
	{
		return focus.addFocusListener( l );
	}

	@Override
	public boolean removeFocusListener( final FocusListener l )
	{
		return focus.removeFocusListener( l );
	}
}
