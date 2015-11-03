package net.trackmate.revised.ui.selection;

import java.util.ArrayList;

import net.trackmate.graph.Edge;
import net.trackmate.graph.GraphIdBimap;
import net.trackmate.graph.Vertex;
import net.trackmate.graph.listenable.GraphListener;

public class HighlightModel< V extends Vertex< E >, E extends Edge< V > > implements GraphListener< V, E >
{
	private final GraphIdBimap< V, E > idmap;

	private int highlightedVertexId;

	private final ArrayList< HighlightListener > listeners;

	public HighlightModel( final GraphIdBimap< V, E > idmap )
	{
		this.idmap = idmap;
		highlightedVertexId = -1;
		listeners = new ArrayList< HighlightListener >();
	}

	public synchronized void highlightVertex( final V vertex )
	{
		final int id = ( vertex == null ) ? - 1 : idmap.getVertexId( vertex );
		if ( highlightedVertexId != id )
		{
			highlightedVertexId = id;
			notifyListeners();
		}
	}

	public synchronized V getHighlightedVertex( final V ref )
	{
		return ( highlightedVertexId < 0 ) ?
				null : idmap.getVertex( highlightedVertexId, ref );
	}

	public boolean addHighlightListener( final HighlightListener listener )
	{
		if ( ! listeners.contains( listener ) )
		{
			listeners.add( listener );
			return true;
		}
		return false;
	}

	public boolean removeHighlightListener( final HighlightListener listener )
	{
		return listeners.remove( listener );
	}

	private void notifyListeners()
	{
		for ( final HighlightListener l : listeners )
			l.highlightChanged();
	}

	@Override
	public void graphRebuilt()
	{
		highlightVertex( null );
	}

	@Override
	public void vertexAdded( final V vertex )
	{}

	@Override
	public synchronized void vertexRemoved( final V vertex )
	{
		if ( highlightedVertexId == idmap.getVertexId( vertex ) )
			highlightVertex( null );
	}

	@Override
	public void edgeAdded( final E edge )
	{}

	@Override
	public synchronized void edgeRemoved( final E edge )
	{}
}
