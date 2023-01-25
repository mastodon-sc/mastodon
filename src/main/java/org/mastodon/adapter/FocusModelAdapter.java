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
package org.mastodon.adapter;

import org.mastodon.graph.Edge;
import org.mastodon.graph.Vertex;
import org.mastodon.model.FocusListener;
import org.mastodon.model.FocusModel;

/**
 * Adapts a {@code FocusModel<V, E>} as a {@code FocusModel<WV, WE>}. The
 * mapping between source vertices/edges ({@code V, E}) and wrapped
 * vertices/edges ({@code WV, WE}) is established by {@link RefBimap}s.
 *
 * @param <V>
 *            vertex type of source graph.
 * @param <E>
 *            edge type of source graph.
 * @param <WV>
 *            vertex type of this wrapped {@link FocusModel}.
 * @param <WE>
 *            edge type of this wrapped {@link FocusModel}.
 *
 * @author Tobias Pietzsch
 */
public class FocusModelAdapter< V extends Vertex< E >, E extends Edge< V >, WV extends Vertex< WE >,
		WE extends Edge< WV > >
		implements FocusModel< WV, WE >
{
	private final FocusModel< V, E > focus;

	private final RefBimap< V, WV > vertexMap;

	private final ForwardedListeners< FocusListener > listeners;

	public FocusModelAdapter(
			final FocusModel< V, E > focus,
			final RefBimap< V, WV > vertexMap,
			final RefBimap< E, WE > edgeMap )
	{
		this.focus = focus;
		this.vertexMap = vertexMap;
		this.listeners = new ForwardedListeners.List<>( focus.listeners() );
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
	public ForwardedListeners< FocusListener > listeners()
	{
		return listeners;
	}
}
