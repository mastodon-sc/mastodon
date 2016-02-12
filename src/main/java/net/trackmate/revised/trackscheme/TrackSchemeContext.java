package net.trackmate.revised.trackscheme;

import java.util.Iterator;
import java.util.concurrent.locks.Lock;

import net.trackmate.graph.GraphIdBimap;
import net.trackmate.revised.trackscheme.context.Context;

public class TrackSchemeContext< V > implements
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
