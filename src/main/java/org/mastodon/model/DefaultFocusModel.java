/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2024 Tobias Pietzsch, Jean-Yves Tinevez
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
 * Class to manage the model vertex that has the "focus", regardless of how this
 * focus is used.
 *
 * @param <V>
 *            type of model vertices.
 * @param <E>
 *            the of model edges.
 */
public class DefaultFocusModel< V extends Vertex< E >, E extends Edge< V > >
		implements FocusModel< V >, GraphListener< V, E >
{
	private final GraphIdBimap< V, E > idmap;

	private int focusVertexId;

	private final Listeners.List< FocusListener > listeners;

	public DefaultFocusModel( final GraphIdBimap< V, E > idmap )
	{
		this.idmap = idmap;
		focusVertexId = -1;
		listeners = new Listeners.SynchronizedList<>();
	}

	@Override
	public synchronized void focusVertex( final V vertex )
	{
		final int id = ( vertex == null ) ? -1 : idmap.getVertexId( vertex );
		if ( focusVertexId != id )
		{
			focusVertexId = id;
			notifyListeners();
		}
	}

	@Override
	public synchronized V getFocusedVertex( final V ref )
	{
		return ( focusVertexId < 0 ) ? null : idmap.getVertex( focusVertexId, ref );
	}

	@Override
	public Listeners< FocusListener > listeners()
	{
		return listeners;
	}

	private void notifyListeners()
	{
		for ( final FocusListener l : listeners.list )
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
