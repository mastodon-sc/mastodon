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
import org.mastodon.graph.Vertex;
import org.mastodon.spatial.HasTimepoint;
import org.scijava.listeners.Listeners;

/**
 * A {@code FocusModel} that calls {@code notifyNavigateToVertex()} on
 * {@code focusVertex()}.
 * <p>
 * This allows to implement view-follows-focus behaviour on demand (without
 * having to hard-wire navigation into {@code FocusActions}).
 *
 * @param <V>
 *            the type of vertices in the graph.
 * @param <E>
 *            the type of edges in the graph.
 */
public class AutoNavigateFocusModel< V extends Vertex< E > & HasTimepoint, E extends Edge< V > >
		implements FocusModel< V >
{
	private final FocusModel< V > focus;

	private final NavigationHandler< V, E > navigation;

	private final TimepointModel timepointModel;

	public AutoNavigateFocusModel(
			final FocusModel< V > focus,
			final NavigationHandler< V, E > navigation )
	{
		this( focus, navigation, null );
	}

	public AutoNavigateFocusModel(
			final FocusModel< V > focus,
			final NavigationHandler< V, E > navigation,
			final TimepointModel timepointModel )
	{
		this.focus = focus;
		this.navigation = navigation;
		this.timepointModel = timepointModel;
	}

	@Override
	public void focusVertex( final V vertex )
	{
		if ( timepointModel != null && vertex != null )
			timepointModel.setTimepoint( vertex.getTimepoint() );
		focus.focusVertex( vertex );
		navigation.notifyNavigateToVertex( vertex );
	}

	@Override
	public V getFocusedVertex( final V ref )
	{
		return focus.getFocusedVertex( ref );
	}

	@Override
	public Listeners< FocusListener > listeners()
	{
		return focus.listeners();
	}
}
