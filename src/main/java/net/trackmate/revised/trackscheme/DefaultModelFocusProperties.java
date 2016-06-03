package net.trackmate.revised.trackscheme;

import net.trackmate.graph.Edge;
import net.trackmate.graph.GraphIdBimap;
import net.trackmate.graph.ReadOnlyGraph;
import net.trackmate.graph.Vertex;
import net.trackmate.revised.ui.selection.FocusListener;
import net.trackmate.revised.ui.selection.FocusModel;
import net.trackmate.spatial.HasTimepoint;


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
