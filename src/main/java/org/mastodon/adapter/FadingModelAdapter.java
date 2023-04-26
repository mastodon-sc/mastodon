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
		implements FadingModel< WV, WE >
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
		this.fadingModel = fadingModel;
		if ( this.fadingModel != null )
			fadingModel.listeners().addAll( listeners.list );
	}

	@Override
	public void timepointChanged()
	{
		if ( fadingModel != null )
			fadingModel.timepointChanged();
	}
}
