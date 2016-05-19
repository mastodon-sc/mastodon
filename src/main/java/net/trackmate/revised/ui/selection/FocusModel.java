package net.trackmate.revised.ui.selection;

import java.util.ArrayList;

import net.trackmate.graph.Edge;
import net.trackmate.graph.GraphIdBimap;
import net.trackmate.graph.GraphListener;
import net.trackmate.graph.Vertex;

/**
 * Class to manage the model vertex that has the "focus", regardless of how this
 * focus is used.
 *
 * @param <V>
 *            type of model vertices.
 * @param <E>
 *            the of model edges.
 */
public class FocusModel< V extends Vertex< E >, E extends Edge< V > > implements GraphListener< V, E >
{
	private final GraphIdBimap< V, E > idmap;

	private int focusVertexId;

	private final ArrayList< FocusListener > listeners;

	public FocusModel( final GraphIdBimap< V, E > idmap )
	{
		this.idmap = idmap;
		focusVertexId = -1;
		listeners = new ArrayList< FocusListener >();
	}

	public synchronized void focusVertex( final V vertex )
	{
		final int id = ( vertex == null ) ? - 1 : idmap.getVertexId( vertex );
		if ( focusVertexId != id )
		{
			focusVertexId = id;
			notifyListeners();
		}
	}

	public synchronized V getFocusedVertex( final V ref )
	{
		return ( focusVertexId < 0 ) ?
				null : idmap.getVertex( focusVertexId, ref );
	}

	public synchronized boolean addFocusListener( final FocusListener listener )
	{
		if ( ! listeners.contains( listener ) )
		{
			listeners.add( listener );
			return true;
		}
		return false;
	}

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
