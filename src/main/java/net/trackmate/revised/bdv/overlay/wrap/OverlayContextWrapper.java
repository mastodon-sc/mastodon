package net.trackmate.revised.bdv.overlay.wrap;

import java.util.Iterator;
import java.util.concurrent.locks.Lock;

import net.trackmate.graph.Edge;
import net.trackmate.graph.Vertex;
import net.trackmate.revised.bdv.overlay.OverlayContext;
import net.trackmate.revised.context.Context;
import net.trackmate.revised.context.ContextListener;

public class OverlayContextWrapper< V extends Vertex< E >, E extends Edge< V > >
		implements Context< V >, ContextListener< OverlayVertexWrapper< V, E > >
{
	private final OverlayContext< OverlayVertexWrapper< V, E > > context;

	private final ContextListener< V > contextListener;

	public OverlayContextWrapper(
			final OverlayContext< OverlayVertexWrapper< V, E > > context,
			final ContextListener< V > contextListener )
	{
		this.context = context;
		this.contextListener = contextListener;
		context.setContextListener( this );
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
	public void contextChanged( final Context< OverlayVertexWrapper< V, E > > context )
	{
		contextListener.contextChanged( this );
	}
}
