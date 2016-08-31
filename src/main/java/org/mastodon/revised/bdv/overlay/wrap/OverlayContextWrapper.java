package org.mastodon.revised.bdv.overlay.wrap;

import java.util.Iterator;
import java.util.concurrent.locks.Lock;

import org.mastodon.graph.Edge;
import org.mastodon.graph.Vertex;
import org.mastodon.revised.bdv.overlay.OverlayContext;
import org.mastodon.revised.context.Context;
import org.mastodon.revised.context.ContextListener;
import org.mastodon.revised.mamut.WindowManager.BdvContextAdapter;

/**
 * TODO!!! related to {@link BdvContextAdapter}
 *
 * @param <V>
 * @param <E>
 *
 * @author Tobias Pietzsch &lt;tobias.pietzsch@gmail.com&gt;
 */
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
