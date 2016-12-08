package org.mastodon.revised.ui.selection;

import java.util.ArrayList;

import org.mastodon.graph.Edge;
import org.mastodon.graph.GraphIdBimap;
import org.mastodon.graph.GraphListener;
import org.mastodon.graph.Vertex;

/**
 * Manages the highlighted vertex.
 * <p>
 * A highlighted vertex is a vertex that is drawn in a salient manner across all
 * the views opened on a single model. It is meant to quickly highlight a single
 * vertex on all views while the mouse is hovering above its representation in
 * any view.
 *
 * @param <V>
 *            the type of the model vertices.
 * @param <E>
 *            the type of the model edges.
 */
//TODO: should HighlightModel be an interface
public class HighlightModelImp< V extends Vertex< E >, E extends Edge< V > >
		implements HighlightModel< V, E >, GraphListener< V, E >
{
	private final GraphIdBimap< V, E > idmap;

	private int highlightedVertexId;

	private int highlightedEdgeId;

	private final ArrayList< HighlightListener > listeners;

	/**
	 * Creates a new highlight model for the graph with the specified
	 * bidirectional map.
	 *
	 * @param idmap
	 *            the graph bidirectional map from vertices and edges to their
	 *            id.
	 */
	public HighlightModelImp( final GraphIdBimap< V, E > idmap )
	{
		this.idmap = idmap;
		highlightedVertexId = -1;
		highlightedEdgeId = -1;
		listeners = new ArrayList< HighlightListener >();
	}

	/**
	 * Sets the specified vertex highlighted in this model.
	 *
	 * @param vertex
	 *            the vertex to highlight, or {@code null} to clear highlight.
	 */
	@Override
	public synchronized void highlightVertex( final V vertex )
	{
		final int id = ( vertex == null ) ? - 1 : idmap.getVertexId( vertex );
		if ( id < 0 )
			clearHighlight();
		else if ( highlightedVertexId != id )
		{
			highlightedVertexId = id;
			highlightedEdgeId = -1;
			notifyListeners();
		}
	}

	/**
	 * Sets the specified edge highlighted in this model.
	 *
	 * @param edge
	 *            the edge to highlight, or {@code null} to clear highlight.
	 */
	@Override
	public synchronized void highlightEdge( final E edge )
	{
		final int id = ( edge == null ) ? - 1 : idmap.getEdgeId( edge );
		if ( id < 0 )
			clearHighlight();
		else if ( highlightedEdgeId != id )
		{
			highlightedVertexId = -1;
			highlightedEdgeId = id;
			notifyListeners();
		}
	}

	/**
	 * Clear highlight.
	 */
	@Override
	public synchronized void clearHighlight()
	{
		boolean notify = false;
		if ( highlightedEdgeId >= 0 )
		{
			highlightedEdgeId = -1;
			notify = true;
		}
		if ( highlightedVertexId >= 0 )
		{
			highlightedVertexId = -1;
			notify = true;
		}
		if ( notify )
			notifyListeners();
	}

	/**
	 * Returns the vertex highlighted in this model.
	 *
	 * @param ref
	 *            a vertex reference used for retrieval.
	 * @return the highlighted vertex, or {@code null} if no vertex is
	 *         highlighted.
	 */
	@Override
	public synchronized V getHighlightedVertex( final V ref )
	{
		return ( highlightedVertexId < 0 ) ?
				null : idmap.getVertex( highlightedVertexId, ref );
	}

	/**
	 * Returns the edge highlighted in this model.
	 *
	 * @param ref
	 *            an edge reference used for retrieval.
	 * @return the highlighted edge, or {@code null} if no edge is
	 *         highlighted.
	 */
	@Override
	public synchronized E getHighlightedEdge( final E ref )
	{
		return ( highlightedEdgeId < 0 ) ?
				null : idmap.getEdge( highlightedEdgeId, ref );
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
	@Override
	public synchronized boolean addHighlightListener( final HighlightListener listener )
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
	@Override
	public synchronized boolean removeHighlightListener( final HighlightListener listener )
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
