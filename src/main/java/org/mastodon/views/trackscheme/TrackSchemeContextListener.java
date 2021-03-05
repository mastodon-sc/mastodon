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
package org.mastodon.views.trackscheme;

import java.util.Iterator;
import java.util.concurrent.locks.Lock;

import org.mastodon.graph.GraphIdBimap;
import org.mastodon.graph.Vertex;
import org.mastodon.views.context.Context;
import org.mastodon.views.context.ContextListener;

/**
 * A {@link ContextListener} on vertice type {@code V} that wraps {@link Context
 * Context&lt;V&gt;} and forwards them to a {@link ContextListener ContextListener
 * &lt;TrackSchemeVertex&gt;}.
 *
 * @param <V>
 *            model vertex type.
 *
 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
 */
public class TrackSchemeContextListener< V extends Vertex< ? > >  implements ContextListener< V >
{
	private final GraphIdBimap< V, ? > idmap;

	private final TrackSchemeGraph< ?, ? > graph;

	private ContextListener< TrackSchemeVertex > listener;

	private Context< V > previousContext;

	private TrackSchemeContext< V > trackSchemeContext;

	public TrackSchemeContextListener( final TrackSchemeGraph< V, ? > graph )
	{
		this.idmap = graph.getGraphIdBimap();
		this.graph = graph;
		listener = null;
		previousContext = null;
		trackSchemeContext = null;
	}

	@Override
	public synchronized void contextChanged( final Context< V > context )
	{
		if ( previousContext != context )
		{
			previousContext = context;
			trackSchemeContext = ( context == null )
					? null
					: new TrackSchemeContext< >( idmap, graph, context );
		}
		if ( listener != null )
			listener.contextChanged( trackSchemeContext );
	}

	public synchronized void setContextListener( final ContextListener< TrackSchemeVertex > l )
	{
		listener = l;
		listener.contextChanged( trackSchemeContext );
	}

	/**
	 * Wraps a {@link Context} on model vertices as a {@link Context} of
	 * {@link TrackSchemeVertex TrackSchemeVertices}.
	 *
	 * @param <V>
	 *            model vertex type.
	 *
	 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
	 */
	static class TrackSchemeContext< V > implements
			Context< TrackSchemeVertex >
	{
		private final GraphIdBimap< V, ? > idmap;

		private final TrackSchemeGraph< ?, ? > graph;

		private final Context< V > context;

		public TrackSchemeContext(
				final GraphIdBimap< V, ? > idmap,
				final TrackSchemeGraph< ?, ? > graph,
				final Context< V > context )
		{
			this.idmap = idmap;
			this.graph = graph;
			this.context = context;
		}

		@Override
		public Lock readLock()
		{
			return context.readLock();
		}

		@Override
		public Iterable< TrackSchemeVertex > getInsideVertices( final int timepoint )
		{
			final Iterable< V > insideVertices = context.getInsideVertices( timepoint );

			return new Iterable< TrackSchemeVertex >()
			{
				@Override
				public Iterator< TrackSchemeVertex > iterator()
				{
					return new Iterator< TrackSchemeVertex >()
					{
						private final Iterator< V > it = insideVertices.iterator();

						private final TrackSchemeVertex ref = graph.vertexRef();

						@Override
						public boolean hasNext()
						{
							return it.hasNext();
						}

						@Override
						public TrackSchemeVertex next()
						{
							return graph.getTrackSchemeVertexForModelId( idmap.getVertexId( it.next() ), ref );
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
}
