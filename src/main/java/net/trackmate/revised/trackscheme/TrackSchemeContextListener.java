package net.trackmate.revised.trackscheme;

import java.util.Iterator;
import java.util.concurrent.locks.Lock;

import net.trackmate.graph.GraphIdBimap;
import net.trackmate.revised.trackscheme.context.Context;
import net.trackmate.revised.trackscheme.context.ContextListener;

/**
 * A {@link ContextListener} on vertice type {@code V} that wraps {@link Context
 * Context<V>} and forwards them to a {@link ContextListener ContextListener
 * <TrackSchemeVertex>}.
 *
 * @param <V>
 *            model vertex type.
 *
 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
 */
public class TrackSchemeContextListener< V > implements ContextListener< V >
{
	private final GraphIdBimap< V, ? > idmap;

	private final TrackSchemeGraph< ?, ? > graph;

	private ContextListener< TrackSchemeVertex > listener;

	private Context< V > previousContext;

	private TrackSchemeContext< V > trackSchemeContext;

	public TrackSchemeContextListener(
			final GraphIdBimap< V, ? > idmap,
			final TrackSchemeGraph< ?, ? > graph )
	{
		this.idmap = idmap;
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
		public int getTimepoint()
		{
			return context.getTimepoint();
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
	}
}
