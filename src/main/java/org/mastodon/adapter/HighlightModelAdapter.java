/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2021 Tobias Pietzsch, Jean-Yves Tinevez
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
import org.mastodon.model.HighlightListener;
import org.mastodon.model.HighlightModel;

/**
 * Adapts a {@code HighlightModel<V, E>} as a {@code HighlightModel<WV, WE>}.
 * The mapping between source vertices/edges ({@code V, E}) and wrapped
 * vertices/edges ({@code WV, WE}) is established by {@link RefBimap}s.
 *
 * @param <V>
 *            vertex type of source graph.
 * @param <E>
 *            edge type of source graph.
 * @param <WV>
 *            vertex type this wrapped {@link HighlightModel}.
 * @param <WE>
 *            edge type this wrapped {@link HighlightModel}.
 *
 * @author Tobias Pietzsch
 */
public class HighlightModelAdapter< V extends Vertex< E >, E extends Edge< V >, WV extends Vertex< WE >, WE extends Edge< WV > >
		implements HighlightModel< WV, WE >
{
	private final HighlightModel< V, E > highlight;

	private final RefBimap< V, WV > vertexMap;

	private final RefBimap< E, WE > edgeMap;

	private final ForwardedListeners< HighlightListener > listeners;

	public HighlightModelAdapter(
			final HighlightModel< V, E > highlight,
			final RefBimap< V, WV > vertexMap,
			final RefBimap< E, WE > edgeMap )
	{
		this.highlight = highlight;
		this.vertexMap = vertexMap;
		this.edgeMap = edgeMap;
		this.listeners = new ForwardedListeners.SynchronizedList<>( highlight.listeners() );
	}

	@Override
	public void highlightVertex( final WV vertex )
	{
		highlight.highlightVertex( vertexMap.getLeft( vertex ) );
	}

	@Override
	public void highlightEdge( final WE edge )
	{
		highlight.highlightEdge( edgeMap.getLeft( edge ) );
	}

	@Override
	public void clearHighlight()
	{
		highlight.clearHighlight();
	}

	@Override
	public WV getHighlightedVertex( final WV ref )
	{
		return vertexMap.getRight( highlight.getHighlightedVertex( vertexMap.reusableLeftRef( ref ) ), ref );
	}

	@Override
	public WE getHighlightedEdge( final WE ref )
	{
		return edgeMap.getRight( highlight.getHighlightedEdge( edgeMap.reusableLeftRef( ref ) ), ref );
	}

	@Override
	public ForwardedListeners< HighlightListener > listeners()
	{
		return listeners;
	}
}
