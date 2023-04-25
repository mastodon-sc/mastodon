package org.mastodon.adapter;

import org.mastodon.graph.Edge;
import org.mastodon.graph.Vertex;
import org.mastodon.model.FadedModel;
import org.mastodon.model.SelectionModel;
import org.mastodon.model.TimepointListener;
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
public class FadedModelAdapter< V extends Vertex< E >, E extends Edge< V >, WV extends Vertex< WE >,
		WE extends Edge< WV > >
		implements FadedModel< WV, WE >
{
	@Nullable
	private FadedModel< V, E > fadedModel;

	private final RefBimap< V, WV > vertexMap;

	private final RefBimap< E, WE > edgeMap;

	private final Listeners.List< TimepointListener > listeners = new Listeners.SynchronizedList<>();

	public FadedModelAdapter(
			@Nullable final FadedModel< V, E > fadedModel,
			final RefBimap< V, WV > vertexMap,
			final RefBimap< E, WE > edgeMap )
	{
		this.fadedModel = fadedModel;
		this.vertexMap = vertexMap;
		this.edgeMap = edgeMap;
	}

	@Override
	public boolean isFaded( final WV vertex )
	{
		if ( fadedModel == null )
			return false;
		return fadedModel.isFaded( vertexMap.getLeft( vertex ) );
	}

	@Override
	public boolean isFaded( final WE edge )
	{
		if ( fadedModel == null )
			return false;
		return fadedModel.isFaded( edgeMap.getLeft( edge ) );
	}

	@Override
	public Listeners< TimepointListener > listeners()
	{
		return listeners;
	}

	public void setFadedModel( final @Nullable FadedModel< V, E > fadedModel )
	{
		this.fadedModel = fadedModel;
		if ( this.fadedModel != null )
			fadedModel.listeners().addAll( listeners.list );
	}
}
