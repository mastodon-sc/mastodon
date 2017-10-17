package org.mastodon.adapter;

import org.mastodon.graph.Edge;
import org.mastodon.graph.Vertex;
import org.mastodon.model.HighlightListener;
import org.mastodon.model.HighlightModel;
import org.mastodon.util.Listeners;

/**
 * Adapts a {@code HighlightModel<V, E>} as a {@code HighlightModel<WV, WE>}.
 * The mapping between source vertices/edges ({@code V, E}) and wrapped
 * vertices/edges ({@code WV, WE}) is established by {@link RefBimap}s.
 *
 * @param <V>
 *            vertex type of source graph.
 * @param <E>
 *            edge type of source graph.
 * @param <WV>
 *            vertex type this wrapped {@link HighlightModel}.
 * @param <WE>
 *            edge type this wrapped {@link HighlightModel}.
 *
 * @author Tobias Pietzsch
 */
public class HighlightAdapter< V extends Vertex< E >, E extends Edge< V >, WV extends Vertex< WE >, WE extends Edge< WV > >
		implements HighlightModel< WV, WE >
{
	private final HighlightModel< V, E > highlight;

	private final RefBimap< V, WV > vertexMap;

	private final RefBimap< E, WE > edgeMap;

	public HighlightAdapter(
			final HighlightModel< V, E > highlight,
			final RefBimap< V, WV > vertexMap,
			final RefBimap< E, WE > edgeMap )
	{
		this.highlight = highlight;
		this.vertexMap = vertexMap;
		this.edgeMap = edgeMap;
	}

	@Override
	public void highlightVertex( final WV vertex )
	{
		highlight.highlightVertex( vertexMap.getLeft( vertex ) );
	}

	@Override
	public void highlightEdge( final WE edge )
	{
		highlight.highlightEdge( edgeMap.getLeft( edge ) );
	}

	@Override
	public void clearHighlight()
	{
		highlight.clearHighlight();
	}

	@Override
	public WV getHighlightedVertex( final WV ref )
	{
		return vertexMap.getRight( highlight.getHighlightedVertex( vertexMap.reusableLeftRef( ref ) ), ref );
	}

	@Override
	public WE getHighlightedEdge( final WE ref )
	{
		return edgeMap.getRight( highlight.getHighlightedEdge( edgeMap.reusableLeftRef( ref ) ), ref );
	}

	@Override
	public Listeners< HighlightListener > listeners()
	{
		return highlight.listeners();
	}
}
