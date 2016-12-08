package org.mastodon.revised.ui.selection;

import java.util.ArrayList;

import org.mastodon.graph.Edge;
import org.mastodon.graph.GraphIdBimap;
import org.mastodon.graph.GraphListener;
import org.mastodon.graph.Vertex;

/**
 * Class to manage the model vertex that has the "focus", regardless of how this
 * focus is used.
 *
 * @param <V>
 *            type of model vertices.
 * @param <E>
 *            the of model edges.
 */
public class FocusModelImp< V extends Vertex< E >, E extends Edge< V > >
		implements FocusModel< V, E >, GraphListener< V, E >
{
	private final GraphIdBimap< V, E > idmap;

	private int focusVertexId;

	private final ArrayList< FocusListener > listeners;

	public FocusModelImp( final GraphIdBimap< V, E > idmap )
	{
		this.idmap = idmap;
		focusVertexId = -1;
		listeners = new ArrayList< FocusListener >();
	}

	@Override
	public synchronized void focusVertex( final V vertex )
	{
		final int id = ( vertex == null ) ? - 1 : idmap.getVertexId( vertex );
		if ( focusVertexId != id )
		{
			focusVertexId = id;
			notifyListeners();
		}
	}

	@Override
	public synchronized V getFocusedVertex( final V ref )
	{
		return ( focusVertexId < 0 ) ?
				null : idmap.getVertex( focusVertexId, ref );
	}

	@Override
	public synchronized boolean addFocusListener( final FocusListener listener )
	{
		if ( ! listeners.contains( listener ) )
		{
			listeners.add( listener );
			return true;
		}
		return false;
	}

	@Override
	public synchronized boolean removeFocusListener( final FocusListener listener )
	{
		return listeners.remove( listener );
	}

	private void notifyListeners()
	{
		for ( final FocusListener l : listeners )
			l.focusChanged();
	}

	@Override
	public void graphRebuilt()
	{
		focusVertex( null );
// TODO: notifyListeners(); ? (This may change the layout and we might want to re-center on the focused vertex
	}

	@Override
	public void vertexAdded( final V vertex )
	{
// TODO: notifyListeners(); ? (This may change the layout and we might want to re-center on the focused vertex
	}

	@Override
	public synchronized void vertexRemoved( final V vertex )
	{
		if ( focusVertexId == idmap.getVertexId( vertex ) )
			focusVertex( null );
// TODO: notifyListeners(); ? (This may change the layout and we might want to re-center on the focused vertex
	}

	@Override
	public void edgeAdded( final E edge )
	{}

	@Override
	public synchronized void edgeRemoved( final E edge )
	{}
}
