package org.mastodon.adapter;

import org.mastodon.graph.Edge;
import org.mastodon.graph.Vertex;
import org.mastodon.model.FocusListener;
import org.mastodon.model.FocusModel;
import org.mastodon.util.Listeners;

/**
 * Adapts a {@code FocusModel<V, E>} as a {@code FocusModel<WV, WE>}. The
 * mapping between source vertices/edges ({@code V, E}) and wrapped
 * vertices/edges ({@code WV, WE}) is established by {@link RefBimap}s.
 *
 * @param <V>
 *            vertex type of source graph.
 * @param <E>
 *            edge type of source graph.
 * @param <WV>
 *            vertex type this wrapped {@link FocusModel}.
 * @param <WE>
 *            edge type this wrapped {@link FocusModel}.
 *
 * @author Tobias Pietzsch
 */
public class FocusModelAdapter< V extends Vertex< E >, E extends Edge< V >, WV extends Vertex< WE >, WE extends Edge< WV > >
		implements FocusModel< WV, WE >
{
	private final FocusModel< V, E > focus;

	private final RefBimap< V, WV > vertexMap;

	private final RefBimap< E, WE > edgeMap;

	public FocusModelAdapter(
			final FocusModel< V, E > focus,
			final RefBimap< V, WV > vertexMap,
			final RefBimap< E, WE > edgeMap )
	{
		this.focus = focus;
		this.vertexMap = vertexMap;
		this.edgeMap = edgeMap;
	}

	@Override
	public void focusVertex( final WV vertex )
	{
		focus.focusVertex( vertexMap.getLeft( vertex ) );
	}

	@Override
	public WV getFocusedVertex( final WV ref )
	{
		return vertexMap.getRight( focus.getFocusedVertex( vertexMap.reusableLeftRef( ref ) ), ref );
	}

	@Override
	public Listeners< FocusListener > listeners()
	{
		return focus.listeners();
	}
}
