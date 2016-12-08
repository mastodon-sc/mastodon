package org.mastodon.adapter;

import org.mastodon.graph.Edge;
import org.mastodon.graph.Vertex;
import org.mastodon.revised.ui.selection.HighlightListener;
import org.mastodon.revised.ui.selection.HighlightModel;

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
	public boolean addHighlightListener( final HighlightListener listener )
	{
		return highlight.addHighlightListener( listener );
	}

	@Override
	public boolean removeHighlightListener( final HighlightListener listener )
	{
		return highlight.removeHighlightListener( listener );
	}
}
