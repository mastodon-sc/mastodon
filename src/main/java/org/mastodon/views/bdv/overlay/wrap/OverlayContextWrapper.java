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
package org.mastodon.views.bdv.overlay.wrap;

import java.util.Iterator;
import java.util.concurrent.locks.Lock;

import org.mastodon.graph.Edge;
import org.mastodon.graph.Vertex;
import org.mastodon.views.bdv.overlay.OverlayContext;
import org.mastodon.views.context.Context;
import org.mastodon.views.context.ContextListener;

/**
 * Wraps an {@link OverlayContext} (a {@code Context} on
 * {@code OverlayVertexWrapper}) as a {@code Context} on model vertices
 * {@code V}. Passes on {@code contextChanged} notifications to a listener.
 *
 * @param <V>
 *            the type of model vertex wrapped.
 * @param <E>
 *            the type of model edge wrapped.
 *
 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
 */
public class OverlayContextWrapper< V extends Vertex< E >, E extends Edge< V > >
		implements Context< V >
{
	private final OverlayContext< OverlayVertexWrapper< V, E > > context;

	/**
	 * @param context
	 *            {@link OverlayContext} to wrap
	 * @param contextListener
	 *            {@code contextChanged} of {@code context} are translated an
	 *            send to this listeners.
	 */
	public OverlayContextWrapper(
			final OverlayContext< OverlayVertexWrapper< V, E > > context,
			final ContextListener< V > contextListener )
	{
		this.context = context;
		context.setContextListener( c -> contextListener.contextChanged( this ) );
	}

	@Override
	public Lock readLock()
	{
		return context.readLock();
	}

	@Override
	public Iterable< V > getInsideVertices( final int timepoint )
	{
		final Iterable< OverlayVertexWrapper< V, E > > insideVertices = context.getInsideVertices( timepoint );
		return new Iterable< V >()
		{
			@Override
			public Iterator< V > iterator()
			{
				final Iterator< OverlayVertexWrapper< V, E > > iter = insideVertices.iterator();
				return new Iterator< V >()
				{
					@Override
					public boolean hasNext()
					{
						return iter.hasNext();
					}

					@Override
					public V next()
					{
						return iter.next().wv;
					}
				};
			}
		};
	}

	@Override
	public int getTimepoint()
	{
		return context.getTimepoint();
	}
}
