/*-
 * #%L
 * Mastodon
 * %%
 * Copyright (C) 2014 - 2023 Tobias Pietzsch, Jean-Yves Tinevez
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
import org.mastodon.model.FadingListener;
import org.mastodon.model.FadingModel;
import org.mastodon.model.SelectionModel;
import org.scijava.listeners.Listeners;

import javax.annotation.Nullable;

/**
 * Adapts a {@code FadedModel<V, E>} as a {@code FadedModel<WV, WE>}.
 * The mapping between source vertices/edges ({@code V, E}) and wrapped
 * vertices/edges ({@code WV, WE}) is established by {@link RefBimap}s.
 *
 * @param <V>
 *            vertex type of source graph.
 * @param <E>
 *            edge type of source graph.
 * @param <WV>
 *            vertex type of the wrapped {@link SelectionModel}.
 * @param <WE>
 *            edge type of the wrapped {@link SelectionModel}.
 */
public class FadingModelAdapter< V extends Vertex< E >, E extends Edge< V >, WV extends Vertex< WE >,
		WE extends Edge< WV > >
		implements FadingModel< WV, WE >, FadingListener
{
	@Nullable
	private FadingModel< V, E > fadingModel;

	private final RefBimap< V, WV > vertexMap;

	private final RefBimap< E, WE > edgeMap;

	private final Listeners.List< FadingListener > listeners = new Listeners.SynchronizedList<>();

	public FadingModelAdapter(
			@Nullable final FadingModel< V, E > fadingModel,
			final RefBimap< V, WV > vertexMap,
			final RefBimap< E, WE > edgeMap )
	{
		this.fadingModel = fadingModel;
		this.vertexMap = vertexMap;
		this.edgeMap = edgeMap;
	}

	@Override
	public boolean isFaded( final WV vertex )
	{
		if ( fadingModel == null )
			return false;
		return fadingModel.isFaded( vertexMap.getLeft( vertex ) );
	}

	@Override
	public boolean isFaded( final WE edge )
	{
		if ( fadingModel == null )
			return false;
		return fadingModel.isFaded( edgeMap.getLeft( edge ) );
	}

	@Override
	public Listeners< FadingListener > listeners()
	{
		return listeners;
	}

	public void setFadingModel( final @Nullable FadingModel< V, E > fadingModel )
	{
		if ( this.fadingModel != null )
			this.fadingModel.listeners().remove( this );
		this.fadingModel = fadingModel;
		if ( this.fadingModel != null )
			this.fadingModel.listeners().add( this );
	}

	public void removeAllListeners()
	{
		if ( fadingModel != null )
			fadingModel.listeners().remove( this );
		listeners.list.clear();
	}

	@Override
	public void fadingChanged()
	{
		for ( FadingListener fadingListener : listeners.list )
			fadingListener.fadingChanged();
	}
}
