/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2022 Tobias Pietzsch, Jean-Yves Tinevez
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package org.mastodon.model;

import org.mastodon.graph.Edge;
import org.mastodon.graph.GraphIdBimap;
import org.mastodon.graph.GraphListener;
import org.mastodon.graph.Vertex;
import org.scijava.listeners.Listeners;

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
public class DefaultHighlightModel< V extends Vertex< E >, E extends Edge< V > >
		implements HighlightModel< V, E >, GraphListener< V, E >
{
	private final GraphIdBimap< V, E > idmap;

	private int highlightedVertexId;

	private int highlightedEdgeId;

	private final Listeners.List< HighlightListener > listeners;

	/**
	 * Creates a new highlight model for the graph with the specified
	 * bidirectional map.
	 *
	 * @param idmap
	 *            the graph bidirectional map from vertices and edges to their
	 *            id.
	 */
	public DefaultHighlightModel( final GraphIdBimap< V, E > idmap )
	{
		this.idmap = idmap;
		highlightedVertexId = -1;
		highlightedEdgeId = -1;
		listeners = new Listeners.SynchronizedList<>();
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
		final int id = ( vertex == null ) ? -1 : idmap.getVertexId( vertex );
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
		final int id = ( edge == null ) ? -1 : idmap.getEdgeId( edge );
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
		return ( highlightedVertexId < 0 ) ? null : idmap.getVertex( highlightedVertexId, ref );
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
		return ( highlightedEdgeId < 0 ) ? null : idmap.getEdge( highlightedEdgeId, ref );
	}

	@Override
	public Listeners< HighlightListener > listeners()
	{
		return listeners;
	}

	private void notifyListeners()
	{
		for ( final HighlightListener l : listeners.list )
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
			clearHighlight();
	}

	@Override
	public void edgeAdded( final E edge )
	{}

	@Override
	public synchronized void edgeRemoved( final E edge )
	{
		if ( highlightedEdgeId == idmap.getEdgeId( edge ) )
			clearHighlight();
	}
}
