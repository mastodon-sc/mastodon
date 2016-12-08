package org.mastodon.adapter;

import org.mastodon.graph.Edge;
import org.mastodon.graph.Vertex;
import org.mastodon.revised.ui.selection.FocusListener;
import org.mastodon.revised.ui.selection.FocusModel;

public class FocusAdapter< V extends Vertex< E >, E extends Edge< V >, WV extends Vertex< WE >, WE extends Edge< WV > >
		implements FocusModel< WV, WE >
{
	private final FocusModel< V, E > focus;

	private final RefBimap< V, WV > vertexMap;

	private final RefBimap< E, WE > edgeMap;

	public FocusAdapter(
			final FocusModel< V, E > focus,
			final RefBimap< V, WV > vertexMap,
			final RefBimap< E, WE > edgeMap )
	{
		this.focus = focus;
		this.vertexMap = vertexMap;
		this.edgeMap = edgeMap;
	}

	@Override
	public void focusVertex( final WV vertex )
	{
		focus.focusVertex( vertexMap.getLeft( vertex ) );
	}

	@Override
	public WV getFocusedVertex( final WV ref )
	{
		return vertexMap.getRight( focus.getFocusedVertex( vertexMap.reusableLeftRef( ref ) ), ref );
	}

	@Override
	public boolean addFocusListener( final FocusListener listener )
	{
		return focus.addFocusListener( listener );
	}

	@Override
	public boolean removeFocusListener( final FocusListener listener )
	{
		return focus.removeFocusListener( listener );
	}
}
