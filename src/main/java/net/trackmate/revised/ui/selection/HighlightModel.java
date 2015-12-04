package net.trackmate.revised.ui.selection;

import java.util.ArrayList;

import net.trackmate.graph.Edge;
import net.trackmate.graph.GraphIdBimap;
import net.trackmate.graph.Vertex;
import net.trackmate.graph.listenable.GraphListener;

/**
 * Manages the highlighted vertex.
 * <p>
 * A highlighted vertex is a vertex that is drawn in a salient manner across all
 * the views opened on a single model. It is meant to quickly highlight a single
 * vertex on all views while the mouse is overing above its representation in
 * any view.
 *
 * @param <V>
 *            the type of the model vertices.
 * @param <E>
 *            the type of the model edges.
 */
//TODO: should HighlightModel be an interface
public class HighlightModel< V extends Vertex< E >, E extends Edge< V > > implements GraphListener< V, E >
{
	private final GraphIdBimap< V, E > idmap;

	private int highlightedVertexId;

	private final ArrayList< HighlightListener > listeners;

	/**
	 * Creates a new highlight model for the graph with the specified
	 * bidirectional map.
	 * 
	 * @param idmap
	 *            the graph bidirectional map from vertices and edges to their
	 *            id.
	 */
	public HighlightModel( final GraphIdBimap< V, E > idmap )
	{
		this.idmap = idmap;
		highlightedVertexId = -1;
		listeners = new ArrayList< HighlightListener >();
	}

	/**
	 * Sets the specified vertex highlighted in this model.
	 * 
	 * @param vertex
	 *            the vertex to highlight.
	 */
	public synchronized void highlightVertex( final V vertex )
	{
		final int id = ( vertex == null ) ? - 1 : idmap.getVertexId( vertex );
		if ( highlightedVertexId != id )
		{
			highlightedVertexId = id;
			notifyListeners();
		}
	}

	/**
	 * Returns the vertex highlighted in this model.
	 * 
	 * @param ref
	 *            a vertex reference used for retrieval.
	 * @return the highlighted vertex.
	 */
	public synchronized V getHighlightedVertex( final V ref )
	{
		return ( highlightedVertexId < 0 ) ?
				null : idmap.getVertex( highlightedVertexId, ref );
	}

	/**
	 * Registers a HighlightListener to this highlight model, that will be
	 * notified when the highlighted vertex changes.
	 * 
	 * @param listener
	 *            the listener to register.
	 * @return {@code true} if the listener was successfully registered.
	 *         {@code false} if it was already registered.
	 */
	public boolean addHighlightListener( final HighlightListener listener )
	{
		if ( ! listeners.contains( listener ) )
		{
			listeners.add( listener );
			return true;
		}
		return false;
	}

	/**
	 * Removes the specified listener from the listeners of this highlight
	 * model.
	 * 
	 * @param listener
	 *            the listener to remove.
	 * @return {@code true} if the listener was present in the listeners of
	 *         this model and was succesfully removed.
	 */
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
